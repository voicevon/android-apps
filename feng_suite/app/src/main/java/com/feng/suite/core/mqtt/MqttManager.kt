package com.feng.suite.core.mqtt

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.feng.suite.feature.clock.alarm.AlarmScheduler
import com.feng.suite.feature.clock.data.AlarmDatabase
import com.feng.suite.feature.clock.data.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.UUID

enum class MqttConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class MqttConfig(
    val serverHost: String = "broker.emqx.io",
    val serverPort: Int = 1883,
    val deviceName: String = "设备-${Build.MODEL.takeLast(4)}",
    val isEnabled: Boolean = false
)

object MqttManager {

    private const val TAG = "MqttManager"
    private const val PREFS_NAME = "feng_mqtt_prefs"
    private const val KEY_HOST = "mqtt_host"
    private const val KEY_PORT = "mqtt_port"
    private const val KEY_DEVICE_NAME = "mqtt_device_name"
    private const val KEY_ENABLED = "mqtt_enabled"

    private const val TOPIC_BROADCAST = "feng_suite/alarm/broadcast"
    private const val TOPIC_DEVICE_PREFIX = "feng_suite/alarm/device/"

    private var appContext: Context? = null
    private var mqttClient: MqttClient? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionStatus = MutableStateFlow(MqttConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<MqttConnectionStatus> = _connectionStatus.asStateFlow()

    private val _config = MutableStateFlow(MqttConfig())
    val config: StateFlow<MqttConfig> = _config.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        loadConfig(context)

        if (_config.value.isEnabled) {
            connect()
        }
    }

    private fun loadConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultDeviceName = "设备-${Build.MODEL.takeLast(4).ifEmpty { "A" }}"
        val host = prefs.getString(KEY_HOST, "broker.emqx.io") ?: "broker.emqx.io"
        val port = prefs.getInt(KEY_PORT, 1883)
        val deviceName = prefs.getString(KEY_DEVICE_NAME, defaultDeviceName) ?: defaultDeviceName
        val isEnabled = prefs.getBoolean(KEY_ENABLED, false)

