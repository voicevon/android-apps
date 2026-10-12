package com.feng.suite.feature.todo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feng.suite.feature.todo.data.TodoDatabase
import com.feng.suite.feature.todo.data.TodoItem
import com.feng.suite.ui.theme.TagColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val todoDao = TodoDatabase.getDatabase(application).todoDao()

    private val _selectedColorFilter = MutableStateFlow<String?>(null)
    val selectedColorFilter: StateFlow<String?> = _selectedColorFilter.asStateFlow()

    val allTodos: StateFlow<List<TodoItem>> = todoDao.getAllTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredTodos: StateFlow<List<TodoItem>> = combine(allTodos, _selectedColorFilter) { list, color ->
        if (color == null) {
            val colorPriority = TagColors.mapIndexed { index, hex -> hex.lowercase() to index }.toMap()
            list.sortedWith(
                compareBy<TodoItem> { todo ->
                    colorPriority[todo.colorHex.lowercase()] ?: Int.MAX_VALUE
                }.thenBy { it.isCompleted }
                 .thenBy { it.orderIndex }
                 .thenByDescending { it.createdAt }
            )
        } else {
            list.filter { it.colorHex.equals(color, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setColorFilter(colorHex: String?) {
        _selectedColorFilter.value = colorHex
    }

    fun addTodo(
        title: String,
        description: String = "",
        hasReminder: Boolean = false,
        reminderTime: Long? = null,
        colorHex: String = "#2979FF"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val maxIndex = todoDao.getMaxOrderIndex() ?: 0
            val item = TodoItem(
                title = title.trim(),
                description = description.trim(),
                hasReminder = hasReminder,
                reminderTime = reminderTime,
                colorHex = colorHex,
                orderIndex = maxIndex + 1
            )
            todoDao.insertTodo(item)
        }
    }

    fun toggleCompleted(todo: TodoItem) {
        viewModelScope.launch {
            todoDao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            todoDao.updateTodo(todo)
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            todoDao.deleteTodo(todo)
        }
    }

    fun restoreTodo(todo: TodoItem) {
        viewModelScope.launch {
            todoDao.insertTodo(todo)
        }
    }

    /**
     * 上下拖拽交换或移动项，并持久化到数据库
     */
    fun reorderItems(fromIndex: Int, toIndex: Int) {
        val currentList = filteredTodos.value.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices || fromIndex == toIndex) {
            return
        }

        val movedItem = currentList.removeAt(fromIndex)
        currentList.add(toIndex, movedItem)

        // 重新分配 orderIndex 并批量持久化
        val updatedItems = currentList.mapIndexed { index, item ->
            item.copy(orderIndex = index)
        }

        viewModelScope.launch {
            todoDao.updateTodos(updatedItems)
        }
    }

    fun moveTodoUp(todo: TodoItem) {
        val currentList = filteredTodos.value
        val index = currentList.indexOfFirst { it.id == todo.id }
        if (index > 0) {
            reorderItems(index, index - 1)
        }
    }

    fun moveTodoDown(todo: TodoItem) {
        val currentList = filteredTodos.value
        val index = currentList.indexOfFirst { it.id == todo.id }
        if (index in 0 until currentList.size - 1) {
            reorderItems(index, index + 1)
        }
    }
}
