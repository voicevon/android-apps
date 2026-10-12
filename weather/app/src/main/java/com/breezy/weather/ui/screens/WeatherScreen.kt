package com.breezy.weather.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breezy.weather.data.ConditionType
import com.breezy.weather.data.WeatherCodeMapper
import com.breezy.weather.data.WeatherData
import com.breezy.weather.ui.components.CityDialog
import com.breezy.weather.ui.components.CurrentWeatherSection
import com.breezy.weather.ui.components.DailyForecastCard
import com.breezy.weather.ui.components.HourlyForecastCard
import com.breezy.weather.ui.components.MetricsGridCard
import com.breezy.weather.ui.theme.CloudyGradient
import com.breezy.weather.ui.theme.NightGradient
import com.breezy.weather.ui.theme.RainyGradient
import com.breezy.weather.ui.theme.SunnyGradient
import com.breezy.weather.ui.theme.TextWhitePrimary
import com.breezy.weather.ui.theme.TextWhiteSecondary
import com.breezy.weather.ui.viewmodel.WeatherUiState
import com.breezy.weather.ui.viewmodel.WeatherViewModel
import java.util.Calendar

import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.ui.platform.LocalContext

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCity by viewModel.currentCity.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }

    // 动态根据天气与时间选择沉浸渐变背景
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isNight = currentHour < 6 || currentHour >= 19

    val gradientBrush = when {
        isNight -> NightGradient
        weatherState is WeatherUiState.Success -> {
            val code = (weatherState as WeatherUiState.Success).data.weatherCode
            val type = WeatherCodeMapper.map(code).type
            when (type) {
                ConditionType.SUNNY -> SunnyGradient
                ConditionType.CLOUDY -> CloudyGradient
                ConditionType.RAINY, ConditionType.SNOWY, ConditionType.THUNDER -> RainyGradient
            }
        }
        else -> SunnyGradient
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏：城市切换胶囊 + GPS 定位按钮 + 刷新按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 城市选择胶囊
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { showCityDialog = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = TextWhitePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = currentCity.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = "切换城市",
                        tint = TextWhiteSecondary
                    )
                }

                // 右侧操作栏：GPS 定位按钮 + 刷新按钮
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 1. 独立 GPS 定位按钮
                    IconButton(
                        onClick = { viewModel.locateCurrentCity(context, onRequestLocationPermission) }
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                color = TextWhitePrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.MyLocation,
                                contentDescription = "GPS 定位当前城市",
                                tint = TextWhitePrimary
                            )
                        }
                    }

                    // 2. 刷新按钮
                    IconButton(
                        onClick = { viewModel.refreshWeather() }
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = TextWhitePrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "刷新天气",
                                tint = TextWhitePrimary
                            )
                        }
                    }
                }
            }

            // 内容区域
            Crossfade(targetState = weatherState, label = "WeatherStateCrossfade") { state ->
                when (state) {
                    is WeatherUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TextWhitePrimary)
                        }
                    }
                    is WeatherUiState.Success -> {
                        WeatherContent(data = state.data)
                    }
                    is WeatherUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "加载失败，请点击右上角重试",
                                color = TextWhitePrimary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCityDialog) {
        CityDialog(
            currentCity = currentCity,
            onDismiss = { showCityDialog = false },
            onSelectCity = { city ->
                viewModel.selectCity(city)
            },
            onLocateClick = {
                viewModel.locateCurrentCity(context, onRequestLocationPermission)
            },
            isLocating = isLocating
        )
    }
}

@Composable
private fun WeatherContent(data: WeatherData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 1. 首屏核心温度看板
        CurrentWeatherSection(data = data)

        // 2. 24 小时逐小时横滑卡片
        HourlyForecastCard(hourlyList = data.hourlyList)

        Spacer(modifier = Modifier.height(14.dp))

        // 3. 未来 7 天预报卡片
        DailyForecastCard(dailyList = data.dailyList)

        Spacer(modifier = Modifier.height(14.dp))

        // 4. 核心气象指标网格 (湿度/风速/紫外线/气压)
        MetricsGridCard(data = data)

        Spacer(modifier = Modifier.height(32.dp))
    }
}
