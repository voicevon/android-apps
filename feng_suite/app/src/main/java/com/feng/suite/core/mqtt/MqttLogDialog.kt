package com.feng.suite.core.mqtt

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feng.suite.ui.theme.CardBackgroundDark
import com.feng.suite.ui.theme.CardBorderDark
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MqttLogDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val logs by MqttManager.logs.collectAsState()
    val status by MqttManager.connectionStatus.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var expandAll by remember { mutableStateOf(false) }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "HEARTBEAT" -> logs.filter { it.msgType == "HEARTBEAT" }
            "CHAT" -> logs.filter { it.msgType == "CHAT" }
            "ALARM" -> logs.filter { it.msgType == "ALARM" }
            else -> logs
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackgroundDark)
                .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. 顶部 Header 栏（标题 + 状态点 + 操作按钮 + 关闭叉号）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Column {
                            Text(
                                text = "MQTT 消息与心跳日志",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextWhitePrimary
                            )
                            Text(
                                text = "全设备在线心跳与通信报文 · 共 ${logs.size} 条",
                                fontSize = 11.sp,
                                color = TextWhiteSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 展开/折叠全部切换按钮
                        IconButton(
                            onClick = { expandAll = !expandAll },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (expandAll) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore,
                                contentDescription = if (expandAll) "收起全部" else "展开全部",
                                tint = if (expandAll) Color(0xFF60A5FA) else TextWhiteSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // 发送测试心跳按钮
                        IconButton(
                            onClick = {
                                MqttManager.publishHeartbeat()
                                Toast.makeText(context, "已触发一次在线心跳包广播", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Favorite,
                                contentDescription = "发送心跳",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // 清空日志按钮
                        if (logs.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    MqttManager.clearLogs()
                                    Toast.makeText(context, "日志已清空", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "清空日志",
                                    tint = TextWhiteSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // 关闭叉号按钮
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "关闭",
                                tint = TextWhiteSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 2. 筛选 Chips 栏
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "全部 (${logs.size})",
                        "HEARTBEAT" to "💓 心跳 (${logs.count { it.msgType == "HEARTBEAT" }})",
                        "CHAT" to "💬 消息 (${logs.count { it.msgType == "CHAT" }})",
                        "ALARM" to "⏰ 闹钟 (${logs.count { it.msgType == "ALARM" }})"
                    )

                    items(filters) { (type, label) ->
                        val isSelected = selectedFilter == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = type },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF0F172A),
                                labelColor = TextWhiteSecondary,
                                selectedContainerColor = Color(0xFF2563EB).copy(alpha = 0.35f),
                                selectedLabelColor = Color(0xFF93C5FD)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CardBorderDark,
                                selectedBorderColor = Color(0xFF3B82F6)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // 3. 核心日志列表区
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "暂无相关日志记录",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhiteSecondary
                            )
                            Text(
                                text = "心跳每小时自动发送并记录；\n点击右上角 💓 可立即发布一条测试心跳。",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            MqttLogCard(
                                log = log,
                                timeStr = timeFormat.format(Date(log.timestamp)),
                                defaultExpanded = expandAll,
                                onCopyPayload = {
                                    clipboardManager.setText(AnnotatedString(log.payload))
                                    Toast.makeText(context, "Payload 已复制", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MqttLogCard(
    log: MqttLogEntry,
    timeStr: String,
    defaultExpanded: Boolean,
    onCopyPayload: () -> Unit
) {
    var isExpanded by remember(defaultExpanded) { mutableStateOf(defaultExpanded) }

    val (typeColor, typeBg, typeText) = when (log.msgType) {
        "HEARTBEAT" -> Triple(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.16f), "💓 心跳")
        "CHAT" -> Triple(Color(0xFF38BDF8), Color(0xFF38BDF8).copy(alpha = 0.16f), "💬 聊天")
        "ALARM" -> Triple(Color(0xFFFBBF24), Color(0xFFFBBF24).copy(alpha = 0.16f), "⏰ 闹钟")
        else -> Triple(Color(0xFF94A3B8), Color(0xFF94A3B8).copy(alpha = 0.16f), "📦 通信")
    }

    val dirColor = if (log.isOutgoing) Color(0xFF60A5FA) else Color(0xFF34D399)
    val dirText = if (log.isOutgoing) "发" else "收"

    // 提炼业务摘要文字
    val summaryText = remember(log.payload, log.msgType) {
        extractSummary(log.msgType, log.payload)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(
                1.dp,
                if (isExpanded) Color(0xFF3B82F6).copy(alpha = 0.45f) else CardBorderDark,
                RoundedCornerShape(12.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // 第一行：[时间 (绝不挤压)] + [收/发] + [类型] ---- 弹性占位 ---- [发送方设备 ➔ 目标]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间戳：固定单行，等宽字体，不换行
            Text(
                text = timeStr,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(7.dp))

            // 方向胶囊 [收/发]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(dirColor.copy(alpha = 0.18f))
                    .padding(horizontal = 4.5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = dirText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dirColor,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            // 类型胶囊 [💓 心跳 / 💬 聊天 / ⏰ 闹钟]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(typeBg)
                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = typeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 设备名称与流向（向右对齐，带省略号防溢出）
            val targetLabel = if (log.target.isNotBlank() && log.target != "全部设备") "➔ ${log.target}" else ""
            Text(
                text = "${log.sender} $targetLabel".trim(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhitePrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 第二行：紧凑高可读性业务摘要（一目了然，替代原始臃肿的 JSON）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (log.msgType == "HEARTBEAT") {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }
                Text(
                    text = summaryText,
                    fontSize = 12.sp,
                    color = if (log.msgType == "HEARTBEAT") Color(0xFF34D399) else Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = if (isExpanded) "收起 ▲" else "详情 ▼",
                fontSize = 11.sp,
                color = Color(0xFF60A5FA),
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        // 第三行：展开详情区（Topic 与格式化 JSON Payload）
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Topic: ${log.topic}",
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF64748B)
                )

                val prettyPayload = remember(log.payload) {
                    try {
                        JSONObject(log.payload).toString(2)
                    } catch (_: Exception) {
                        log.payload
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = prettyPayload,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onCopyPayload,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "复制 Payload",
                                tint = TextWhiteSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 从 Payload 提炼易读的业务摘要，避免原始 JSON 大括号刷屏
 */
private fun extractSummary(msgType: String, payload: String): String {
    return try {
        val json = JSONObject(payload)
        when (msgType) {
            "HEARTBEAT" -> {
                val status = json.optString("status", "ONLINE")
                val dev = json.optString("device", json.optString("deviceName", ""))
                "在线心跳 · 状态: $status" + if (dev.isNotBlank()) " ($dev)" else ""
            }
            "CHAT" -> {
                val content = json.optString("content", "")
                "“$content”"
            }
            "ALARM" -> {
                val action = json.optString("action", "SYNC")
                val hour = json.optInt("hour", -1)
                val minute = json.optInt("minute", -1)
                val label = json.optString("label", "")
                if (hour >= 0 && minute >= 0) {
                    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                    "闹钟: $action $timeStr" + if (label.isNotBlank()) " ($label)" else ""
                } else {
                    "闹钟动作: $action"
                }
            }
            else -> payload.take(60)
        }
    } catch (_: Exception) {
        payload.take(60)
    }
}
