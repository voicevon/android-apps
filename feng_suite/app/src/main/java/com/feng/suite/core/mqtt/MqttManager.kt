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
import com.feng.suite.feature.chat.data.ChatDatabase
import com.feng.suite.feature.chat.data.ChatMessageEntity
import com.feng.suite.feature.chat.data.MessageDeliveryStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val serverHost: String = "voicevon.vicp.io",
    val serverPort: Int = 1883,
    val deviceName: String = "设备-${Build.MODEL.takeLast(4)}",
    val isEnabled: Boolean = true
)

data class MqttLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val topic: String,
    val sender: String,
    val target: String = "全部设备",
    val msgType: String, // HEARTBEAT, CHAT, ALARM, OTHER
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = false
)

object MqttManager {

    private const val TAG = "MqttManager"
    private const val PREFS_NAME = "feng_mqtt_prefs"
    private const val KEY_HOST = "mqtt_host"
    private const val KEY_PORT = "mqtt_port"
    private const val KEY_DEVICE_NAME = "mqtt_device_name"
    private const val KEY_ENABLED = "mqtt_enabled"
    private const val KEY_LOGS_JSON = "mqtt_logs_json"

    private const val TOPIC_BROADCAST = "feng_suite/alarm/broadcast"
    private const val TOPIC_DEVICE_PREFIX = "feng_suite/alarm/device/"
    private const val TOPIC_CHAT_BROADCAST = "feng_suite/chat/broadcast"
    private const val TOPIC_CHAT_DEVICE_PREFIX = "feng_suite/chat/device/"
    private const val TOPIC_HEARTBEAT = "feng_suite/heartbeat"

    private var appContext: Context? = null
    private var mqttClient: MqttClient? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var heartbeatJob: kotlinx.coroutines.Job? = null

    private val _connectionStatus = MutableStateFlow(MqttConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<MqttConnectionStatus> = _connectionStatus.asStateFlow()

    private val _config = MutableStateFlow(MqttConfig())
    val config: StateFlow<MqttConfig> = _config.asStateFlow()

    private val _logs = MutableStateFlow<List<MqttLogEntry>>(emptyList())
    val logs: StateFlow<List<MqttLogEntry>> = _logs.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        loadConfig(context)
        loadLogs(context)

        if (_config.value.isEnabled) {
            connect()
        }
    }

