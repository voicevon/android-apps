package com.feng.suite.ui.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.feng.suite.core.mqtt.MqttLogDialog
import com.feng.suite.core.mqtt.MqttMenuDialog
import com.feng.suite.core.mqtt.MqttSettingsDialog
import com.feng.suite.feature.chat.ui.components.DashboardChatCard
import com.feng.suite.feature.chat.ui.viewmodel.ChatViewModel
import com.feng.suite.feature.clock.ui.components.DashboardAlarmCard
import com.feng.suite.feature.clock.ui.viewmodel.AlarmViewModel
import com.feng.suite.feature.todo.ui.TodoViewModel
import com.feng.suite.feature.todo.ui.components.AddEditTodoDialog
import com.feng.suite.feature.todo.ui.components.DashboardTodoCard
import com.feng.suite.feature.weather.ui.components.DashboardWeatherCard
import com.feng.suite.feature.weather.ui.viewmodel.WeatherViewModel
import com.feng.suite.ui.navigation.NavItem
import com.feng.suite.ui.theme.BackgroundDark
import com.feng.suite.ui.theme.TagColors
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    weatherViewModel: WeatherViewModel,
    alarmViewModel: AlarmViewModel,
    todoViewModel: TodoViewModel,
    chatViewModel: ChatViewModel,
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

    var showMqttMenuDialog by remember { mutableStateOf(false) }
    var showMqttSettingsDialog by remember { mutableStateOf(false) }
    var showMqttLogDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
        // 1. 顶部 Header：左侧两行日期 + 中右双外地时区芯片 + 最右上角主菜单 (三道杠)
        DashboardHeader(
            nowMillis = nowMillis,
            zone1Id = zone1Id,
            zone1Name = zone1Name,
            zone2Id = zone2Id,
            zone2Name = zone2Name,
            onZoneClick = { editingZoneIndex = it },
            onOpenSettings = { showMqttMenuDialog = true },
            onOpenAbout = { showAboutDialog = true }
        )

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

        // 4. 第三行：即时消息卡片 (纯净输入框 + 发送，点击空白处或图标进入全屏聊天历史)
        DashboardChatCard(
            onSendMessage = { text -> chatViewModel.sendMessage(text) },
            onClick = { onNavigateTo(NavItem.CHAT) },
            modifier = Modifier.fillMaxWidth()
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
    editingZoneIndex?.let { targetIndex ->
        WorldZonePickerDialog(
            slotIndex = targetIndex,
            selectedZoneId = if (targetIndex == 1) zone1Id else zone2Id,
            selectedZoneName = if (targetIndex == 1) zone1Name else zone2Name,
            onSelect = { zone ->
                if (targetIndex == 1) {
                    zone1Id = zone.zoneId
                    zone1Name = zone.name
                } else {
                    zone2Id = zone.zoneId
                    zone2Name = zone.name
                }
                prefs.edit()
                    .putString("world_clock_${targetIndex}_id", zone.zoneId)
                    .putString("world_clock_${targetIndex}_name", zone.name)
                    .apply()
                editingZoneIndex = null
            },
            onDismiss = { editingZoneIndex = null }
        )
    }

    if (showMqttMenuDialog) {
        MqttMenuDialog(
            onDismiss = { showMqttMenuDialog = false },
            onOpenSettings = {
                showMqttMenuDialog = false
                showMqttSettingsDialog = true
            },
            onOpenLogs = {
                showMqttMenuDialog = false
                showMqttLogDialog = true
            }
        )
    }

    if (showMqttSettingsDialog) {
        MqttSettingsDialog(
            onDismiss = { showMqttSettingsDialog = false }
        )
    }

    if (showMqttLogDialog) {
        MqttLogDialog(
            onDismiss = { showMqttLogDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}
