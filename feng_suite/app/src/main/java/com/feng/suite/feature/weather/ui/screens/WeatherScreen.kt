package com.feng.suite.feature.weather.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.feature.weather.data.ConditionType
import com.feng.suite.feature.weather.data.WeatherCodeMapper
import com.feng.suite.feature.weather.data.WeatherData
import com.feng.suite.feature.weather.ui.components.CityDialog
import com.feng.suite.feature.weather.ui.components.CurrentWeatherSection
import com.feng.suite.feature.weather.ui.components.DailyForecastCard
import com.feng.suite.feature.weather.ui.components.HourlyForecastCard
import com.feng.suite.feature.weather.ui.viewmodel.WeatherUiState
import com.feng.suite.feature.weather.ui.viewmodel.WeatherViewModel
import com.feng.suite.ui.dashboard.getWorldClockCity
import com.feng.suite.ui.theme.CloudyGradient
import com.feng.suite.ui.theme.NightGradient
import com.feng.suite.ui.theme.RainyGradient
import com.feng.suite.ui.theme.SunnyGradient
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary
import java.util.Calendar

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequestLocationPermission: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCity by viewModel.currentCity.collectAsState()
    val localCity by viewModel.localCity.collectAsState()
    val customCities by viewModel.customCities.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()

    var showCityDropdown by remember { mutableStateOf(false) }
    var showCityDialog by remember { mutableStateOf(false) }

    // 每次打开下拉菜单时，重新读取与时间设置绑定的两个外地时区城市
    val zone1City = remember(showCityDropdown) { getWorldClockCity(context, 1) }
    val zone2City = remember(showCityDropdown) { getWorldClockCity(context, 2) }

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
            // 顶栏：返回按钮 + 城市切换胶囊与下拉菜单 + GPS 定位按钮 + 刷新按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：返回键 + 城市选择胶囊
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = TextWhitePrimary
                            )
                        }
                    }

                    // 城市选择胶囊与两大类城市下拉菜单
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { showCityDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = TextWhitePrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = currentCity.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = null,
                                tint = TextWhitePrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showCityDropdown,
                            onDismissRequest = { showCityDropdown = false },
                            modifier = Modifier
                                .widthIn(min = 230.dp, max = 280.dp)
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        ) {
                            // 第一大类：固定 3 个城市（与时间/时区设置联动）
                            Text(
                                text = "固定时区城市",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF93C5FD),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )

                            // 1. 本地城市
                            DropdownCityItem(
                                cityName = localCity.name,
                                tag = "本地",
                                icon = Icons.Outlined.MyLocation,
                                isSelected = currentCity.name == localCity.name,
                                onClick = {
                                    viewModel.selectCity(localCity)
                                    showCityDropdown = false
                                }
                            )

                            // 2. 时区城市 1
                            DropdownCityItem(
                                cityName = zone1City.name,
                                tag = "时区 1",
                                icon = Icons.Outlined.Public,
                                isSelected = currentCity.name == zone1City.name,
                                onClick = {
                                    viewModel.selectCity(zone1City)
                                    showCityDropdown = false
                                }
                            )

                            // 3. 时区城市 2
                            DropdownCityItem(
                                cityName = zone2City.name,
                                tag = "时区 2",
                                icon = Icons.Outlined.Public,
                                isSelected = currentCity.name == zone2City.name,
                                onClick = {
                                    viewModel.selectCity(zone2City)
                                    showCityDropdown = false
                                }
                            )

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // 第二大类：关注城市列表（用户可管理：添加、删除）
                            Text(
                                text = "关注城市 (可管理)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6EE7B7),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )

                            if (customCities.isEmpty()) {
                                Text(
                                    text = "暂无自选城市，可点击下方添加",
                                    fontSize = 12.sp,
                                    color = TextWhiteSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            } else {
                                customCities.forEach { city ->
                                    DropdownManagedCityItem(
                                        cityName = city.name,
                                        isSelected = currentCity.name == city.name,
                                        onClick = {
                                            viewModel.selectCity(city)
                                            showCityDropdown = false
                                        },
                                        onDelete = {
                                            viewModel.removeCustomCity(city)
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // 操作项：+ 添加/搜索更多城市
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showCityDropdown = false
                                        showCityDialog = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "添加城市",
                                    tint = Color(0xFF67B0F8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "添加 / 搜索更多城市",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF67B0F8)
                                )
                            }
                        }
                    }
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
                viewModel.addCustomCity(city)
            },
            onLocateClick = {
                viewModel.locateCurrentCity(context, onRequestLocationPermission)
            },
            isLocating = isLocating
        )
    }
}

@Composable
private fun DropdownCityItem(
    cityName: String,
    tag: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF67B0F8) else TextWhiteSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = cityName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF67B0F8) else TextWhitePrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = tag,
                    fontSize = 10.sp,
                    color = TextWhiteSecondary
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "当前选中",
                tint = Color(0xFF67B0F8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DropdownManagedCityItem(
    cityName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationCity,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF67B0F8) else TextWhiteSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = cityName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF67B0F8) else TextWhitePrimary,
                maxLines = 1
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "当前选中",
                    tint = Color(0xFF67B0F8),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "删除该城市",
                tint = Color(0xFFFCA5A5).copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}
