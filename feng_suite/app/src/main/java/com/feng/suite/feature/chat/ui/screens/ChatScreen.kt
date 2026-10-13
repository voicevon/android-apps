package com.feng.suite.feature.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.core.mqtt.MqttConnectionStatus
import com.feng.suite.core.mqtt.MqttManager
import com.feng.suite.feature.chat.data.ChatMessageEntity
import com.feng.suite.feature.chat.ui.viewmodel.ChatViewModel
import com.feng.suite.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val mqttStatus by MqttManager.connectionStatus.collectAsState()
    val mqttConfig by MqttManager.config.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showWikiDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<ChatMessageEntity?>(null) }

    val listState = rememberLazyListState()

    // 默认时间正序，有新消息时平滑滚到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "即时消息",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = TextWhitePrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // 在线微指示小圆点 (绿/橙/灰/红直观代表连接状态)
                            val statusColor = when (mqttStatus) {
                                MqttConnectionStatus.CONNECTED -> Color(0xFF4CAF50)
                                MqttConnectionStatus.CONNECTING -> Color(0xFFFF9800)
                                MqttConnectionStatus.DISCONNECTED -> Color(0xFF9E9E9E)
                                MqttConnectionStatus.ERROR -> Color(0xFFF44336)
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                        }
                        // 仅显示本机设备名，精简无冗余字样
                        Text(
                            text = "本机: ${mqttConfig.deviceName}",
                            fontSize = 12.sp,
                            color = TextWhiteSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回概览",
                            tint = TextWhitePrimary
                        )
                    }
                },
                actions = {
                    // 查看 MQTT 协议规范 Wiki
                    IconButton(onClick = { showWikiDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "协议规范 Wiki",
                            tint = Color(0xFF93C5FD)
                        )
                    }

                    // 清空历史记录按钮
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "清空消息",
                                tint = TextWhiteSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
            // 底部消息发送栏
            Surface(
                color = CardBackgroundDark,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text("输入消息广播给所有设备...", fontSize = 14.sp, color = TextWhiteSecondary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary,
                            cursorColor = BluePrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) BluePrimary else Color(0xFF334155))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = TextWhiteSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "暂无消息记录",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhiteSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "在下方输入框可立即向全部设备发布即时消息",
                            fontSize = 12.sp,
                            color = TextWhiteTertiary
                        )
                    }
                }
            } else {
                // 永远时间正序排列
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onLongClick = { messageToDelete = msg }
                        )
                    }
                }
            }
        }
    }

    // 单条删除确认
    messageToDelete?.let { msg ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("删除该条消息？") },
            text = { Text("内容: “${msg.content}”") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMessage(msg.id)
                        messageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Text("删除", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 全部清空确认
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空所有消息记录？") },
            text = { Text("此操作将清空本地所有已收发的即时消息历史记录。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAll()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Text("清空", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 协议规范 Wiki 弹窗
    if (showWikiDialog) {
        MqttProtocolWikiDialog(onDismiss = { showWikiDialog = false })
    }
}

@Composable
private fun MqttProtocolWikiDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA)
                )
                Text(
                    text = "MQTT 通讯协议与字段规范",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "【Broker 连接配置】\n• Host: voicevon.vicp.io\n• Port: 1883 (TCP / QoS 1)\n• ClientID: feng_suite_{UUID}",
                    fontSize = 12.5.sp,
                    color = TextWhiteSecondary,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = CardBorderDark)

                Text(
                    text = "【1. 设备在线心跳包】\n• 频率: 每 1 小时上报一次\n• Topic: feng_suite/heartbeat\n• Payload 字段:\n  - msgType: \"HEARTBEAT\"\n  - device: 设备名称\n  - timestamp: 毫秒时间戳\n  - status: \"ONLINE\"",
                    fontSize = 12.sp,
                    color = TextWhitePrimary,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = CardBorderDark)

                Text(
                    text = "【2. 即时消息广播/单播】\n• 广播 Topic: feng_suite/chat/broadcast\n• 定向 Topic: feng_suite/chat/device/{deviceName}\n• Payload 字段:\n  - msgType: \"CHAT\"\n  - id: 消息UUID\n  - sender: 发送设备名\n  - target: 目标（\"全部设备\"或指定名）\n  - content: 消息文本正文\n  - timestamp: 毫秒时间戳",
                    fontSize = 12.sp,
                    color = TextWhitePrimary,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = CardBorderDark)

                Text(
                    text = "【3. 闹钟同步广播】\n• Topic: feng_suite/alarm/broadcast\n• Payload 字段:\n  - action: CREATE_OR_UPDATE / TOGGLE / DELETE\n  - sourceDevice: 发起设备\n  - targetDevice: 目标设备\n  - hour / minute: 响铃时分\n  - isEnabled: 开关状态\n  - daysBitmask: 重复星期掩码\n  - label: 闹钟标签",
                    fontSize = 12.sp,
                    color = TextWhitePrimary,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("了解", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessageEntity,
    onLongClick: () -> Unit
) {
    val isMe = message.isFromMe
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // 发送者与时间说明行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (!isMe) {
                Text(
                    text = message.sender,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34D399)
                )
            }
            Text(
                text = timeFormatted,
                fontSize = 10.5.sp,
                color = TextWhiteTertiary
            )
            if (isMe) {
                Text(
                    text = "我",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF60A5FA)
                )
            }
        }

        // 气泡实体
        val bubbleShape = if (isMe) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        } else {
            RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }

        val bubbleBackgroundModifier = if (isMe) {
            Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))))
        } else {
            Modifier
                .background(Color(0xFF1E293B))
                .border(1.dp, CardBorderDark, bubbleShape)
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .then(bubbleBackgroundModifier)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content,
                fontSize = 14.5.sp,
                color = TextWhitePrimary,
                lineHeight = 20.sp
            )
        }
    }
}
