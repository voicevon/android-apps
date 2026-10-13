package com.feng.suite.feature.todo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feng.suite.feature.todo.data.TodoItem
import com.feng.suite.ui.theme.BluePrimary
import com.feng.suite.ui.theme.CardBackgroundDark
import com.feng.suite.ui.theme.CardBorderDark
import com.feng.suite.ui.theme.TextWhitePrimary
import com.feng.suite.ui.theme.TextWhiteSecondary

@Composable
fun DashboardTodoCard(
    selectedColor: String?,
    onSelectColor: (String?) -> Unit,
    todoList: List<TodoItem>,
    hasAnyTodos: Boolean,
    onToggleComplete: (TodoItem) -> Unit,
    onAddTodo: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            // 第一行：纯颜色过滤器 + 最右侧胶囊加号新建任务按钮
            ColorFilterBar(
                selectedColorHex = selectedColor,
                onSelectColor = onSelectColor,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onAddTodo)
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新建待办任务",
                            tint = TextWhitePrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (todoList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!hasAnyTodos) "暂无待办事项，点击添加" else "该分类下暂无待办事项",
                        fontSize = 13.sp,
                        color = TextWhiteSecondary
                    )
                }
            } else {
                todoList.forEach { todo ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(todo.colorHex))
                    } catch (_: Exception) {
                        BluePrimary
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 32dp 点击区包裹 18dp 可视勾选框，降低误触卡片跳转的概率
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleComplete(todo) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(1.5.dp, color, RoundedCornerShape(5.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(3.dp))

                        Text(
                            text = todo.title,
                            fontSize = 14.sp,
                            color = TextWhitePrimary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
