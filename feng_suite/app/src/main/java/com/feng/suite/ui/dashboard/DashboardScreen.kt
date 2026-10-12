package com.feng.suite.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.feature.clock.ui.viewmodel.AlarmViewModel
import com.feng.suite.feature.todo.ui.TodoViewModel
import com.feng.suite.feature.weather.data.WeatherCodeMapper
import com.feng.suite.feature.weather.ui.viewmodel.WeatherUiState
import com.feng.suite.feature.weather.ui.viewmodel.WeatherViewModel
import com.feng.suite.ui.navigation.NavItem
import com.feng.suite.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    weatherViewModel: WeatherViewModel,
    alarmViewModel: AlarmViewModel,
    todoViewModel: TodoViewModel,
    onNavigateTo: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val currentCity by weatherViewModel.currentCity.collectAsState()
    val alarms by alarmViewModel.alarms.collectAsState()
    val allTodos by todoViewModel.allTodos.collectAsState()

    // 动态时间刷新
    var currentTimeText by remember { mutableStateOf(getCurrentTimeString()) }
    var currentDateText by remember { mutableStateOf(getCurrentDateString()) }
    var greetingText by remember { mutableStateOf(getGreetingString()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeText = getCurrentTimeString()
            currentDateText = getCurrentDateString()
            greetingText = getGreetingString()
            delay(1000L)
        }
    }

    val activeTodos = allTodos.filter { !it.isCompleted }
    val completedTodos = allTodos.filter { it.isCompleted }
    val nextAlarm = alarms.filter { it.isEnabled }
        .minByOrNull { it.calculateNextTriggerTime() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 顶部 Header 问候区
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greetingText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentDateText,
                    fontSize = 13.sp,
                    color = TextWhiteSecondary
                )
            }

            // 当前时间数字芯片
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentTimeText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF67B0F8)
                )
            }
        }

        // 2. 晨间天气速览卡片
        DashboardWeatherCard(
            cityName = currentCity.name,
            weatherState = weatherState,
            onClick = { onNavigateTo(NavItem.WEATHER) }
        )

        // 3. 闹钟与日程核心卡片
        DashboardAlarmCard(
            nextAlarm = nextAlarm,
            onToggleEnabled = { alarm -> alarmViewModel.toggleAlarm(alarm) },
            onClick = { onNavigateTo(NavItem.CLOCK) }
        )

        // 4. 今日待办聚焦卡片
        DashboardTodoCard(
            totalCount = allTodos.size,
            activeCount = activeTodos.size,
            completedCount = completedTodos.size,
            todoList = activeTodos.take(3),
            onToggleComplete = { todo -> todoViewModel.toggleCompleted(todo) },
            onClick = { onNavigateTo(NavItem.TODO) }
        )

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun DashboardWeatherCard(
    cityName: String,
    weatherState: WeatherUiState,
    onClick: () -> Unit
) {
    val cardBackgroundBrush = Brush.horizontalGradient(
        listOf(Color(0xFF1E293B), Color(0xFF1E3A5F))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBackgroundBrush)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        when (weatherState) {
            is WeatherUiState.Success -> {
                val data = weatherState.data
                val info = WeatherCodeMapper.map(data.weatherCode)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF67B0F8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cityName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = info.description,
                                fontSize = 13.sp,
                                color = TextWhiteSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${data.currentTemp}°",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "↑${data.maxTemp}° ↓${data.minTemp}° • 体感 ${data.apparentTemp}°",
                                fontSize = 12.sp,
                                color = TextWhiteSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = info.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "正在同步天气数据...",
                        fontSize = 14.sp,
                        color = TextWhiteSecondary
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF67B0F8),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardAlarmCard(
    nextAlarm: com.feng.suite.feature.clock.data.AlarmEntity?,
    onToggleEnabled: (com.feng.suite.feature.clock.data.AlarmEntity) -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "硬件级 RTC 闹钟",
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                    if (nextAlarm != null) {
                        Text(
                            text = String.format("%02d:%02d", nextAlarm.hour, nextAlarm.minute),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        Text(
                            text = if (nextAlarm.label.isNotBlank()) nextAlarm.label else "下次响铃已锁定硬件休眠唤醒",
                            fontSize = 11.sp,
                            color = TextWhiteTertiary
                        )
                    } else {
                        Text(
                            text = "暂无活跃闹钟",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhitePrimary
                        )
                    }
                }
            }

            if (nextAlarm != null) {
                Switch(
                    checked = nextAlarm.isEnabled,
                    onCheckedChange = { onToggleEnabled(nextAlarm) }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = TextWhiteSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardTodoCard(
    totalCount: Int,
    activeCount: Int,
    completedCount: Int,
    todoList: List<com.feng.suite.feature.todo.data.TodoItem>,
    onToggleComplete: (com.feng.suite.feature.todo.data.TodoItem) -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BluePrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "今日待办清单",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhitePrimary
                    )
                }

                Text(
                    text = "$completedCount / $totalCount 已完成",
                    fontSize = 12.sp,
                    color = TextWhiteSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (todoList.isEmpty()) {
                Text(
                    text = if (totalCount == 0) "暂无待办事项，点击添加" else "🎉 所有待办事项已全部完成！",
                    fontSize = 13.sp,
                    color = TextWhiteSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                todoList.forEach { todo ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(todo.colorHex))
                    } catch (_: Exception) {
                        BluePrimary
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .border(1.5.dp, color, RoundedCornerShape(5.dp))
                                .clickable { onToggleComplete(todo) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (todo.isCompleted) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = todo.title,
                            fontSize = 14.sp,
                            color = TextWhitePrimary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun getCurrentTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

private fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
    return sdf.format(Date())
}

private fun getGreetingString(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..10 -> "早上好，冯氏"
        in 11..13 -> "中午好，冯氏"
        in 14..18 -> "下午好，冯氏"
        else -> "晚上好，冯氏"
    }
}
