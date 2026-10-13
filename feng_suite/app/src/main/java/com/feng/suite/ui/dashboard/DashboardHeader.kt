package com.feng.suite.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.ui.theme.CardBorderDark
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 顶部 Header：左侧两行日期 + 中右双外地时区芯片 + 最右上角主菜单 (三道杠)
 * 日期连续点击 7 次（彩蛋）与菜单「设置」均触发 onOpenSettings
 */
@Composable
fun DashboardHeader(
    nowMillis: Long,
    zone1Id: String,
    zone1Name: String,
    zone2Id: String,
    zone2Name: String,
    onZoneClick: (slotIndex: Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDayText = remember(nowMillis) { getMonthDayString(nowMillis) }
    val weekdayText = remember(nowMillis) { getWeekdayString(nowMillis) }
    val zone1TimeText = remember(nowMillis, zone1Id) { getWorldTimeString(zone1Id, nowMillis) }
    val zone2TimeText = remember(nowMillis, zone2Id) { getWorldTimeString(zone2Id, nowMillis) }

    // 隐藏 7 连击进入 MQTT 控制中心菜单
    var dateClickCount by remember { mutableIntStateOf(0) }
    var lastDateClickTime by remember { mutableLongStateOf(0L) }
    var showMainMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧日期：两行显示（10月12日 / 星期一），连续点击 7 次依然作为彩蛋触发设置
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val now = System.currentTimeMillis()
                    if (now - lastDateClickTime < 500L) {
                        dateClickCount++
                        if (dateClickCount >= 7) {
                            dateClickCount = 0
                            onOpenSettings()
                        }
                    } else {
                        dateClickCount = 1
                    }
                    lastDateClickTime = now
                }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = monthDayText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )
            Text(
                text = weekdayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhiteSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 中右侧双外地时区胶囊（往左移一点，紧邻最右侧菜单按钮，上下两行排列）
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            ZoneChip(
                cityName = formatZoneCityName(zone1Name),
                timeText = zone1TimeText,
                onClick = { onZoneClick(1) }
            )
            ZoneChip(
                cityName = formatZoneCityName(zone2Name),
                timeText = zone2TimeText,
                onClick = { onZoneClick(2) }
            )
        }

        // 最右上角：主菜单按钮 (三个横杠图标)
        Box {
            IconButton(
                onClick = { showMainMenu = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                    .border(1.dp, CardBorderDark, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "主菜单",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMainMenu,
                onDismissRequest = { showMainMenu = false },
                offset = DpOffset(x = 0.dp, y = 8.dp),
                modifier = Modifier
                    .background(Color(0xFF1E293B))
                    .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(18.dp)
                            )
                            Text("设置", color = TextWhitePrimary, fontSize = 14.sp)
                        }
                    },
                    onClick = {
                        showMainMenu = false
                        onOpenSettings()
                    }
                )

                HorizontalDivider(color = CardBorderDark)

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(18.dp)
                            )
                            Text("关于", color = TextWhitePrimary, fontSize = 14.sp)
                        }
                    },
                    onClick = {
                        showMainMenu = false
                        onOpenAbout()
                    }
                )
            }
        }
    }
}

private fun getWorldTimeString(zoneId: String, nowMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone(zoneId)
    }
    return sdf.format(Date(nowMillis))
}

private fun getMonthDayString(nowMillis: Long): String {
    val sdf = SimpleDateFormat("M月d日", Locale.CHINESE)
    return sdf.format(Date(nowMillis))
}

private fun getWeekdayString(nowMillis: Long): String {
    val sdf = SimpleDateFormat("EEEE", Locale.CHINESE)
    return sdf.format(Date(nowMillis))
}

/**
 * 城市名称限制 3 个汉字（老外地名过长时截取前3个字）
 */
private fun formatZoneCityName(name: String): String {
    return if (name.length > 3) name.substring(0, 3) else name
}

/**
 * 顶部外地时区芯片胶囊
 */
@Composable
private fun ZoneChip(
    cityName: String,
    timeText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF2563EB).copy(alpha = 0.18f))
            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$cityName $timeText",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF93C5FD),
            maxLines = 1
        )
    }
}
