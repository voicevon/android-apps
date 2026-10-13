package com.feng.suite

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.feng.suite.core.mqtt.HeartbeatWorker
import com.feng.suite.core.mqtt.MqttManager
import com.feng.suite.feature.weather.work.WeatherUpdateWorker

class FengApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // 注册天气每小时后台同步
        WeatherUpdateWorker.enqueuePeriodicWork(this)
        // 注册设备每小时 MQTT 在线心跳包
        HeartbeatWorker.enqueuePeriodicWork(this)
        // 初始化 MQTT 跨设备同步
        MqttManager.init(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // 闹钟专属最高优先级通道
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "冯氏闹钟响铃",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "闹钟到期提醒与全屏响铃通知"
                setBypassDnd(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(alarmChannel)

            // 天气更新常规通知渠道
            val weatherChannel = NotificationChannel(
                WEATHER_CHANNEL_ID,
                "天气动态",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台天气同步状态"
            }
            notificationManager.createNotificationChannel(weatherChannel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "feng_alarm_channel"
        const val WEATHER_CHANNEL_ID = "feng_weather_channel"
    }
}
