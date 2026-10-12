package com.feng.suite.feature.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val hasReminder: Boolean = false,
    val reminderTime: Long? = null,
    val colorHex: String = "#2979FF",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
