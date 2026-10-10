package com.fossify.clock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fossify.clock.data.AlarmEntity
import com.fossify.clock.receiver.AlarmReceiver

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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

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
