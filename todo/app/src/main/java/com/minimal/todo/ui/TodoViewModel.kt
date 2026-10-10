package com.minimal.todo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.minimal.todo.data.TodoDatabase
import com.minimal.todo.data.TodoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val todoDao = TodoDatabase.getDatabase(application).todoDao()

    val todos: StateFlow<List<TodoItem>> = todoDao.getAllTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTodo(
        title: String,
        description: String = "",
        hasReminder: Boolean = false,
        reminderTime: Long? = null,
        colorHex: String = "#3D5AFE"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val item = TodoItem(
                title = title.trim(),
                description = description.trim(),
                hasReminder = hasReminder,
                reminderTime = reminderTime,
                colorHex = colorHex
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
}
