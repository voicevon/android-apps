package com.breezy.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.breezy.weather.data.City
import com.breezy.weather.data.WeatherCache
import com.breezy.weather.data.WeatherData
import com.breezy.weather.data.WeatherService
import com.breezy.weather.work.WeatherUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _currentCity: MutableStateFlow<City>
    val currentCity: StateFlow<City>

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    init {
        // 1. 恢复上一次用户选择的城市（若无则默认为北京）
        val cachedCity = WeatherCache.getCachedCity(context) ?: WeatherService.DEFAULT_CITIES[0]
        _currentCity = MutableStateFlow(cachedCity)
        currentCity = _currentCity.asStateFlow()

        // 2. 检查本地持久化缓存（实现 0ms 秒开）
        val cachedWeather = WeatherCache.getCachedWeather(context)
        if (cachedWeather != null) {
            // 秒开立即可见，无任何等待和转圈
            _weatherState.value = WeatherUiState.Success(cachedWeather)

            // 检查缓存新鲜度（是否在 1 小时内）
            if (!WeatherCache.isCacheFresh(context)) {
                // 缓存超过 1 小时，在后台静默异步刷新，不打扰当前展示
                loadWeather(cachedCity, isSilent = true)
            }
        } else {
            // 首次安装无缓存时，全屏加载
            loadWeather(cachedCity, isSilent = false)
        }

        // 3. 注册 WorkManager 后台每小时自动同步任务
        WeatherUpdateWorker.enqueuePeriodicWork(context)
    }

    fun selectCity(city: City) {
        _currentCity.value = city
        loadWeather(city, isSilent = false)
    }

    fun locateCurrentCity(context: android.content.Context, onPermissionNeeded: () -> Unit) {
        if (!com.breezy.weather.util.LocationHelper.hasLocationPermission(context)) {
            onPermissionNeeded()
            return
        }

        viewModelScope.launch {
            _isLocating.value = true
            try {
                val location = com.breezy.weather.util.LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    val city = WeatherService.reverseGeocode(context, location.latitude, location.longitude)
                    _currentCity.value = city
                    loadWeather(city, isSilent = false)
                } else {
                    refreshWeather()
                }
            } catch (_: Exception) {
            } finally {
                _isLocating.value = false
            }
        }
    }

    fun refreshWeather() {
        loadWeather(_currentCity.value, isManualRefresh = true)
    }

    private fun loadWeather(
        city: City,
        isManualRefresh: Boolean = false,
        isSilent: Boolean = false
    ) {
        viewModelScope.launch {
            if (isManualRefresh) {
                _isRefreshing.value = true
            } else if (!isSilent && _weatherState.value !is WeatherUiState.Success) {
                _weatherState.value = WeatherUiState.Loading
            }

            try {
                val data = WeatherService.fetchWeather(city)
                _weatherState.value = WeatherUiState.Success(data)
                // 持久化缓存最新数据和城市
                WeatherCache.saveWeather(context, city, data)
            } catch (e: Exception) {
                if (_weatherState.value !is WeatherUiState.Success) {
                    val fallback = WeatherService.getFallbackWeatherData(city)
                    _weatherState.value = WeatherUiState.Success(fallback)
                    WeatherCache.saveWeather(context, city, fallback)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
