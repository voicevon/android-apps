package com.fossify.clock.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fossify.clock.ui.theme.DangerRed
import com.fossify.clock.ui.theme.FossifyOrangePrimary
import kotlinx.coroutines.delay

@Composable
fun StopwatchScreen(modifier: Modifier = Modifier) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTimeMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        var lastTime = System.currentTimeMillis()
        while (isRunning) {
            delay(16L) // ~60 FPS
            val now = System.currentTimeMillis()
            elapsedTimeMillis += (now - lastTime)
            lastTime = now
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 大字号毫秒数字显示
        Text(
            text = formatStopwatchTime(elapsedTimeMillis),
            fontSize = 58.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 控制按钮栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 复位按钮
            IconButton(
                onClick = {
                    isRunning = false
                    elapsedTimeMillis = 0L
                    laps.clear()
                },
                enabled = elapsedTimeMillis > 0,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (elapsedTimeMillis > 0) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        CircleShape
                    )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "复位", tint = MaterialTheme.colorScheme.onSurface)
            }

            // 主启动/暂停按钮 (大圆)
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) DangerRed else FossifyOrangePrimary
                ),
                shape = CircleShape,
                modifier = Modifier.size(76.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "暂停" else "开始",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // 计次按钮
            IconButton(
                onClick = {
                    if (isRunning) {
                        laps.add(0, elapsedTimeMillis)
                    }
                },
                enabled = isRunning,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isRunning) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        CircleShape
                    )
            ) {
                Icon(Icons.Default.Flag, contentDescription = "计次", tint = FossifyOrangePrimary)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 计次圈数列表
        if (laps.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(laps) { index, lapTime ->
                    val lapNumber = laps.size - index
                    val lapSplit = if (index < laps.size - 1) lapTime - laps[index + 1] else lapTime

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "计次 #$lapNumber",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${formatStopwatchTime(lapSplit)}",
                            fontSize = 14.sp,
                            color = FossifyOrangePrimary
                        )
                        Text(
                            text = formatStopwatchTime(lapTime),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun formatStopwatchTime(millis: Long): String {
    val totalSec = millis / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val ms = (millis % 1000) / 10
    return String.format("%02d:%02d.%02d", min, sec, ms)
}
