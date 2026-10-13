package com.feng.suite.core.mqtt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.ui.theme.CardBackgroundDark
import com.feng.suite.ui.theme.CardBorderDark
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary

@Composable
fun MqttSettingsDialog(
    onDismiss: () -> Unit
) {
    val config by MqttManager.config.collectAsState()
    val status by MqttManager.connectionStatus.collectAsState()

    var host by remember(config) { mutableStateOf(config.serverHost) }
    var portText by remember(config) { mutableStateOf(config.serverPort.toString()) }
    var deviceName by remember(config) { mutableStateOf(config.deviceName) }
    var isEnabled by remember(config) { mutableStateOf(config.isEnabled) }
    var showAdvanced by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Hub,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "MQTT 同步设置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "设备名称与通信参数",
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. 当前连接状态栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusColor = when (status) {
                            MqttConnectionStatus.CONNECTED -> Color(0xFF10B981)
                            MqttConnectionStatus.CONNECTING -> Color(0xFFF59E0B)
                            MqttConnectionStatus.DISCONNECTED -> Color(0xFF64748B)
                            MqttConnectionStatus.ERROR -> Color(0xFFEF4444)
                        }
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        val statusLabel = when (status) {
                            MqttConnectionStatus.CONNECTED -> "已连接 (在线)"
                            MqttConnectionStatus.CONNECTING -> "正在连接..."
                            MqttConnectionStatus.DISCONNECTED -> "未连接"
                            MqttConnectionStatus.ERROR -> "连接失败"
                        }
                        Text(
                            text = statusLabel,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }

                    if (isEnabled) {
                        TextButton(
                            onClick = { MqttManager.connect() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("重新连接", fontSize = 12.sp, color = Color(0xFF60A5FA))
                        }
                    }
                }

                // 2. 本机设备名称（核心突出项）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "本机设备名称",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "用于多端即时通讯与闹钟同步的身份标识",
                        fontSize = 11.5.sp,
                        color = TextWhiteSecondary
                    )

                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        placeholder = { Text("如：客厅平板、卧室手机") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Smartphone,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = CardBorderDark,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 常用快速别名
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("客厅平板", "卧室手机", "备用设备").forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable { deviceName = suggestion }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // 3. 启用开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "启用 MQTT 跨端同步",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhitePrimary
                        )
                        Text(
                            text = "开启后支持闹钟同步与即时通讯",
                            fontSize = 11.sp,
                            color = TextWhiteSecondary
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB)
                        )
                    )
                }

                // 4. 高级连接参数折叠栏（Server Host & Port）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvanced = !showAdvanced }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "高级网络参数 (${host}:${portText})",
                            fontSize = 12.sp,
                            color = TextWhiteSecondary
                        )
                        Icon(
                            imageVector = if (showAdvanced) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = TextWhiteSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = showAdvanced) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = host,
                                onValueChange = { host = it },
                                label = { Text("Broker 服务器 Host") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Dns, contentDescription = null, tint = Color(0xFF94A3B8))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = CardBorderDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = portText,
                                onValueChange = { portText = it.filter { char -> char.isDigit() } },
                                label = { Text("端口 (默认 1883)") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Sensors, contentDescription = null, tint = Color(0xFF94A3B8))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = CardBorderDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portText.toIntOrNull() ?: 1883
                    MqttManager.saveConfig(host, port, deviceName, isEnabled)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("保存设置", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextWhiteSecondary)
            }
        },
        containerColor = CardBackgroundDark,
        shape = RoundedCornerShape(20.dp)
    )
}
