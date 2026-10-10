package com.fossify.clock.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.fossify.clock.ui.theme.DangerRed
import com.fossify.clock.ui.theme.FossifyOrangePrimary
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(modifier: Modifier = Modifier) {
    var totalSeconds by remember { mutableIntStateOf(300) } // 默认 5 分钟
    var remainingSeconds by remember { mutableIntStateOf(300) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "TimerProgress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 中央环形倒计时器
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 10.dp
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = FossifyOrangePrimary,
                strokeWidth = 10.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTimerTime(remainingSeconds),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (remainingSeconds == 0) {
                    Text(
                        text = "时间到！",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                }
            }
        }

        // 快速预设药丸按钮 (未运行时可快捷选定)
        if (!isRunning && remainingSeconds == totalSeconds) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "常用倒计时预设",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    val presets = listOf(
                        "1分钟" to 60,
                        "3分钟" to 180,
                        "5分钟" to 300,
                        "10分钟" to 600,
                        "番茄钟" to 1500
                    )
                    presets.forEach { (name, secs) ->
                        val isSelected = totalSeconds == secs
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) FossifyOrangePrimary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    totalSeconds = secs
                                    remainingSeconds = secs
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 控制按钮栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 停止/重置按钮
            IconButton(
                onClick = {
                    isRunning = false
                    remainingSeconds = totalSeconds
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "停止", tint = MaterialTheme.colorScheme.onSurface)
            }

            // 启动/暂停主按钮
            Button(
                onClick = {
                    if (remainingSeconds == 0) {
                        remainingSeconds = totalSeconds
                    }
                    isRunning = !isRunning
                },
                colors = ButtonDefaults.buttonColors(containerColor = FossifyOrangePrimary),
                shape = CircleShape,
                modifier = Modifier.size(76.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "暂停" else "开始",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

private fun formatTimerTime(totalSec: Int): String {
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