        _config.value = MqttConfig(
            serverHost = host,
            serverPort = port,
            deviceName = deviceName,
            isEnabled = isEnabled
        )
    }

    fun saveConfig(host: String, port: Int, deviceName: String, isEnabled: Boolean) {
        val context = appContext ?: return
        val cleanHost = host.trim()
        val cleanDevice = deviceName.trim().ifEmpty { "设备-${Build.MODEL.takeLast(4)}" }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_HOST, cleanHost)
            .putInt(KEY_PORT, port)
            .putString(KEY_DEVICE_NAME, cleanDevice)
            .putBoolean(KEY_ENABLED, isEnabled)
            .apply()

        _config.value = MqttConfig(
            serverHost = cleanHost,
            serverPort = port,
            deviceName = cleanDevice,
            isEnabled = isEnabled
        )

        disconnect()
        if (isEnabled) {
            connect()
        }
    }

    @Synchronized
    fun connect() {
        val context = appContext ?: return
        val currentCfg = _config.value
        if (currentCfg.serverHost.isBlank()) return

        scope.launch {
            try {
                disconnect()
                _connectionStatus.value = MqttConnectionStatus.CONNECTING

                val brokerUri = "tcp://${currentCfg.serverHost}:${currentCfg.serverPort}"
                val clientId = "feng_suite_" + UUID.randomUUID().toString().take(8)

                val client = MqttClient(brokerUri, clientId, MemoryPersistence())
                val options = MqttConnectOptions().apply {
                    isAutomaticReconnect = true
                    isCleanSession = true
                    connectionTimeout = 8
                    keepAliveInterval = 20
                }

                client.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        Log.d(TAG, "MQTT Connected successfully: $serverURI (reconnect=$reconnect)")
                        _connectionStatus.value = MqttConnectionStatus.CONNECTED
                        subscribeTopics(client, currentCfg.deviceName)
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.w(TAG, "MQTT Connection lost", cause)
                        _connectionStatus.value = MqttConnectionStatus.DISCONNECTED
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        handleIncomingMessage(topic, message)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })

                client.connect(options)
                mqttClient = client
            } catch (e: Exception) {
                Log.e(TAG, "MQTT Connect failed: ${e.message}", e)
                _connectionStatus.value = MqttConnectionStatus.ERROR
            }
        }
    }

    @Synchronized
    fun disconnect() {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    it.disconnect()
                }
                it.close()
            }
        } catch (_: Exception) {}
        mqttClient = null
        _connectionStatus.value = MqttConnectionStatus.DISCONNECTED
    }

    private fun subscribeTopics(client: MqttClient, deviceName: String) {
        try {
            // 1. 订阅公共广播
            client.subscribe(TOPIC_BROADCAST, 1)
            // 2. 订阅本设备专属直接下发 Topic
            val directTopic = "$TOPIC_DEVICE_PREFIX$deviceName"
            client.subscribe(directTopic, 1)
            Log.d(TAG, "Subscribed to $TOPIC_BROADCAST and $directTopic")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe topics", e)
        }
    }

    fun publishAlarm(alarm: AlarmEntity, action: String = "CREATE_OR_UPDATE") {
        if (!_config.value.isEnabled) return
        val client = mqttClient ?: return
        if (!client.isConnected) return

        scope.launch {
            try {
                val myDevice = _config.value.deviceName
                val target = if (alarm.targetDevice.isBlank()) "全部设备" else alarm.targetDevice

                val json = JSONObject().apply {
                    put("action", action)
                    put("sourceDevice", myDevice)
                    put("targetDevice", target)
                    put("id", alarm.id)
                    put("hour", alarm.hour)
                    put("minute", alarm.minute)
                    put("isEnabled", alarm.isEnabled)
                    put("daysBitmask", alarm.daysBitmask)
                    put("label", alarm.label)
                    put("isVibrate", alarm.isVibrate)
                    put("snoozeMinutes", alarm.snoozeMinutes)
                    put("timestamp", System.currentTimeMillis())
                }

                val payload = json.toString().toByteArray(Charsets.UTF_8)
                val message = MqttMessage(payload).apply { qos = 1 }

                // 若指定了目标设备且非全部设备，发布到专属设备 topic，同时也发送到 broadcast 携带 targetDevice
                if (target != "全部设备" && target != "本机") {
                    client.publish("$TOPIC_DEVICE_PREFIX$target", message)
                } else {
                    client.publish(TOPIC_BROADCAST, message)
                }
                Log.d(TAG, "Published alarm to target=$target: $json")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to publish alarm", e)
            }
        }
    }

    private fun handleIncomingMessage(topic: String?, message: MqttMessage?) {
        if (message == null) return
        val context = appContext ?: return

        scope.launch {
            try {
                val bodyStr = String(message.payload, Charsets.UTF_8)
                val json = JSONObject(bodyStr)

                val sourceDevice = json.optString("sourceDevice", "")
                val targetDevice = json.optString("targetDevice", "全部设备")
                val action = json.optString("action", "CREATE_OR_UPDATE")
                val myDevice = _config.value.deviceName

                // 忽略本机自己发出的消息，避免环路
                if (sourceDevice.equals(myDevice, ignoreCase = true)) {
                    return@launch
                }

                // 检查目标设备是否为本机或全部设备
                val isForMe = targetDevice == "全部设备" || targetDevice.isBlank() || targetDevice.equals(myDevice, ignoreCase = true)
                if (!isForMe) {
                    return@launch
                }

                val hour = json.getInt("hour")
                val minute = json.getInt("minute")
                val label = json.optString("label", "")
                val isEnabled = json.optBoolean("isEnabled", true)
                val daysBitmask = json.optInt("daysBitmask", 0)
                val isVibrate = json.optBoolean("isVibrate", true)
                val snoozeMinutes = json.optInt("snoozeMinutes", 10)

                val dao = AlarmDatabase.getInstance(context).alarmDao()
                val scheduler = AlarmScheduler(context)

                when (action) {
                    "CREATE_OR_UPDATE" -> {
                        val existingList = dao.getAllAlarmsList()
                        val existing = existingList.find { it.hour == hour && it.minute == minute && it.label == label }
                        val toSave = if (existing != null) {
                            existing.copy(
                                isEnabled = isEnabled,
                                daysBitmask = daysBitmask,
                                isVibrate = isVibrate,
                                snoozeMinutes = snoozeMinutes,
                                targetDevice = "来自 $sourceDevice"
                            )
                        } else {
                            AlarmEntity(
                                hour = hour,
                                minute = minute,
                                label = label,
                                isEnabled = isEnabled,
                                daysBitmask = daysBitmask,
                                isVibrate = isVibrate,
                                snoozeMinutes = snoozeMinutes,
                                targetDevice = "来自 $sourceDevice"
                            )
                        }

                        val savedId = if (existing != null) {
                            dao.updateAlarm(toSave)
                            toSave.id
                        } else {
                            dao.insertAlarm(toSave)
                        }

                        val finalAlarm = toSave.copy(id = savedId)
                        if (finalAlarm.isEnabled) {
                            scheduler.schedule(finalAlarm)
                        } else {
                            scheduler.cancel(finalAlarm.id)
                        }

                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "收到来自 [$sourceDevice] 的闹钟: ${String.format("%02d:%02d", hour, minute)} $label",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    "DELETE" -> {
                        val existingList = dao.getAllAlarmsList()
                        val existing = existingList.find { it.hour == hour && it.minute == minute }
                        if (existing != null) {
                            scheduler.cancel(existing.id)
                            dao.deleteAlarm(existing)
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "[$sourceDevice] 已远程移除闹钟: ${String.format("%02d:%02d", hour, minute)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    "TOGGLE" -> {
                        val existingList = dao.getAllAlarmsList()
                        val existing = existingList.find { it.hour == hour && it.minute == minute }
                        if (existing != null) {
                            val updated = existing.copy(isEnabled = isEnabled)
                            dao.updateAlarm(updated)
                            if (isEnabled) scheduler.schedule(updated) else scheduler.cancel(updated.id)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing incoming MQTT alarm message", e)
            }
        }
    }
}
