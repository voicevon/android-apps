package com.feng.suite.core.mqtt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.ErrorOutline
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
import com.feng.suite.feature.clock.ui.theme.FossifyOrangePrimary

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Hub,
                    contentDescription = null,
                    tint = FossifyOrangePrimary
                )
                Text(
                    text = "MQTT 多端同步设置",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusColor = when (status) {
                            MqttConnectionStatus.CONNECTED -> Color(0xFF4CAF50)
                            MqttConnectionStatus.CONNECTING -> Color(0xFFFF9800)
                            MqttConnectionStatus.DISCONNECTED -> Color(0xFF9E9E9E)
                            MqttConnectionStatus.ERROR -> Color(0xFFF44336)
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }

                    if (isEnabled) {
                        TextButton(
                            onClick = { MqttManager.connect() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("重新连接", fontSize = 12.sp, color = FossifyOrangePrimary)
                        }
                    }
                }

                // 2. 启用同步总开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "启用 MQTT 跨设备同步",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "开启后支持多机闹钟下发与接收",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FossifyOrangePrimary
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // 3. 本机设备名称
                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("本机设备名称") },
                    placeholder = { Text("如：客厅平板、卧室手机") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Smartphone, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. MQTT Broker 服务器地址
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("MQTT 服务器地址 (Host)") },
                    placeholder = { Text("如：broker.emqx.io 或局域网 IP") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Dns, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. 端口
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it.filter { char -> char.isDigit() } },
                    label = { Text("端口 (Port)") },
                    placeholder = { Text("默认 1883") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Sensors, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portText.toIntOrNull() ?: 1883
                    MqttManager.saveConfig(host, port, deviceName, isEnabled)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FossifyOrangePrimary)
            ) {
                Text("保存配置", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
