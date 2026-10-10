package com.fossify.clock.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fossify.clock.alarm.AlarmRinger
import com.fossify.clock.alarm.AlarmScheduler
import com.fossify.clock.receiver.AlarmReceiver
import com.fossify.clock.ui.theme.DangerRed
import com.fossify.clock.ui.theme.FossifyClockTheme
import com.fossify.clock.ui.theme.FossifyDarkBackground
import com.fossify.clock.ui.theme.FossifyOrangePrimary

class RingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 亮屏并允许锁屏上方显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)
        val hour = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_HOUR, 7)
        val minute = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, 30)
        val label = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: ""
        val isVibrate = intent.getBooleanExtra(AlarmReceiver.EXTRA_IS_VIBRATE, true)
        val snoozeMinutes = intent.getIntExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, 10)

        setContent {
            FossifyClockTheme(darkTheme = true) {
                RingingScreen(
                    hour = hour,
                    minute = minute,
                    label = label,
                    snoozeMinutes = snoozeMinutes,
                    onSnooze = {
                        AlarmRinger.stop(this)
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.cancel(alarmId.toInt())
                        if (alarmId > 0) {
                            AlarmScheduler(this).scheduleSnooze(alarmId, snoozeMinutes, label, isVibrate)
                        }
                        finish()
                    },
                    onDismiss = {
                        AlarmRinger.stop(this)
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.cancel(alarmId.toInt())
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmRinger.stop(this)
    }
}

@Composable
fun RingingScreen(
    hour: Int,
    minute: Int,
    label: String,
    snoozeMinutes: Int,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlarmScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FossifyDarkBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 动态呼吸闹钟图标
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(FossifyOrangePrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = FossifyOrangePrimary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 响铃时间与标签
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", hour, minute),
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (label.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = FossifyOrangePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 贪睡按钮
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.height(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("贪睡 ($snoozeMinutes 分钟)", color = Color.White, fontSize = 15.sp)
                }

                // 关闭按钮 (红色高亮)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.height(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("关闭闹钟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
