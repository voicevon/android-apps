package com.feng.suite.core.mqtt

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class HeartbeatWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        MqttManager.publishHeartbeat()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "feng_mqtt_heartbeat_worker"

        fun enqueuePeriodicWork(context: Context) {
            val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
