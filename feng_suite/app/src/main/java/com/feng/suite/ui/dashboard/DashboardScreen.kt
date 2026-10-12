package com.feng.suite.ui.dashboard

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    weatherViewModel: WeatherViewModel,
    alarmViewModel: AlarmViewModel,
    todoViewModel: TodoViewModel,
    onNavigateTo: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("feng_dashboard_prefs", Context.MODE_PRIVATE) }

    var zone1Id by remember {
        mutableStateOf(prefs.getString("second_zone_1_id", prefs.getString("second_zone_id", "America/Los_Angeles")) ?: "America/Los_Angeles")
    }
    var zone1Name by remember {
        mutableStateOf(prefs.getString("second_zone_1_name", prefs.getString("second_zone_name", "旧金山")) ?: "旧金山")
    }
    var zone2Id by remember {
        mutableStateOf(prefs.getString("second_zone_2_id", "Europe/Berlin") ?: "Europe/Berlin")
    }
    var zone2Name by remember {
        mutableStateOf(prefs.getString("second_zone_2_name", "柏林") ?: "柏林")
    }
    var editingZoneIndex by remember { mutableStateOf<Int?>(null) }

    val weatherState by weatherViewModel.weatherState.collectAsState()
    val currentCity by weatherViewModel.currentCity.collectAsState()
    val alarms by alarmViewModel.alarms.collectAsState()
    val allTodos by todoViewModel.allTodos.collectAsState()

    // 动态时间走秒刷新
    var monthDayText by remember { mutableStateOf(getMonthDayString()) }
    var weekdayText by remember { mutableStateOf(getWeekdayString()) }
    var zone1TimeText by remember { mutableStateOf(getWorldTimeString(zone1Id)) }
    var zone2TimeText by remember { mutableStateOf(getWorldTimeString(zone2Id)) }

    LaunchedEffect(zone1Id, zone2Id) {
        while (true) {
            monthDayText = getMonthDayString()
            weekdayText = getWeekdayString()
            zone1TimeText = getWorldTimeString(zone1Id)
            zone2TimeText = getWorldTimeString(zone2Id)
            delay(1000L)
        }
    }

    var selectedTodoColor by remember { mutableStateOf<String?>(null) }
    val activeTodos = allTodos.filter { !it.isCompleted }
    val filteredTodos = remember(activeTodos, selectedTodoColor) {
        if (selectedTodoColor == null) {
            activeTodos
        } else {
            activeTodos.filter { it.colorHex.equals(selectedTodoColor, ignoreCase = true) }
        }
    }

    // 筛选已开启并按最快发生时间排序的前 2 个闹钟
    val upcomingAlarms = remember(alarms) {
        alarms.filter { it.isEnabled }
            .sortedBy { it.calculateNextTriggerTime() }
            .take(2)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. 顶部 Header：左侧两行日期 + 右侧双外地时区芯片（限制 3 汉字）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧日期：两行显示（10月12日 / 星期一）
            Column {
                Text(
                    text = monthDayText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )
                Text(
                    text = weekdayText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhiteSecondary
                )
            }

            // 右侧双外地时区胶囊（上下两行排列，空间充裕防误触，与左侧两行日期完美对称）
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ZoneChip(
                    cityName = formatZoneCityName(zone1Name),
                    timeText = zone1TimeText,
                    onClick = { editingZoneIndex = 1 }
                )
                ZoneChip(
                    cityName = formatZoneCityName(zone2Name),
                    timeText = zone2TimeText,
                    onClick = { editingZoneIndex = 2 }
                )
            }
        }

        // 2. 第一行核心便当盒并排卡片：左天气 (50%) + 右闹钟 (50%)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardWeatherCard(
                cityName = currentCity.name,
                weatherState = weatherState,
                onClick = { onNavigateTo(NavItem.WEATHER) },
                modifier = Modifier
                    .weight(1f)
                    .height(136.dp)
            )

            DashboardAlarmCard(
                upcomingAlarms = upcomingAlarms,
                onToggleEnabled = { alarm -> alarmViewModel.toggleAlarm(alarm) },
                onClick = { onNavigateTo(NavItem.CLOCK) },
                modifier = Modifier
                    .weight(1f)
                    .height(136.dp)
            )
        }

        // 3. 第二行：今日待办聚焦卡片 (100% 全宽，顶部为颜色过滤器)
        DashboardTodoCard(
            selectedColor = selectedTodoColor,
            onSelectColor = { color -> selectedTodoColor = color },
            todoList = filteredTodos.take(4),
            hasAnyTodos = allTodos.isNotEmpty(),
            onToggleComplete = { todo -> todoViewModel.toggleCompleted(todo) },
            onClick = { onNavigateTo(NavItem.TODO) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // 双外地时区切换选择弹窗
    if (editingZoneIndex != null) {
        val targetIndex = editingZoneIndex!!
        var searchQuery by remember { mutableStateOf("") }
        val filteredZones = remember(searchQuery) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) {
                EXTENDED_WORLD_ZONES
            } else {
                EXTENDED_WORLD_ZONES.filter { zone ->
                    zone.name.lowercase().contains(q) ||
                    zone.nameEn.lowercase().contains(q) ||
                    zone.country.lowercase().contains(q) ||
                    zone.zoneId.lowercase().contains(q)
                }
            }
        }

        val currentSelectedZoneId = if (targetIndex == 1) zone1Id else zone2Id
        val currentSelectedZoneName = if (targetIndex == 1) zone1Name else zone2Name

        AlertDialog(
            onDismissRequest = { editingZoneIndex = null },
            title = {
                Text(
                    text = "选择第 ${if (targetIndex == 1) "一" else "二"} 外地时区城市",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextWhitePrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 搜索输入框
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "输入城市或国家搜索",
                                fontSize = 13.5.sp,
                                color = TextWhiteSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = Color(0xFF93C5FD),
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清空",
                                        tint = TextWhiteSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardBorderDark,
                            cursorColor = BluePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    // 列表展示
                    if (filteredZones.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "未搜索到匹配城市\n支持输入中英文名称或国家",
                                fontSize = 13.sp,
                                color = TextWhiteSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp)
                        ) {
                            items(filteredZones, key = { it.name + "_" + it.zoneId }) { zone ->
                                val isSelected = zone.zoneId == currentSelectedZoneId && zone.name == currentSelectedZoneName
                                val (timeStr, offsetStr) = getZoneTimeAndOffset(zone.zoneId)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.22f) else Color.Transparent)
                                        .clickable {
                                            if (targetIndex == 1) {
                                                zone1Id = zone.zoneId
                                                zone1Name = zone.name
                                                prefs.edit()
                                                    .putString("second_zone_1_id", zone.zoneId)
                                                    .putString("second_zone_1_name", zone.name)
                                                    .apply()
                                            } else {
                                                zone2Id = zone.zoneId
                                                zone2Name = zone.name
                                                prefs.edit()
                                                    .putString("second_zone_2_id", zone.zoneId)
                                                    .putString("second_zone_2_name", zone.name)
                                                    .apply()
                                            }
                                            editingZoneIndex = null
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = zone.name,
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) Color(0xFF67B0F8) else TextWhitePrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = zone.country,
                                                fontSize = 11.5.sp,
                                                color = TextWhiteSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = zone.nameEn,
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = timeStr,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color(0xFF67B0F8) else TextWhitePrimary
                                        )
                                        Text(
                                            text = offsetStr,
                                            fontSize = 11.sp,
                                            color = TextWhiteSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { editingZoneIndex = null }) {
                    Text("关闭", color = Color(0xFF67B0F8), fontSize = 14.sp)
                }
            },
            containerColor = CardBackgroundDark,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * 紧凑型天气方块卡片 (占半屏宽，去除了体感温度，包含城市、大字气温与高低温)
 */
@Composable
private fun DashboardWeatherCard(
    cityName: String,
    weatherState: WeatherUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFF1E293B), Color(0xFF162A45))
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cardBackgroundBrush)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        when (weatherState) {
            is WeatherUiState.Success -> {
                val data = weatherState.data
                val info = WeatherCodeMapper.map(data.weatherCode)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 顶部：城市名与天气描述 + 小图标
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF67B0F8),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = cityName,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhitePrimary,
                                maxLines = 1
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = info.description,
                                fontSize = 12.sp,
                                color = TextWhiteSecondary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = info.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 中部：左右结构核心温度区（24°在左放大，最高温右上，最低温右下更小）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${data.currentTemp}°",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "↑${data.maxTemp}°",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFCA5A5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "↓${data.minTemp}°",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF93C5FD)
                            )
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF67B0F8),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "同步天气中...",
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                }
            }
        }
    }
}

