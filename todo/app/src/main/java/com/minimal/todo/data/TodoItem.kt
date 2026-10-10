package com.minimal.todo.data

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
    val colorHex: String = "#3D5AFE",
    val createdAt: Long = System.currentTimeMillis()
)
