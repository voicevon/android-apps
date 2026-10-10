package com.fossify.clock.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fossify.clock.alarm.AlarmRinger
import com.fossify.clock.alarm.AlarmScheduler
import com.fossify.clock.data.AlarmDatabase
import com.fossify.clock.ui.screens.RingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: ""
        val hour = intent.getIntExtra(EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_ALARM_MINUTE, 0)
        val isVibrate = intent.getBooleanExtra(EXTRA_IS_VIBRATE, true)
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10)

        when (action) {
            ACTION_ALARM_TRIGGER -> {
                // 1. 播放响铃与振动
                AlarmRinger.start(context, isVibrate)

                // 2. 弹出全屏通知与唤醒界面
                showAlarmNotification(context, alarmId, hour, minute, label, isVibrate, snoozeMinutes)

                // 3. 如果单次闹钟，更新数据库将其设为已关闭
                if (alarmId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AlarmDatabase.getInstance(context)
                        val alarm = db.alarmDao().getAlarmById(alarmId)
                        if (alarm != null && alarm.daysBitmask == 0) {
                            db.alarmDao().updateAlarm(alarm.copy(isEnabled = false))
                        }
                    }
                }
            }

            ACTION_SNOOZE -> {
                // 停止响铃
                AlarmRinger.stop(context)
                cancelNotification(context, alarmId)

                // 调度贪睡
                if (alarmId > 0) {
                    val scheduler = AlarmScheduler(context)
                    scheduler.scheduleSnooze(alarmId, snoozeMinutes, label, isVibrate)
                }
            }

            ACTION_DISMISS -> {
                // 停止响铃并关闭通知
                AlarmRinger.stop(context)
                cancelNotification(context, alarmId)
            }
        }
    }

    private fun showAlarmNotification(
        context: Context,
        alarmId: Long,
        hour: Int,
        minute: Int,
        label: String,
        isVibrate: Boolean,
        snoozeMinutes: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fossify_alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "闹钟提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "精确响铃与全屏闹钟通知"
                enableVibration(isVibrate)
                setSound(null, null) // 已通过 AlarmRinger 独立播放
            }
            notificationManager.createNotificationChannel(channel)
        }

        val timeFormatted = String.format("%02d:%02d", hour, minute)
        val titleText = if (label.isNotBlank()) label else "闹钟响铃"

        // 全屏 Intent
        val fullScreenIntent = Intent(context, RingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_HOUR, hour)
            putExtra(EXTRA_ALARM_MINUTE, minute)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_IS_VIBRATE, isVibrate)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 贪睡 Action
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_SNOOZE
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_IS_VIBRATE, isVibrate)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 50000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 关闭 Action
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_DISMISS
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 60000).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titleText)
            .setContentText("$timeFormatted - 点击或滑动处理")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "贪睡 ($snoozeMinutes 分钟)", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "关闭", dismissPendingIntent)
            .build()

        notificationManager.notify(alarmId.toInt(), notification)
    }

    private fun cancelNotification(context: Context, alarmId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId.toInt())
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.fossify.clock.ACTION_ALARM_TRIGGER"
        const val ACTION_SNOOZE = "com.fossify.clock.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.fossify.clock.ACTION_DISMISS"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_IS_VIBRATE = "extra_is_vibrate"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }
}
