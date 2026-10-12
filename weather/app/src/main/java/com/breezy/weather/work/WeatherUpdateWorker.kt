package com.breezy.weather.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.breezy.weather.data.WeatherCache
import com.breezy.weather.data.WeatherService
import java.util.concurrent.TimeUnit

class WeatherUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val city = WeatherCache.getCachedCity(applicationContext) ?: WeatherService.DEFAULT_CITIES[0]
            val weatherData = WeatherService.fetchWeather(city)
            WeatherCache.saveWeather(applicationContext, city, weatherData)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "breezy_hourly_weather_sync"

        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // 每 1 小时自动后台定期拉取最新天气，确保用户打开时数据永远是新鲜的
            val syncRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
