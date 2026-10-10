package com.breezy.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.breezy.weather.ui.screens.WeatherScreen
import com.breezy.weather.ui.theme.BreezyWeatherTheme
import com.breezy.weather.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BreezyWeatherTheme {
                WeatherScreen(viewModel = weatherViewModel)
            }
        }
    }
}
