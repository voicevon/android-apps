package com.feng.suite.core.mqtt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.ui.theme.CardBackgroundDark
import com.feng.suite.ui.theme.CardBorderDark
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary

@Composable
fun MqttMenuDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogs: () -> Unit
) {
    val config by MqttManager.config.collectAsState()
    val status by MqttManager.connectionStatus.collectAsState()
    val logs by MqttManager.logs.collectAsState()

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
                        .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Hub,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "MQTT 控制中心",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhitePrimary
                    )
                    Text(
                        text = "多端通信调试与设备监控",
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
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 状态状态栏概览卡片
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
                            MqttConnectionStatus.CONNECTED -> "在线 (已连接)"
                            MqttConnectionStatus.CONNECTING -> "连接中..."
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

                    Text(
                        text = "本机: ${config.deviceName}",
                        fontSize = 12.sp,
                        color = TextWhiteSecondary
                    )
                }

                // 2. 菜单入口 1：MQTT 同步设置
                MenuEntryCard(
                    icon = Icons.Outlined.Settings,
                    iconBg = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    iconTint = Color(0xFFFBBF24),
                    title = "MQTT 同步设置",
                    subtitle = "配置本机名称与 Broker 连接参数",
                    onClick = onOpenSettings
                )

                // 3. 菜单入口 2：MQTT 消息与心跳日志
                val heartbeatCount = logs.count { it.msgType == "HEARTBEAT" }
                MenuEntryCard(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    iconBg = Color(0xFF10B981).copy(alpha = 0.15f),
                    iconTint = Color(0xFF34D399),
                    title = "MQTT 消息与心跳日志",
                    subtitle = "查看设备心跳、即时消息与通信 Payload",
                    badgeText = "${logs.size} 条 (心跳 $heartbeatCount)",
                    onClick = onOpenLogs
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = TextWhiteSecondary)
            }
        },
        containerColor = CardBackgroundDark,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun MenuEntryCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhitePrimary
                    )
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(iconTint.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconTint
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = TextWhiteSecondary,
                    lineHeight = 15.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextWhiteSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