/**
 * 紧凑型闹钟方块卡片 (占半屏宽，展示最快触发的最多两个闹钟，带今天/明天标记)
 */
@Composable
private fun DashboardAlarmCard(
    upcomingAlarms: List<com.feng.suite.feature.clock.data.AlarmEntity>,
    onToggleEnabled: (com.feng.suite.feature.clock.data.AlarmEntity) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 第一行：时钟图标 + 紧凑 Switch 开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(17.dp)
                    )
                }

                val firstAlarm = upcomingAlarms.firstOrNull()
                if (firstAlarm != null) {
                    Switch(
                        checked = firstAlarm.isEnabled,
                        onCheckedChange = { onToggleEnabled(firstAlarm) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // 第二行：闹钟列表（最多显示两行，带今天/明天微小暗标）
            if (upcomingAlarms.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    upcomingAlarms.forEach { alarm ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextWhitePrimary.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = getAlarmDayTag(alarm.calculateNextTriggerTime()),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextWhiteSecondary.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "--:--",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextWhiteSecondary.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private data class FilterColorOption(
    val hex: String?,
    val color: Color,
    val name: String
)

@Composable
private fun DashboardTodoCard(
    selectedColor: String?,
    onSelectColor: (String?) -> Unit,
    todoList: List<com.feng.suite.feature.todo.data.TodoItem>,
    hasAnyTodos: Boolean,
    onToggleComplete: (com.feng.suite.feature.todo.data.TodoItem) -> Unit,
    onClick: () -> Unit
) {
    val filterColors = remember {
        listOf(
            FilterColorOption(null, Color.White, "全部"),
            FilterColorOption("#2563EB", Color(0xFF2563EB), "蓝色"),
            FilterColorOption("#16A34A", Color(0xFF16A34A), "绿色"),
            FilterColorOption("#DC2626", Color(0xFFDC2626), "红色"),
            FilterColorOption("#D97706", Color(0xFFD97706), "黄色")
        )
    }

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
            // 第一行：纯颜色过滤器（白色/全部、蓝色、绿色、红色、黄色），彻底移除原有文字与统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterColors.forEach { option ->
                    val isSelected = if (option.hex == null) {
                        selectedColor == null
                    } else {
                        selectedColor.equals(option.hex, ignoreCase = true)
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) option.color.copy(alpha = 0.18f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) option.color else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                if (option.hex == null) {
                                    onSelectColor(null)
                                } else {
                                    if (selectedColor.equals(option.hex, ignoreCase = true)) {
                                        onSelectColor(null)
                                    } else {
                                        onSelectColor(option.hex)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(option.color)
                                .border(
                                    width = 1.dp,
                                    color = if (option.color == Color.White) Color(0xFFCBD5E1) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = option.name,
                                    tint = if (option.color == Color.White || option.color == Color(0xFFD97706)) Color(0xFF1E293B) else Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (todoList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!hasAnyTodos) "暂无待办事项，点击添加" else "该分类下暂无待办事项",
                        fontSize = 13.sp,
                        color = TextWhiteSecondary
                    )
                }
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
                            .padding(vertical = 5.dp),
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

private fun getLocalTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

private fun getWorldTimeString(zoneId: String): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone(zoneId)
    }
    return sdf.format(Date())
}

private fun getZoneTimeAndOffset(zoneId: String): Pair<String, String> {
    val localTz = TimeZone.getDefault()
    val targetTz = TimeZone.getTimeZone(zoneId)
    val now = System.currentTimeMillis()

    val targetSdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = targetTz
    }
    val timeStr = targetSdf.format(Date(now))

    val localOffset = localTz.getOffset(now)
    val targetOffset = targetTz.getOffset(now)
    val diffHours = (targetOffset - localOffset) / (1000 * 3600)

    val offsetStr = when {
        diffHours == 0 -> "与本地同时"
        diffHours > 0 -> "快 ${diffHours}h"
        else -> "慢 ${-diffHours}h"
    }
    return Pair(timeStr, offsetStr)
}

private fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
    return sdf.format(Date())
}

private fun getMonthDayString(): String {
    val sdf = SimpleDateFormat("M月d日", Locale.CHINESE)
    return sdf.format(Date())
}

private fun getWeekdayString(): String {
    val sdf = SimpleDateFormat("EEEE", Locale.CHINESE)
    return sdf.format(Date())
}

/**
 * 城市名称限制 3 个汉字（老外地名过长时截取前3个字）
 */
private fun formatZoneCityName(name: String): String {
    return if (name.length > 3) name.substring(0, 3) else name
}

/**
 * 计算闹钟下一次响铃是今天还是明天
 */
private fun getAlarmDayTag(triggerTimeMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = triggerTimeMillis }
    return if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    ) {
        "今天"
    } else {
        "明天"
    }
}

/**
 * 顶部外地时区芯片胶囊
 */
@Composable
private fun ZoneChip(
    cityName: String,
    timeText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF2563EB).copy(alpha = 0.18f))
            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Public,
                contentDescription = null,
                tint = Color(0xFF93C5FD),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "$cityName $timeText",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF93C5FD),
                maxLines = 1
            )
        }
    }
}


