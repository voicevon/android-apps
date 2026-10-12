package com.minimal.todo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minimal.todo.ui.theme.TagColors

@Composable
fun ColorFilterBar(
    selectedColorHex: String?,
    onSelectColor: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // 5 个圆形紧凑并排：白色 (代表全部/全选) + 蓝、绿、黄、红 4 种颜色
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 全部 (白色圆点，不带文字)
        val isAllSelected = selectedColorHex == null
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (isAllSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color.Transparent
                )
                .border(
                    width = if (isAllSelected) 2.dp else 0.dp,
                    color = if (isAllSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onSelectColor(null) },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCCCCCC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isAllSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "全选",
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 2. 蓝、绿、黄、红 4 种颜色
        TagColors.forEach { colorHex ->
            val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
            val parsedColor = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
                .getOrDefault(Color.Gray)

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) parsedColor.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) parsedColor else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable {
                        if (isSelected) onSelectColor(null) else onSelectColor(colorHex)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(parsedColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已选择",
                            tint = if (colorHex == "#FFEA00" || colorHex == "#FFD600") Color.Black else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