    fun addLog(entry: MqttLogEntry) {
        scope.launch {
            val current = _logs.value.toMutableList()
            current.add(0, entry)
            if (current.size > 150) {
                current.removeAt(current.size - 1)
            }
            _logs.value = current
            saveLogsToPrefs(current)
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
        saveLogsToPrefs(emptyList())
    }

    private fun loadLogs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_LOGS_JSON, null) ?: return
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<MqttLogEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MqttLogEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        topic = obj.optString("topic", ""),
                        sender = obj.optString("sender", ""),
                        target = obj.optString("target", "全部设备"),
                        msgType = obj.optString("msgType", "OTHER"),
                        payload = obj.optString("payload", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isOutgoing = obj.optBoolean("isOutgoing", false)
                    )
                )
            }
            _logs.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load mqtt logs", e)
        }
    }

    private fun saveLogsToPrefs(list: List<MqttLogEntry>) {
        val context = appContext ?: return
        try {
            val array = org.json.JSONArray()
            list.take(100).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("topic", item.topic)
                    put("sender", item.sender)
                    put("target", item.target)
                    put("msgType", item.msgType)
                    put("payload", item.payload)
                    put("timestamp", item.timestamp)
                    put("isOutgoing", item.isOutgoing)
                }
                array.put(obj)
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_LOGS_JSON, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save mqtt logs", e)
        }
    }

    private fun loadConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultDeviceName = "设备-${Build.MODEL.takeLast(4).ifEmpty { "A" }}"
        var host = prefs.getString(KEY_HOST, "voicevon.vicp.io") ?: "voicevon.vicp.io"
        if (host == "broker.emqx.io") {
            host = "voicevon.vicp.io"
        }
        val port = prefs.getInt(KEY_PORT, 1883)
        val deviceName = prefs.getString(KEY_DEVICE_NAME, defaultDeviceName) ?: defaultDeviceName
        val isEnabled = if (prefs.contains(KEY_ENABLED)) prefs.getBoolean(KEY_ENABLED, true) else true

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
                        startHeartbeatLoop()
                        resendPendingChatMessages()
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.w(TAG, "MQTT Connection lost", cause)
                        _connectionStatus.value = MqttConnectionStatus.DISCONNECTED
                        heartbeatJob?.cancel()
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
        heartbeatJob?.cancel()
        _connectionStatus.value = MqttConnectionStatus.DISCONNECTED
    }

    private fun subscribeTopics(client: MqttClient, deviceName: String) {
        try {
            // 1. 订阅公共闹钟广播与设备专属闹钟
            client.subscribe(TOPIC_BROADCAST, 1)
            val directAlarmTopic = "$TOPIC_DEVICE_PREFIX$deviceName"
            client.subscribe(directAlarmTopic, 1)

            // 2. 订阅公共即时消息广播与设备专属消息
            client.subscribe(TOPIC_CHAT_BROADCAST, 1)
            val directChatTopic = "$TOPIC_CHAT_DEVICE_PREFIX$deviceName"
            client.subscribe(directChatTopic, 1)

            // 3. 订阅所有设备在线心跳广播
            client.subscribe(TOPIC_HEARTBEAT, 1)

            Log.d(TAG, "Subscribed to topics: $TOPIC_BROADCAST, $directAlarmTopic, $TOPIC_CHAT_BROADCAST, $directChatTopic, $TOPIC_HEARTBEAT")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe topics", e)
        }
    }

    suspend fun publishChatMessage(chatMessage: ChatMessageEntity): Boolean = withContext(Dispatchers.IO) {
        if (!_config.value.isEnabled) return@withContext false
        val client = mqttClient ?: return@withContext false
        if (!client.isConnected) return@withContext false

        return@withContext try {
            val json = JSONObject().apply {
                put("msgType", "CHAT")
                put("id", chatMessage.id)
                put("sender", chatMessage.sender)
                put("target", chatMessage.target)
                put("content", chatMessage.content)
                put("timestamp", chatMessage.timestamp)
            }
            val payload = json.toString().toByteArray(Charsets.UTF_8)
            val message = MqttMessage(payload).apply { qos = 1 }

            val targetTopic = if (chatMessage.target != "全部设备" && chatMessage.target.isNotBlank()) {
                "$TOPIC_CHAT_DEVICE_PREFIX${chatMessage.target}"
            } else {
                TOPIC_CHAT_BROADCAST
            }
            client.publish(targetTopic, message)
            Log.d(TAG, "Published chat message: $json")

            addLog(
                MqttLogEntry(
                    topic = targetTopic,
                    sender = chatMessage.sender,
                    target = chatMessage.target,
                    msgType = "CHAT",
                    payload = json.toString(),
                    timestamp = chatMessage.timestamp,
                    isOutgoing = true
                )
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish chat message", e)
            false
        }
    }

    fun resendPendingChatMessages() {
        val context = appContext ?: return
        scope.launch {
            try {
                val dao = ChatDatabase.getInstance(context).chatMessageDao()
                val pendingList = dao.getUnsentMessages()
                if (pendingList.isEmpty()) return@launch

                Log.d(TAG, "Found ${pendingList.size} unsent chat messages, starting auto-resend...")
                for (msg in pendingList) {
                    val success = publishChatMessage(msg)
                    if (success) {
                        dao.updateDeliveryStatus(msg.id, MessageDeliveryStatus.SENT)
                        Log.d(TAG, "Auto-resend succeeded for msg id=${msg.id}")
                    } else {
                        dao.updateDeliveryStatus(msg.id, MessageDeliveryStatus.FAILED)
                        Log.w(TAG, "Auto-resend failed for msg id=${msg.id}, aborting remaining batch")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in resendPendingChatMessages", e)
            }
        }
    }

    fun publishHeartbeat() {
        if (!_config.value.isEnabled) return
        val client = mqttClient ?: return
        if (!client.isConnected) return

        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("msgType", "HEARTBEAT")
                    put("device", _config.value.deviceName)
                    put("timestamp", System.currentTimeMillis())
                    put("status", "ONLINE")
                }
                val payload = json.toString().toByteArray(Charsets.UTF_8)
                val message = MqttMessage(payload).apply { qos = 1 }
                client.publish(TOPIC_HEARTBEAT, message)
                Log.d(TAG, "Published heartbeat: $json")

                addLog(
                    MqttLogEntry(
                        topic = TOPIC_HEARTBEAT,
                        sender = _config.value.deviceName,
                        target = "全部设备",
                        msgType = "HEARTBEAT",
                        payload = json.toString(),
                        timestamp = System.currentTimeMillis(),
                        isOutgoing = true
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to publish heartbeat", e)
            }
        }
    }

    private fun startHeartbeatLoop() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                publishHeartbeat()
                delay(3600_000L) // 每 1 小时发送一次心跳
            }
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

                val targetTopic = if (target != "全部设备" && target != "本机") {
                    "$TOPIC_DEVICE_PREFIX$target"
                } else {
                    TOPIC_BROADCAST
                }
                client.publish(targetTopic, message)
                Log.d(TAG, "Published alarm to target=$target: $json")

                addLog(
                    MqttLogEntry(
                        topic = targetTopic,
                        sender = myDevice,
                        target = target,
                        msgType = "ALARM",
                        payload = json.toString(),
                        timestamp = System.currentTimeMillis(),
                        isOutgoing = true
                    )
                )
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

                val msgType = json.optString("msgType", "")
                val myDevice = _config.value.deviceName

                // 0. 判断是否为设备在线心跳 (HEARTBEAT)
                val isHeartbeatTopic = topic?.startsWith(TOPIC_HEARTBEAT) == true
                if (msgType == "HEARTBEAT" || isHeartbeatTopic) {
                    val device = json.optString("device", json.optString("deviceName", "未知设备"))
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    Log.d(TAG, "Heartbeat received from $device ($timestamp)")

                    addLog(
                        MqttLogEntry(
                            topic = topic ?: TOPIC_HEARTBEAT,
                            sender = device,
                            target = "全部设备",
                            msgType = "HEARTBEAT",
                            payload = bodyStr,
                            timestamp = timestamp,
                            isOutgoing = device.equals(myDevice, ignoreCase = true)
                        )
                    )
                    return@launch
                }

                // 1. 判断是否为即时聊天消息 (CHAT)
                val isChatTopic = topic?.startsWith("feng_suite/chat") == true
                if (msgType == "CHAT" || isChatTopic) {
                    val sender = json.optString("sender", "")
                    val target = json.optString("target", "全部设备")
                    val content = json.optString("content", "")
                    val id = json.optString("id", UUID.randomUUID().toString())
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())

                    addLog(
                        MqttLogEntry(
                            topic = topic ?: TOPIC_CHAT_BROADCAST,
                            sender = sender,
                            target = target,
                            msgType = "CHAT",
                            payload = bodyStr,
                            timestamp = timestamp,
                            isOutgoing = sender.equals(myDevice, ignoreCase = true)
                        )
                    )

                    // 过滤本机自己发出的消息，避免回环
                    if (sender.equals(myDevice, ignoreCase = true)) {
                        return@launch
                    }

                    // 检查是否发送给本机或属于全部设备广播
                    val isForMe = target == "全部设备" || target.isBlank() || target.equals(myDevice, ignoreCase = true)
                    if (!isForMe) return@launch

                    val entity = ChatMessageEntity(
                        id = id,
                        sender = sender,
                        target = target,
                        content = content,
                        timestamp = timestamp,
                        isFromMe = false
                    )
                    ChatDatabase.getInstance(context).chatMessageDao().insertMessage(entity)

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "[$sender]: $content",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // 2. 闹钟处理分支
                val sourceDevice = json.optString("sourceDevice", "")
                val targetDevice = json.optString("targetDevice", "全部设备")
                val action = json.optString("action", "CREATE_OR_UPDATE")

                addLog(
                    MqttLogEntry(
                        topic = topic ?: TOPIC_BROADCAST,
                        sender = sourceDevice,
                        target = targetDevice,
                        msgType = "ALARM",
                        payload = bodyStr,
                        timestamp = System.currentTimeMillis(),
                        isOutgoing = sourceDevice.equals(myDevice, ignoreCase = true)
                    )
                )

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
