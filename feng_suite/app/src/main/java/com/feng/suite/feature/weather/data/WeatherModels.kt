package com.feng.suite.feature.weather.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val province: String = ""
)

enum class ConditionType {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY,
    THUNDER
}

data class WeatherInfo(
    val description: String,
    val icon: ImageVector,
    val type: ConditionType
)

data class HourlyForecast(
    val time: String,
    val temperature: Int,
    val weatherCode: Int
)

data class DailyForecast(
    val dateText: String,
    val minTemp: Int,
    val maxTemp: Int,
    val weatherCode: Int
)

data class WeatherData(
    val cityName: String,
    val currentTemp: Int,
    val apparentTemp: Int,
    val maxTemp: Int,
    val minTemp: Int,
    val weatherCode: Int,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val uvIndex: Double,
    val hourlyList: List<HourlyForecast>,
    val dailyList: List<DailyForecast>,
    val updateTimeText: String,
    val pm25: Double = 25.0
)

object WeatherCodeMapper {
    fun map(code: Int): WeatherInfo {
        return when (code) {
            0 -> WeatherInfo("晴朗", Icons.Outlined.WbSunny, ConditionType.SUNNY)
            1, 2 -> WeatherInfo("多云", Icons.Outlined.WbCloudy, ConditionType.CLOUDY)
            3 -> WeatherInfo("阴天", Icons.Outlined.Cloud, ConditionType.CLOUDY)
            45, 48 -> WeatherInfo("雾", Icons.Outlined.Air, ConditionType.CLOUDY)
            51, 53, 55 -> WeatherInfo("细雨", Icons.Outlined.Grain, ConditionType.RAINY)
            61, 63, 65 -> WeatherInfo("雨", Icons.Outlined.WaterDrop, ConditionType.RAINY)
            71, 73, 75 -> WeatherInfo("雪", Icons.Outlined.Grain, ConditionType.SNOWY)
            80, 81, 82 -> WeatherInfo("阵雨", Icons.Outlined.WaterDrop, ConditionType.RAINY)
            95, 96, 99 -> WeatherInfo("雷暴", Icons.Outlined.Thunderstorm, ConditionType.THUNDER)
            else -> WeatherInfo("晴", Icons.Outlined.WbSunny, ConditionType.SUNNY)
        }
    }
}
