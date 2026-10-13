package com.feng.suite.feature.clock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.feng.suite.feature.clock.data.AlarmEntity
import com.feng.suite.ui.theme.YellowPrimary

@Composable
fun AlarmEditDialog(
    initialAlarm: AlarmEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        hour: Int,
        minute: Int,
        label: String,
        daysBitmask: Int,
        isVibrate: Boolean,
        snoozeMinutes: Int,
        targetDevice: String
    ) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 30) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "") }
    var daysBitmask by remember { mutableIntStateOf(initialAlarm?.daysBitmask ?: 0) }
    var isVibrate by remember { mutableStateOf(initialAlarm?.isVibrate ?: true) }
    var snoozeMinutes by remember { mutableIntStateOf(initialAlarm?.snoozeMinutes ?: 10) }

    val rawTarget = initialAlarm?.targetDevice ?: ""
    var targetType by remember {
        mutableStateOf(
            when {
                rawTarget.isBlank() || rawTarget == "本机" -> "本机"
                rawTarget == "全部设备" -> "全部设备"
                rawTarget.startsWith("来自 ") -> "本机"
                else -> "指定设备"
            }
        )
    }
    var customDeviceName by remember {
        mutableStateOf(
            if (rawTarget != "本机" && rawTarget != "全部设备" && !rawTarget.startsWith("来自 ")) rawTarget else ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialAlarm == null) "新建闹钟" else "编辑闹钟",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 大号数字时间拾取盘
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 小时调节
                    NumberSpinner(
                        value = hour,
                        range = 0..23,
                        onValueChange = { hour = it },
                        label = "时"
                    )

                    Text(
                        text = ":",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // 分钟调节
                    NumberSpinner(
                        value = minute,
                        range = 0..59,
                        onValueChange = { minute = it },
                        label = "分"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. 标签备注
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("闹钟标签 (选填)") },
                    placeholder = { Text("如：起床、晨跑、会议") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. 星期重复设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "重复周期",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShortcutChip(text = "仅一次", isSelected = daysBitmask == 0) {
                            daysBitmask = 0
                        }
                        ShortcutChip(text = "工作日", isSelected = daysBitmask == 0b0111110) {
                            daysBitmask = 0b0111110
                        }
                        ShortcutChip(text = "每天", isSelected = daysBitmask == 0b1111111) {
                            daysBitmask = 0b1111111
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                DaySelector(
                    daysBitmask = daysBitmask,
                    onBitmaskChange = { daysBitmask = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 4. 振动与贪睡
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "响铃时振动", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isVibrate,
                        onCheckedChange = { isVibrate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = YellowPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "贪睡间隔", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 15).forEach { mins ->
                            ShortcutChip(
                                text = "${mins}分",
                                isSelected = snoozeMinutes == mins,
                                onClick = { snoozeMinutes = mins }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. 设给谁 (目标设备)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "设给谁",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShortcutChip(text = "本机", isSelected = targetType == "本机") {
                            targetType = "本机"
                        }
                        ShortcutChip(text = "全部设备", isSelected = targetType == "全部设备") {
                            targetType = "全部设备"
                        }
                        ShortcutChip(text = "指定设备", isSelected = targetType == "指定设备") {
                            targetType = "指定设备"
                        }
                    }
                }

                if (targetType == "指定设备") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDeviceName,
                        onValueChange = { customDeviceName = it },
                        label = { Text("目标设备名称") },
                        placeholder = { Text("如：卧室手机、客厅平板") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTarget = when (targetType) {
                        "本机" -> "本机"
                        "全部设备" -> "全部设备"
                        "指定设备" -> customDeviceName.trim().ifEmpty { "全部设备" }
                        else -> "本机"
                    }
                    onSave(hour, minute, label, daysBitmask, isVibrate, snoozeMinutes, finalTarget)
                },
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary)
            ) {
                Text("保存", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun NumberSpinner(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = {
                val next = if (value + 1 > range.last) range.first else value + 1
                onValueChange(next)
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = "增加 $label")
        }

        Box(
            modifier = Modifier
                .width(76.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = YellowPrimary
            )
        }

        IconButton(
            onClick = {
                val prev = if (value - 1 < range.first) range.last else value - 1
                onValueChange(prev)
            }
        ) {
            Icon(Icons.Default.Remove, contentDescription = "减少 $label")
        }
    }
}

@Composable
private fun ShortcutChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) YellowPrimary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isSelected) YellowPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
