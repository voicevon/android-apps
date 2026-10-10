package com.breezy.weather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breezy.weather.data.City
import com.breezy.weather.data.WeatherData
import com.breezy.weather.data.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel : ViewModel() {

    private val _currentCity = MutableStateFlow(WeatherService.DEFAULT_CITIES[0])
    val currentCity: StateFlow<City> = _currentCity.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadWeather(_currentCity.value)
    }

    fun selectCity(city: City) {
        _currentCity.value = city
        loadWeather(city)
    }

    fun refreshWeather() {
        loadWeather(_currentCity.value, isManualRefresh = true)
    }

    private fun loadWeather(city: City, isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isManualRefresh) {
                _isRefreshing.value = true
            } else if (_weatherState.value !is WeatherUiState.Success) {
                _weatherState.value = WeatherUiState.Loading
            }

            try {
                val data = WeatherService.fetchWeather(city)
                _weatherState.value = WeatherUiState.Success(data)
            } catch (e: Exception) {
                if (_weatherState.value !is WeatherUiState.Success) {
                    _weatherState.value = WeatherUiState.Success(WeatherService.getFallbackWeatherData(city))
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
