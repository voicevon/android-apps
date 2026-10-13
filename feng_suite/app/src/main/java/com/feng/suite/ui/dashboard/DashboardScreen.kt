package com.feng.suite.ui.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.core.mqtt.MqttSettingsDialog
import com.feng.suite.feature.clock.data.AlarmEntity
import com.feng.suite.feature.clock.ui.viewmodel.AlarmViewModel
import com.feng.suite.feature.todo.data.TodoItem
import com.feng.suite.feature.todo.ui.TodoViewModel
import com.feng.suite.feature.todo.ui.components.AddEditTodoDialog
import com.feng.suite.feature.todo.ui.components.ColorFilterBar
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
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("feng_dashboard_prefs", Context.MODE_PRIVATE) }

    var zone1Id by remember {
        mutableStateOf(prefs.getString("world_clock_1_id", "America/Los_Angeles") ?: "America/Los_Angeles")
    }
    var zone1Name by remember {
        mutableStateOf(prefs.getString("world_clock_1_name", "旧金山") ?: "旧金山")
    }
    var zone2Id by remember {
        mutableStateOf(prefs.getString("world_clock_2_id", "Europe/Berlin") ?: "Europe/Berlin")
    }
    var zone2Name by remember {
        mutableStateOf(prefs.getString("world_clock_2_name", "柏林") ?: "柏林")
    }
    var editingZoneIndex by remember { mutableStateOf<Int?>(null) }

    val weatherState by weatherViewModel.weatherState.collectAsState()
    val currentCity by weatherViewModel.currentCity.collectAsState()
    val alarms by alarmViewModel.alarms.collectAsState()
    val allTodos by todoViewModel.allTodos.collectAsState()

    // 按整分钟刷新（界面只显示到分钟），所有依赖当前时间的内容均从 nowMillis 派生
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - System.currentTimeMillis() % 60_000L)
            nowMillis = System.currentTimeMillis()
        }
    }
    val monthDayText = remember(nowMillis) { getMonthDayString(nowMillis) }
    val weekdayText = remember(nowMillis) { getWeekdayString(nowMillis) }
    val zone1TimeText = remember(nowMillis, zone1Id) { getWorldTimeString(zone1Id, nowMillis) }
    val zone2TimeText = remember(nowMillis, zone2Id) { getWorldTimeString(zone2Id, nowMillis) }

    // 选中的待办颜色过滤器持久化：默认不为白色（默认蓝色），若用户曾经选过白色则恢复白色，若选过其他颜色则恢复该颜色
    val savedTodoColor = remember { prefs.getString("selected_todo_color", TagColors.first()) ?: TagColors.first() }
    var selectedTodoColor by remember {
        mutableStateOf<String?>(if (savedTodoColor == "ALL") null else savedTodoColor)
    }
    val onSelectTodoColor: (String?) -> Unit = { newColor ->
        selectedTodoColor = newColor
        prefs.edit().putString("selected_todo_color", newColor ?: "ALL").apply()
    }
    var showAddTodoDialog by remember { mutableStateOf(false) }

    // 隐藏 7 连击进入 MQTT 设置窗口
    var dateClickCount by remember { mutableIntStateOf(0) }
    var lastDateClickTime by remember { mutableLongStateOf(0L) }
    var showMqttSettingsDialog by remember { mutableStateOf(false) }

    val activeTodos = remember(allTodos) { allTodos.filter { !it.isCompleted } }
    val filteredTodos = remember(activeTodos, selectedTodoColor) {
        if (selectedTodoColor == null) {
            activeTodos
        } else {
            activeTodos.filter { it.colorHex.equals(selectedTodoColor, ignoreCase = true) }
        }
    }

    // 筛选已开启并按最快发生时间排序的前 2 个闹钟
    val upcomingAlarms = remember(alarms, nowMillis) {
        alarms.filter { it.isEnabled }
            .map { it to it.calculateNextTriggerTime(nowMillis) }
            .sortedBy { it.second }
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
            // 左侧日期：两行显示（10月12日 / 星期一），连续点击 7 次触发隐藏设置
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastDateClickTime < 500L) {
                            dateClickCount++
                            if (dateClickCount >= 7) {
                                dateClickCount = 0
                                showMqttSettingsDialog = true
                            }
                        } else {
                            dateClickCount = 1
                        }
                        lastDateClickTime = now
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
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
                nowMillis = nowMillis,
                onClick = { onNavigateTo(NavItem.CLOCK) },
                modifier = Modifier
                    .weight(1f)
                    .height(136.dp)
            )
        }

        // 3. 第二行：今日待办聚焦卡片 (100% 全宽，顶部为颜色过滤器与加号胶囊)
        DashboardTodoCard(
            selectedColor = selectedTodoColor,
            onSelectColor = onSelectTodoColor,
            todoList = filteredTodos.take(4),
            hasAnyTodos = allTodos.isNotEmpty(),
            onToggleComplete = { todo -> todoViewModel.toggleCompleted(todo) },
            onAddTodo = { showAddTodoDialog = true },
            onClick = { onNavigateTo(NavItem.TODO) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // 快捷新建任务弹窗
    if (showAddTodoDialog) {
        AddEditTodoDialog(
            initialTodo = null,
            initialColorHex = selectedTodoColor,
            onDismiss = { showAddTodoDialog = false },
            onConfirm = { title, desc, hasReminder, reminderTime, colorHex ->
                todoViewModel.addTodo(
                    title = title,
                    description = desc,
                    hasReminder = hasReminder,
                    reminderTime = reminderTime,
                    colorHex = colorHex
                )
                showAddTodoDialog = false
            }
        )
    }

    // 双外地时区切换选择弹窗
    if (editingZoneIndex != null) {
        val targetIndex = editingZoneIndex!!
        var searchQuery by remember { mutableStateOf("") }
        val filteredZones = remember(searchQuery) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) {
                SELECTABLE_WORLD_ZONES
            } else {
                SELECTABLE_WORLD_ZONES.filter { zone ->
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
                                                    .putString("world_clock_1_id", zone.zoneId)
                                                    .putString("world_clock_1_name", zone.name)
                                                    .apply()
                                            } else {
                                                zone2Id = zone.zoneId
                                                zone2Name = zone.name
                                                prefs.edit()
                                                    .putString("world_clock_2_id", zone.zoneId)
                                                    .putString("world_clock_2_name", zone.name)
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

    if (showMqttSettingsDialog) {
        MqttSettingsDialog(
            onDismiss = { showMqttSettingsDialog = false }
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
            is WeatherUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = Color(0xFFFCA5A5),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "天气获取失败",
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                    Text(
                        text = "点击查看详情",
                        fontSize = 11.sp,
                        color = TextWhiteSecondary.copy(alpha = 0.6f)
                    )
                }
            }
            WeatherUiState.Loading -> {
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
 * 紧凑型闹钟方块卡片 (占半屏宽，展示最快触发的最多两个闹钟，带今天/明天/周X标记)
 * upcomingAlarms: (闹钟, 下次触发时间戳) 列表
 */
@Composable
private fun DashboardAlarmCard(
    upcomingAlarms: List<Pair<AlarmEntity, Long>>,
    nowMillis: Long,
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
            // 第一行：时钟图标（开关操作请进入闹钟页）
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

            // 第二行：闹钟列表（最多显示两行，带今天/明天/周X微小暗标）
            if (upcomingAlarms.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    upcomingAlarms.forEach { (alarm, triggerAtMillis) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(Locale.ROOT, "%02d:%02d", alarm.hour, alarm.minute),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextWhitePrimary.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = getAlarmDayTag(triggerAtMillis, nowMillis),
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

@Composable
private fun DashboardTodoCard(
    selectedColor: String?,
    onSelectColor: (String?) -> Unit,
    todoList: List<TodoItem>,
    hasAnyTodos: Boolean,
    onToggleComplete: (TodoItem) -> Unit,
    onAddTodo: () -> Unit,
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
            // 第一行：纯颜色过滤器 + 最右侧胶囊加号新建任务按钮
            ColorFilterBar(
                selectedColorHex = selectedColor,
                onSelectColor = onSelectColor,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onAddTodo)
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新建待办任务",
                            tint = TextWhitePrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            )

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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 32dp 点击区包裹 18dp 可视勾选框，降低误触卡片跳转的概率
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleComplete(todo) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(1.5.dp, color, RoundedCornerShape(5.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(3.dp))

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

private fun getWorldTimeString(zoneId: String, nowMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone(zoneId)
    }
    return sdf.format(Date(nowMillis))
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
    // 按分钟计算，支持 +5:30、+9:30 等半小时时区
    val diffMinutes = (targetOffset - localOffset) / (1000 * 60)
    val absHours = kotlin.math.abs(diffMinutes) / 60
    val absMinutes = kotlin.math.abs(diffMinutes) % 60
    val amount = if (absMinutes == 0) "${absHours}h" else "${absHours}h${absMinutes}m"

    val offsetStr = when {
        diffMinutes == 0 -> "与本地同时"
        diffMinutes > 0 -> "快 $amount"
        else -> "慢 $amount"
    }
    return Pair(timeStr, offsetStr)
}

private fun getMonthDayString(nowMillis: Long): String {
    val sdf = SimpleDateFormat("M月d日", Locale.CHINESE)
    return sdf.format(Date(nowMillis))
}

private fun getWeekdayString(nowMillis: Long): String {
    val sdf = SimpleDateFormat("EEEE", Locale.CHINESE)
    return sdf.format(Date(nowMillis))
}

/**
 * 城市名称限制 3 个汉字（老外地名过长时截取前3个字）
 */
private fun formatZoneCityName(name: String): String {
    return if (name.length > 3) name.substring(0, 3) else name
}

/**
 * 计算闹钟下一次响铃日标签：今天 / 明天 / 周X（重复闹钟最远 7 天内）
 */
private fun getAlarmDayTag(triggerTimeMillis: Long, nowMillis: Long): String {
    fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // 四舍五入以吸收夏令时切换日的 23h/25h
    val dayDiff = Math.round((startOfDay(triggerTimeMillis) - startOfDay(nowMillis)) / 86_400_000.0)
    return when (dayDiff) {
        0L -> "今天"
        1L -> "明天"
        else -> SimpleDateFormat("EEE", Locale.CHINESE).format(Date(triggerTimeMillis))
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


