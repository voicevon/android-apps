package com.feng.suite.feature.weather.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission(context)) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        // 1. 优先获取最后已知位置（最快，0毫秒延迟）
        val lastGps = runCatching { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
        val lastNetwork = runCatching { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
        val lastPassive = runCatching { locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()

        val bestLast = listOfNotNull(lastGps, lastNetwork, lastPassive)
            .maxByOrNull { it.time }

        // 如果在 30 分钟以内，直接复用快速返回
        if (bestLast != null && System.currentTimeMillis() - bestLast.time < 30 * 60 * 1000L) {
            continuation.resume(bestLast)
            return@suspendCancellableCoroutine
        }

        // 2. 否则请求单次单步更新
        var resumed = false
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!resumed) {
                    resumed = true
                    runCatching { locationManager.removeUpdates(this) }
                    continuation.resume(location)
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        continuation.invokeOnCancellation {
            runCatching { locationManager.removeUpdates(listener) }
        }

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> null
        }

        if (provider != null) {
            try {
                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            } catch (_: Exception) {
                if (!resumed) {
                    resumed = true
                    continuation.resume(bestLast)
                }
            }
        } else {
            continuation.resume(bestLast)
        }
    }
}
