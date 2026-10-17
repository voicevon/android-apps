package com.feng.suite.feature.clock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.feng.suite.feature.clock.alarm.AlarmRinger
import com.feng.suite.feature.clock.alarm.AlarmScheduler
import com.feng.suite.feature.clock.data.AlarmDatabase
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
                // 1. 瞬间获取临时 PARTIAL_WAKE_LOCK，衔接 Service 启动空隙，防止 CPU 闪退休眠
                try {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                    val wakeLock = pm?.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "FengSuite:AlarmReceiverBridge"
                    )
                    wakeLock?.acquire(10 * 1000L) // 10秒足够服务接管
                } catch (_: Exception) {
                }

                // 2. 启动前台响铃服务（AlarmRinger Foreground Service 挂载通知与唤醒锁）
                AlarmRinger.start(
                    context = context,
                    alarmId = alarmId,
                    hour = hour,
                    minute = minute,
                    label = label,
                    isVibrate = isVibrate,
                    snoozeMinutes = snoozeMinutes
                )

                // 3. 重复闹钟立即原子排期下一次！单次闹钟设为关闭。使用 goAsync() 严防进程提前被强杀
                if (alarmId > 0) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AlarmDatabase.getInstance(context)
                            val alarm = db.alarmDao().getAlarmById(alarmId)
                            if (alarm != null) {
                                if (alarm.daysBitmask == 0) {
                                    db.alarmDao().updateAlarm(alarm.copy(isEnabled = false))
                                } else {
                                    val scheduler = AlarmScheduler(context)
                                    scheduler.schedule(alarm)
                                }
                            }
                        } catch (_: Exception) {
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_SNOOZE -> {
                AlarmRinger.stop(context)
                if (alarmId > 0) {
                    val scheduler = AlarmScheduler(context)
                    scheduler.scheduleSnooze(alarmId, snoozeMinutes, label, isVibrate)
                }
            }

            ACTION_DISMISS -> {
                AlarmRinger.stop(context)
            }
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.feng.suite.feature.clock.ACTION_ALARM_TRIGGER"
        const val ACTION_SNOOZE = "com.feng.suite.feature.clock.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.feng.suite.feature.clock.ACTION_DISMISS"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_IS_VIBRATE = "extra_is_vibrate"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }
}
