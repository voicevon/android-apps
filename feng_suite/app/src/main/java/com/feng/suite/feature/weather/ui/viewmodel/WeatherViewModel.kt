package com.feng.suite.feature.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feng.suite.feature.weather.data.City
import com.feng.suite.feature.weather.data.WeatherCache
import com.feng.suite.feature.weather.data.WeatherData
import com.feng.suite.feature.weather.data.WeatherService
import com.feng.suite.feature.weather.work.WeatherUpdateWorker
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
        val cachedCity = WeatherCache.getCachedCity(context) ?: WeatherService.DEFAULT_CITIES[0]
        _currentCity = MutableStateFlow(cachedCity)
        currentCity = _currentCity.asStateFlow()

        val cachedWeather = WeatherCache.getCachedWeather(context)
        if (cachedWeather != null) {
            _weatherState.value = WeatherUiState.Success(cachedWeather)
            if (!WeatherCache.isCacheFresh(context)) {
                loadWeather(cachedCity, isSilent = true)
            }
        } else {
            loadWeather(cachedCity, isSilent = false)
        }

        WeatherUpdateWorker.enqueuePeriodicWork(context)
    }

    fun selectCity(city: City) {
        _currentCity.value = city
        loadWeather(city, isSilent = false)
    }

    fun locateCurrentCity(context: android.content.Context, onPermissionNeeded: () -> Unit) {
        if (!com.feng.suite.feature.weather.util.LocationHelper.hasLocationPermission(context)) {
            onPermissionNeeded()
            return
        }

        viewModelScope.launch {
            _isLocating.value = true
            try {
                val location = com.feng.suite.feature.weather.util.LocationHelper.getCurrentLocation(context)
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
