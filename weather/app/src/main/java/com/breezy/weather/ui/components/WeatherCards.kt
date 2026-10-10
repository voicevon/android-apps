package com.breezy.weather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breezy.weather.data.DailyForecast
import com.breezy.weather.data.HourlyForecast
import com.breezy.weather.data.WeatherCodeMapper
import com.breezy.weather.data.WeatherData
import com.breezy.weather.ui.theme.CardBackground
import com.breezy.weather.ui.theme.CardBorder
import com.breezy.weather.ui.theme.TextWhitePrimary
import com.breezy.weather.ui.theme.TextWhiteSecondary
import com.breezy.weather.ui.theme.TextWhiteTertiary

@Composable
fun CurrentWeatherSection(
    data: WeatherData,
    modifier: Modifier = Modifier
) {
    val info = WeatherCodeMapper.map(data.weatherCode)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 大图标
        Icon(
            imageVector = info.icon,
            contentDescription = info.description,
            tint = TextWhitePrimary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 超大当前温度
        Text(
            text = "${data.currentTemp}°",
            fontSize = 78.sp,
            fontWeight = FontWeight.Light,
            color = TextWhitePrimary,
            letterSpacing = (-2).sp
        )

        // 天气描述
        Text(
            text = info.description,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = TextWhitePrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 当日高低温与体感
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "↑${data.maxTemp}°  ↓${data.minTemp}°",
                fontSize = 15.sp,
                color = TextWhiteSecondary
            )
            Text(
                text = "•",
                fontSize = 15.sp,
                color = TextWhiteTertiary
            )
            Text(
                text = "体感 ${data.apparentTemp}°",
                fontSize = 15.sp,
                color = TextWhiteSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = data.updateTimeText,
            fontSize = 12.sp,
            color = TextWhiteTertiary
        )

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
fun HourlyForecastCard(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "24 小时气温趋势",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhiteSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(hourlyList) { item ->
                    val info = WeatherCodeMapper.map(item.weatherCode)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(52.dp)
                    ) {
                        Text(
                            text = item.time,
                            fontSize = 12.sp,
                            color = TextWhiteSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Icon(
                            imageVector = info.icon,
                            contentDescription = null,
                            tint = TextWhitePrimary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${item.temperature}°",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastCard(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "未来 7 天天气预报",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhiteSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            dailyList.forEachIndexed { index, item ->
                val info = WeatherCodeMapper.map(item.weatherCode)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 日期/星期
                    Text(
                        text = item.dateText,
                        fontSize = 15.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        color = TextWhitePrimary,
                        modifier = Modifier.width(56.dp)
                    )

                    // 天气图标与描述
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(72.dp)
                    ) {
                        Icon(
                            imageVector = info.icon,
                            contentDescription = null,
                            tint = TextWhitePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = info.description,
                            fontSize = 13.sp,
                            color = TextWhiteSecondary
                        )
                    }

                    // 温度区间
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${item.minTemp}°",
                            fontSize = 14.sp,
                            color = TextWhiteSecondary
                        )

                        // 极简微温度条
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .width(70.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .width(46.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.85f))
                            )
                        }

                        Text(
                            text = "${item.maxTemp}°",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                    }
                }

                if (index < dailyList.size - 1) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
fun MetricsGridCard(
    data: WeatherData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricItem(
                icon = Icons.Outlined.WaterDrop,
                title = "空气湿度",
                value = "${data.humidity}%",
                subtitle = if (data.humidity > 70) "偏潮湿" else "舒适",
                modifier = Modifier.weight(1f)
            )

            MetricItem(
                icon = Icons.Outlined.Air,
                title = "实时风速",
                value = "${data.windSpeed} km/h",
                subtitle = "微风徐拂",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricItem(
                icon = Icons.Outlined.WbSunny,
                title = "紫外线指数",
                value = String.format("%.1f", data.uvIndex),
                subtitle = if (data.uvIndex >= 6.0) "需注意防晒" else "适中",
                modifier = Modifier.weight(1f)
            )

            MetricItem(
                icon = Icons.Outlined.Compress,
                title = "地面气压",
                value = "${data.pressure.toInt()} hPa",
                subtitle = "标准大气压",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextWhiteSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextWhiteSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextWhiteTertiary
            )
        }
    }
}
