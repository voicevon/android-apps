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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val minGlobal = dailyList.minOfOrNull { it.minTemp } ?: 0
    val maxGlobal = dailyList.maxOfOrNull { it.maxTemp } ?: 35
    val totalRange = (maxGlobal - minGlobal).coerceAtLeast(1).toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = "未来 7 天天气预报",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhiteSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            dailyList.forEachIndexed { index, item ->
                val info = WeatherCodeMapper.map(item.weatherCode)

                // 紧凑型单行布局 (单行高度严格控制在 20dp 左右，一屏容纳更多内容)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 日期 / 星期 (今天高亮)
                    Text(
                        text = item.dateText,
                        fontSize = 13.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == 0) TextWhitePrimary else TextWhitePrimary.copy(alpha = 0.9f),
                        modifier = Modifier.width(44.dp),
                        maxLines = 1,
                        softWrap = false
                    )

                    // 2. 天气图标与描述
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(50.dp)
                    ) {
                        Icon(
                            imageVector = info.icon,
                            contentDescription = null,
                            tint = TextWhitePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = info.description.take(2),
                            fontSize = 11.sp,
                            color = TextWhiteSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // 3. 最低温 (设置宽度32dp且禁止折行，确保度数符号°绝不换行掉落)
                    Text(
                        text = "${item.minTemp}°",
                        fontSize = 13.sp,
                        color = TextWhiteSecondary,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false
                    )

                    // 4. 动态自适应弹性温域胶囊条
                    val startFraction = ((item.minTemp - minGlobal) / totalRange).coerceIn(0f, 0.9f)
                    val endFraction = ((item.maxTemp - minGlobal) / totalRange).coerceIn(startFraction + 0.1f, 1f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (startFraction > 0f) {
                                Spacer(modifier = Modifier.weight(startFraction))
                            }
                            Box(
                                modifier = Modifier
                                    .weight(endFraction - startFraction)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF81D4FA),
                                                Color(0xFFFFB74D)
                                            )
                                        )
                                    )
                            )
                            if (1f - endFraction > 0f) {
                                Spacer(modifier = Modifier.weight(1f - endFraction))
                            }
                        }
                    }

                    // 5. 最高温 (设置宽度32dp且禁止折行)
                    Text(
                        text = "${item.maxTemp}°",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (index < dailyList.size - 1) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricItem(
                icon = Icons.Outlined.WaterDrop,
                title = "空气湿度",
                valueNumber = "${data.humidity}",
                valueUnit = "%",
                subtitle = if (data.humidity > 70) "偏潮湿" else "舒适",
                modifier = Modifier.weight(1f)
            )

            MetricItem(
                icon = Icons.Outlined.Air,
                title = "实时风速",
                valueNumber = "${data.windSpeed}",
                valueUnit = "km/h",
                subtitle = "微风徐拂",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricItem(
                icon = Icons.Outlined.WbSunny,
                title = "紫外线指数",
                valueNumber = String.format("%.1f", data.uvIndex),
                valueUnit = "UV",
                subtitle = if (data.uvIndex >= 6.0) "需注意防晒" else "适中",
                modifier = Modifier.weight(1f)
            )

            MetricItem(
                icon = Icons.Outlined.Compress,
                title = "地面气压",
                valueNumber = "${data.pressure.toInt()}",
                valueUnit = "hPa",
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
    valueNumber: String,
    valueUnit: String = "",
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextWhiteSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextWhiteSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 数值与单位同排紧凑对齐，绝对不换行
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = valueNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary,
                    maxLines = 1,
                    softWrap = false
                )
                if (valueUnit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = valueUnit,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhiteSecondary,
                        modifier = Modifier.padding(bottom = 1.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextWhiteTertiary,
                maxLines = 1
            )
        }
    }
}
