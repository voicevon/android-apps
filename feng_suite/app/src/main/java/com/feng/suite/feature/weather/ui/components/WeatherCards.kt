package com.feng.suite.feature.weather.ui.components

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
import com.feng.suite.feature.weather.data.DailyForecast
import com.feng.suite.feature.weather.data.HourlyForecast
import com.feng.suite.feature.weather.data.WeatherCodeMapper
import com.feng.suite.feature.weather.data.WeatherData
import com.feng.suite.ui.theme.CardBackground
import com.feng.suite.ui.theme.CardBorder
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary
import com.feng.suite.ui.theme.TextWhiteTertiary

@Composable
fun CurrentWeatherSection(
    data: WeatherData,
    modifier: Modifier = Modifier
) {
    val info = WeatherCodeMapper.map(data.weatherCode)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // 左大栏：图标在上方，天气描述在下方
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.description,
                tint = TextWhitePrimary,
                modifier = Modifier.size(62.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = info.description,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhitePrimary
            )
        }

        Spacer(modifier = Modifier.width(26.dp))

        // 右大栏：核心温度区（左侧实时超大温度，右侧分三行最高/最低/体感）
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${data.currentTemp}°",
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = TextWhitePrimary,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "湿度 ${data.humidity}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF93C5FD)
                )
                Text(
                    text = "风速 ${data.windSpeed}km/h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA7F3D0)
                )
                Text(
                    text = "PM2.5 ${data.pm25.toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhiteSecondary
                )
            }
        }
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

