package com.feng.suite.feature.clock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.feature.clock.data.AlarmEntity
import com.feng.suite.feature.clock.ui.components.AlarmCard
import com.feng.suite.feature.clock.ui.components.AlarmEditDialog
import com.feng.suite.feature.clock.ui.theme.FossifyOrangePrimary
import com.feng.suite.feature.clock.ui.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsState()
    val countdownText by viewModel.nextAlarmCountdown.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("闹钟", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAlarm = null
                    showEditDialog = true
                },
                containerColor = FossifyOrangePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加闹钟", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fossify 经典顶部“下一次响铃”倒计时横幅
            AnimatedVisibility(visible = !countdownText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FossifyOrangePrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = FossifyOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = countdownText ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = FossifyOrangePrimary
                        )
                    }
                }
            }

            if (alarms.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "没有已设置的闹钟",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "点击右下角 \"+\" 快速创建闹钟",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onEdit = {
                                editingAlarm = alarm
                                showEditDialog = true
                            },
                            onDelete = { viewModel.deleteAlarm(alarm) },
                            onDaysChange = { newBitmask ->
                                viewModel.saveAlarm(
                                    id = alarm.id,
                                    hour = alarm.hour,
                                    minute = alarm.minute,
                                    label = alarm.label,
                                    daysBitmask = newBitmask,
                                    isVibrate = alarm.isVibrate,
                                    snoozeMinutes = alarm.snoozeMinutes
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlarmEditDialog(
            initialAlarm = editingAlarm,
            onDismiss = { showEditDialog = false },
            onSave = { hour, minute, label, daysBitmask, isVibrate, snoozeMinutes ->
                viewModel.saveAlarm(
                    id = editingAlarm?.id ?: 0L,
                    hour = hour,
                    minute = minute,
                    label = label,
                    daysBitmask = daysBitmask,
                    isVibrate = isVibrate,
                    snoozeMinutes = snoozeMinutes
                )
                showEditDialog = false
            }
        )
    }
}
