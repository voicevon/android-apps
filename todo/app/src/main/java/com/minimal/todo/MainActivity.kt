package com.minimal.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.minimal.todo.ui.TodoListScreen
import com.minimal.todo.ui.theme.MinimalTodoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinimalTodoTheme {
                TodoListScreen()
            }
        }
    }
}
