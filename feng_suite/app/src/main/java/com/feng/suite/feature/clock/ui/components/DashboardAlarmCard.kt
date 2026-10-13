package com.feng.suite.feature.clock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.feature.clock.data.AlarmEntity
import com.feng.suite.ui.theme.CardBackgroundDark
import com.feng.suite.ui.theme.CardBorderDark
import com.feng.suite.ui.theme.PrimaryPurple
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 紧凑型闹钟方块卡片 (占半屏宽，展示最快触发的最多两个闹钟，带今天/明天/周X标记)
 * upcomingAlarms: (闹钟, 下次触发时间戳) 列表
 */
@Composable
fun DashboardAlarmCard(
    upcomingAlarms: List<Pair<AlarmEntity, Long>>,
    nowMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 第一行：时钟图标（开关操作请进入闹钟页）
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(17.dp)
                )
            }

            // 第二行：闹钟列表（最多显示两行，带今天/明天/周X微小暗标）
            if (upcomingAlarms.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    upcomingAlarms.forEach { (alarm, triggerAtMillis) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(Locale.ROOT, "%02d:%02d", alarm.hour, alarm.minute),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextWhitePrimary.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = getAlarmDayTag(triggerAtMillis, nowMillis),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextWhiteSecondary.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "--:--",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextWhiteSecondary.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/**
 * 计算闹钟下一次响铃日标签：今天 / 明天 / 周X（重复闹钟最远 7 天内）
 */
private fun getAlarmDayTag(triggerTimeMillis: Long, nowMillis: Long): String {
    fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // 四舍五入以吸收夏令时切换日的 23h/25h
    val dayDiff = Math.round((startOfDay(triggerTimeMillis) - startOfDay(nowMillis)) / 86_400_000.0)
    return when (dayDiff) {
        0L -> "今天"
        1L -> "明天"
        else -> SimpleDateFormat("EEE", Locale.CHINESE).format(Date(triggerTimeMillis))
    }
}
