package com.feng.suite.feature.weather.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object WeatherCache {

    private const val PREFS_NAME = "feng_weather_cache"
    private const val KEY_CITY = "cached_city"
    private const val KEY_WEATHER = "cached_weather"
    private const val KEY_LAST_UPDATE_TIME = "last_update_time_millis"

    const val CACHE_FRESH_DURATION_MILLIS = 60 * 60 * 1000L

    fun saveWeather(context: Context, city: City, weather: WeatherData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CITY, serializeCity(city).toString())
            .putString(KEY_WEATHER, serializeWeather(weather).toString())
            .putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getCachedCity(context: Context): City? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cityStr = prefs.getString(KEY_CITY, null) ?: return null
        return try {
            deserializeCity(JSONObject(cityStr))
        } catch (_: Exception) {
            null
        }
    }

    fun getCachedWeather(context: Context): WeatherData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val weatherStr = prefs.getString(KEY_WEATHER, null) ?: return null
        return try {
            deserializeWeather(JSONObject(weatherStr))
        } catch (_: Exception) {
            null
        }
    }

    fun getLastUpdateTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_UPDATE_TIME, 0L)
    }

    private const val KEY_LOCAL_CITY = "local_city"
    private const val KEY_CUSTOM_CITIES = "custom_weather_cities"

    val DEFAULT_CUSTOM_CITIES = listOf(
        City("郑州", 34.7466, 113.6253, "河南省"),
        City("开封", 34.7972, 114.3075, "河南省")
    )

    fun saveLocalCity(context: Context, city: City) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LOCAL_CITY, serializeCity(city).toString()).apply()
    }

    fun getLocalCity(context: Context): City {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cityStr = prefs.getString(KEY_LOCAL_CITY, null)
        if (cityStr != null) {
            try {
                return deserializeCity(JSONObject(cityStr))
            } catch (_: Exception) {}
        }
        return getCachedCity(context) ?: City("济南", 36.6512, 117.1201, "山东省")
    }

    fun saveCustomCities(context: Context, cities: List<City>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        cities.forEach { array.put(serializeCity(it)) }
        prefs.edit().putString(KEY_CUSTOM_CITIES, array.toString()).apply()
    }

    fun getCustomCities(context: Context): List<City> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val str = prefs.getString(KEY_CUSTOM_CITIES, null) ?: return DEFAULT_CUSTOM_CITIES
        return try {
            val array = JSONArray(str)
            val list = mutableListOf<City>()
            for (i in 0 until array.length()) {
                list.add(deserializeCity(array.getJSONObject(i)))
            }
            if (list.isEmpty()) DEFAULT_CUSTOM_CITIES else list
        } catch (_: Exception) {
            DEFAULT_CUSTOM_CITIES
        }
    }

    fun isCacheFresh(context: Context): Boolean {
        val lastTime = getLastUpdateTime(context)
        if (lastTime <= 0L) return false
        val diff = System.currentTimeMillis() - lastTime
        return diff in 0 until CACHE_FRESH_DURATION_MILLIS
    }

    private fun serializeCity(city: City): JSONObject {
        return JSONObject().apply {
            put("name", city.name)
            put("latitude", city.latitude)
            put("longitude", city.longitude)
            put("province", city.province)
        }
    }

    private fun deserializeCity(json: JSONObject): City {
        return City(
            name = json.getString("name"),
            latitude = json.getDouble("latitude"),
            longitude = json.getDouble("longitude"),
            province = json.optString("province", "")
        )
    }

    private fun serializeWeather(weather: WeatherData): JSONObject {
        val json = JSONObject()
        json.put("cityName", weather.cityName)
        json.put("currentTemp", weather.currentTemp)
        json.put("apparentTemp", weather.apparentTemp)
        json.put("maxTemp", weather.maxTemp)
        json.put("minTemp", weather.minTemp)
        json.put("weatherCode", weather.weatherCode)
        json.put("humidity", weather.humidity)
        json.put("windSpeed", weather.windSpeed)
        json.put("pressure", weather.pressure)
        json.put("uvIndex", weather.uvIndex)
        json.put("updateTimeText", weather.updateTimeText)
        json.put("pm25", weather.pm25)

        val hourlyArray = JSONArray()
        weather.hourlyList.forEach { h ->
            val hObj = JSONObject()
            hObj.put("time", h.time)
            hObj.put("temp", h.temperature)
            hObj.put("code", h.weatherCode)
            hourlyArray.put(hObj)
        }
        json.put("hourly", hourlyArray)

        val dailyArray = JSONArray()
        weather.dailyList.forEach { d ->
            val dObj = JSONObject()
            dObj.put("date", d.dateText)
            dObj.put("min", d.minTemp)
            dObj.put("max", d.maxTemp)
            dObj.put("code", d.weatherCode)
            dailyArray.put(dObj)
        }
        json.put("daily", dailyArray)

        return json
    }

    private fun deserializeWeather(json: JSONObject): WeatherData {
        val hourlyList = mutableListOf<HourlyForecast>()
        val hourlyArray = json.optJSONArray("hourly")
        if (hourlyArray != null) {
            for (i in 0 until hourlyArray.length()) {
                val h = hourlyArray.getJSONObject(i)
                hourlyList.add(
                    HourlyForecast(
                        time = h.getString("time"),
                        temperature = h.getInt("temp"),
                        weatherCode = h.getInt("code")
                    )
                )
            }
        }

        val dailyList = mutableListOf<DailyForecast>()
        val dailyArray = json.optJSONArray("daily")
        if (dailyArray != null) {
            for (i in 0 until dailyArray.length()) {
                val d = dailyArray.getJSONObject(i)
                dailyList.add(
                    DailyForecast(
                        dateText = d.getString("date"),
                        minTemp = d.getInt("min"),
                        maxTemp = d.getInt("max"),
                        weatherCode = d.getInt("code")
                    )
                )
            }
        }

        return WeatherData(
            cityName = json.getString("cityName"),
            currentTemp = json.getInt("currentTemp"),
            apparentTemp = json.getInt("apparentTemp"),
            maxTemp = json.getInt("maxTemp"),
            minTemp = json.getInt("minTemp"),
            weatherCode = json.getInt("weatherCode"),
            humidity = json.getInt("humidity"),
            windSpeed = json.getDouble("windSpeed"),
            pressure = json.getDouble("pressure"),
            uvIndex = json.getDouble("uvIndex"),
            hourlyList = hourlyList,
            dailyList = dailyList,
            updateTimeText = json.optString("updateTimeText", "刚刚更新"),
            pm25 = json.optDouble("pm25", 25.0)
        )
    }
}
