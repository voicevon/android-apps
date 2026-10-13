package com.feng.suite.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.core.mqtt.MqttConnectionStatus
import com.feng.suite.core.mqtt.MqttManager
import com.feng.suite.ui.theme.*

@Composable
fun DashboardChatCard(
    onSendMessage: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quickText by remember { mutableStateOf("") }
    val mqttStatus by MqttManager.connectionStatus.collectAsState()

    // 整个外层卡片容器：点击空白处直接展开进入 ChatScreen
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 左侧消息小图标（带在线状态指示），点击直接进入 Chat 页面
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "查看消息历史",
                    tint = if (mqttStatus == MqttConnectionStatus.CONNECTED) Color(0xFF60A5FA) else TextWhiteSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            // 单行文本输入框
            OutlinedTextField(
                value = quickText,
                onValueChange = { quickText = it },
                placeholder = {
                    Text("发送消息...", fontSize = 13.sp, color = TextWhiteSecondary)
                },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = CardBorderDark,
                    focusedTextColor = TextWhitePrimary,
                    unfocusedTextColor = TextWhitePrimary,
                    cursorColor = BluePrimary,
                    focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
                    unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (quickText.isNotBlank()) {
                            onSendMessage(quickText)
                            quickText = ""
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            // 发送按钮
            IconButton(
                onClick = {
                    if (quickText.isNotBlank()) {
                        onSendMessage(quickText)
                        quickText = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (quickText.isNotBlank()) BluePrimary else Color(0xFF334155))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
