package com.feng.suite.feature.clock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.feng.suite.feature.clock.data.AlarmEntity
import com.feng.suite.feature.clock.receiver.AlarmReceiver

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancel(alarm.id)
            return
        }

        // 检查 Android 12+ 精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // 如果未获精确闹钟权限，回退为非精确调度或提醒
            }
        }

        val triggerTime = alarm.calculateNextTriggerTime()
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_IS_VIBRATE, alarm.isVibrate)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, alarm.snoozeMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构造点击系统状态栏/锁屏时钟图标时的跳转 Intent
        val showIntent = Intent(context, com.feng.suite.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 使用 Android 官方最高优先级 setAlarmClock：豁免 Doze 深度休眠，常驻系统锁屏与状态栏闹钟图标
        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: SecurityException) {
            // Android 12+ 极端未授权场景回退
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun scheduleSnooze(alarmId: Long, minutes: Int, label: String, isVibrate: Boolean) {
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, if (label.isNotBlank()) "$label (贪睡)" else "贪睡闹钟")
            putExtra(AlarmReceiver.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, minutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, com.feng.suite.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            (alarmId + 100000).toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancel(alarm: AlarmEntity) = cancel(alarm.id)

    fun cancel(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        /**
         * 检查应用是否已加入电池优化白名单 (豁免 Doze Mode 深度休眠)
         */
        fun isIgnoringBatteryOptimizations(context: Context): Boolean {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        }

        /**
         * 请求加入电池优化白名单以保证深度休眠下 100% 唤醒
         */
        fun requestIgnoreBatteryOptimizations(context: Context) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val fallbackIntent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (_: Exception) {
                }
            }
        }

        /**
         * 计算并生成类似 Fossify Clock 顶部的下一次闹钟倒计时文本
         */
        fun formatNextAlarmCountdown(alarms: List<AlarmEntity>): String? {
            val enabledAlarms = alarms.filter { it.isEnabled }
            if (enabledAlarms.isEmpty()) return null

            val now = System.currentTimeMillis()
            val nextTime = enabledAlarms.minOfOrNull { it.calculateNextTriggerTime(now) } ?: return null
            val diffMillis = nextTime - now
            if (diffMillis <= 0) return null

            val totalMinutes = diffMillis / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val days = hours / 24
            val remainingHours = hours % 24

            return buildString {
                append("下个闹钟将在 ")
                if (days > 0) append("${days}天 ")
                if (remainingHours > 0 || days > 0) append("${remainingHours}小时 ")
                append("${minutes}分钟后响铃")
            }
        }
    }
}
