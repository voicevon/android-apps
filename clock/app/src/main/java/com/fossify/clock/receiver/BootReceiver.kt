package com.fossify.clock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fossify.clock.alarm.AlarmScheduler
import com.fossify.clock.data.AlarmDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val scheduler = AlarmScheduler(context)
            val db = AlarmDatabase.getInstance(context)

            CoroutineScope(Dispatchers.IO).launch {
                val enabledAlarms = db.alarmDao().getEnabledAlarms()
                for (alarm in enabledAlarms) {
                    scheduler.schedule(alarm)
                }
            }
        }
    }
}
