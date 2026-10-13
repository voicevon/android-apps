package com.feng.suite.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.ui.theme.BluePrimary
import com.feng.suite.ui.theme.CardBackgroundDark
import com.feng.suite.ui.theme.CardBorderDark
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 双外地时区切换选择弹窗
 * slotIndex: 1 / 2，对应看板顶部第一 / 第二个外地时区胶囊
 */
@Composable
fun WorldZonePickerDialog(
    slotIndex: Int,
    selectedZoneId: String,
    selectedZoneName: String,
    onSelect: (WorldZone) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredZones = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            SELECTABLE_WORLD_ZONES
        } else {
            SELECTABLE_WORLD_ZONES.filter { zone ->
                zone.name.lowercase().contains(q) ||
                zone.nameEn.lowercase().contains(q) ||
                zone.country.lowercase().contains(q) ||
                zone.zoneId.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择第 ${if (slotIndex == 1) "一" else "二"} 外地时区城市",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextWhitePrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 搜索输入框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "输入城市或国家搜索",
                            fontSize = 13.5.sp,
                            color = TextWhiteSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清空",
                                    tint = TextWhiteSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B),
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = CardBorderDark,
                        cursorColor = BluePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // 列表展示
                if (filteredZones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "未搜索到匹配城市\n支持输入中英文名称或国家",
                            fontSize = 13.sp,
                            color = TextWhiteSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                    ) {
                        items(filteredZones, key = { it.name + "_" + it.zoneId }) { zone ->
                            val isSelected = zone.zoneId == selectedZoneId && zone.name == selectedZoneName
                            val (timeStr, offsetStr) = getZoneTimeAndOffset(zone.zoneId)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.22f) else Color.Transparent)
                                    .clickable { onSelect(zone) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = zone.name,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) Color(0xFF67B0F8) else TextWhitePrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = zone.country,
                                            fontSize = 11.5.sp,
                                            color = TextWhiteSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = zone.nameEn,
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = timeStr,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color(0xFF67B0F8) else TextWhitePrimary
                                    )
                                    Text(
                                        text = offsetStr,
                                        fontSize = 11.sp,
                                        color = TextWhiteSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = Color(0xFF67B0F8), fontSize = 14.sp)
            }
        },
        containerColor = CardBackgroundDark,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun getZoneTimeAndOffset(zoneId: String): Pair<String, String> {
    val localTz = TimeZone.getDefault()
    val targetTz = TimeZone.getTimeZone(zoneId)
    val now = System.currentTimeMillis()

    val targetSdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = targetTz
    }
    val timeStr = targetSdf.format(Date(now))

    val localOffset = localTz.getOffset(now)
    val targetOffset = targetTz.getOffset(now)
    // 按分钟计算，支持 +5:30、+9:30 等半小时时区
    val diffMinutes = (targetOffset - localOffset) / (1000 * 60)
    val absHours = kotlin.math.abs(diffMinutes) / 60
    val absMinutes = kotlin.math.abs(diffMinutes) % 60
    val amount = if (absMinutes == 0) "${absHours}h" else "${absHours}h${absMinutes}m"

    val offsetStr = when {
        diffMinutes == 0 -> "与本地同时"
        diffMinutes > 0 -> "快 $amount"
        else -> "慢 $amount"
    }
    return Pair(timeStr, offsetStr)
}
