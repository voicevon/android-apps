package com.minimal.todo.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minimal.todo.data.TodoItem
import com.minimal.todo.ui.components.AddEditTodoDialog
import com.minimal.todo.ui.components.ColorFilterBar
import com.minimal.todo.ui.components.TodoCard
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel = viewModel()
) {
    val allTodos by viewModel.allTodos.collectAsState()
    val filteredTodos by viewModel.filteredTodos.collectAsState()
    val selectedColorHex by viewModel.selectedColorFilter.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }

    // 拖拽排序状态
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val activeCount = allTodos.count { !it.isCompleted }
    val completedCount = allTodos.size - activeCount

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "冯氏 Todo",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (allTodos.isNotEmpty()) {
                            Text(
                                text = "$activeCount 个待办, $completedCount 个已完成",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加待办"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. 紧凑型颜色分类过滤栏 (白色全选 + 4色过滤)
            ColorFilterBar(
                selectedColorHex = selectedColorHex,
                onSelectColor = { color ->
                    viewModel.setColorFilter(color)
                }
            )

            // 2. 待办列表内容
            Box(modifier = Modifier.fillMaxSize()) {
                if (filteredTodos.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = "Empty",
                            modifier = Modifier.size(76.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedColorHex != null) "该颜色标签下暂无待办事项" else "暂无待办事项！",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedColorHex != null) "点击上方 \"全部\" 查看所有事项" else "点击右下角 \"+\" 快速创建待办",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(
                            items = filteredTodos,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isDragging = index == draggingIndex
                            val itemHeightPx = 200f // 预估项高度像素阈值

                            Box(
                                modifier = Modifier
                                    .zIndex(if (isDragging) 10f else 1f)
                                    .offset {
                                        if (isDragging) IntOffset(0, dragOffsetY.roundToInt())
                                        else IntOffset.Zero
                                    }
                                    .pointerInput(filteredTodos) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggingIndex = index
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y

                                                val targetIndex = (index + (dragOffsetY / itemHeightPx).roundToInt())
                                                    .coerceIn(0, filteredTodos.size - 1)

                                                if (targetIndex != draggingIndex && targetIndex in filteredTodos.indices) {
                                                    viewModel.reorderItems(draggingIndex, targetIndex)
                                                    draggingIndex = targetIndex
                                                    dragOffsetY = 0f
                                                }
                                            },
                                            onDragEnd = {
                                                draggingIndex = -1
                                                dragOffsetY = 0f
                                            },
                                            onDragCancel = {
                                                draggingIndex = -1
                                                dragOffsetY = 0f
                                            }
                                        )
                                    }
                            ) {
                                TodoCard(
                                    todo = item,
                                    onToggleComplete = { viewModel.toggleCompleted(item) },
                                    onClick = { editingTodo = item },
                                    onDelete = {
                                        viewModel.deleteTodo(item)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "已删除 \"${item.title}\"",
                                                actionLabel = "撤销",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreTodo(item)
                                            }
                                        }
                                    },
                                    onMoveUp = if (index > 0) {
                                        { viewModel.moveTodoUp(item) }
                                    } else null,
                                    onMoveDown = if (index < filteredTodos.size - 1) {
                                        { viewModel.moveTodoDown(item) }
                                    } else null
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(84.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditTodoDialog(
            initialTodo = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, hasReminder, reminderTime, colorHex ->
                viewModel.addTodo(
                    title = title,
                    description = desc,
                    hasReminder = hasReminder,
                    reminderTime = reminderTime,
                    colorHex = colorHex
                )
                showAddDialog = false
            }
        )
    }

    editingTodo?.let { targetTodo ->
        AddEditTodoDialog(
            initialTodo = targetTodo,
            onDismiss = { editingTodo = null },
            onConfirm = { title, desc, hasReminder, reminderTime, colorHex ->
                viewModel.updateTodo(
                    targetTodo.copy(
                        title = title,
                        description = desc,
                        hasReminder = hasReminder,
                        reminderTime = reminderTime,
                        colorHex = colorHex
                    )
                )
                editingTodo = null
            }
        )
    }
}
