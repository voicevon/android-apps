package com.feng.suite.feature.todo.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.feng.suite.MainActivity
import com.feng.suite.feature.todo.data.TodoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Todo 提醒到点：重新读取数据库确认仍需提醒后，弹出高优先级横幅通知
 */
class TodoReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TODO_REMINDER) return
        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        if (todoId <= 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todo = TodoDatabase.getDatabase(context).todoDao().getTodoById(todoId)
                if (todo != null && todo.hasReminder && !todo.isCompleted) {
                    showNotification(context, todoId, todo.title, todo.description)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, todoId: Long, title: String, description: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "待办提醒",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "待办事项到点提醒"
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            todoId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(description.ifBlank { "待办提醒" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        // 使用独立 tag，避免与闹钟通知（以 alarmId 为 id）冲突
        notificationManager.notify(NOTIFICATION_TAG, todoId.toInt(), notification)
    }

    companion object {
        const val ACTION_TODO_REMINDER = "com.feng.suite.feature.todo.ACTION_TODO_REMINDER"
        const val EXTRA_TODO_ID = "extra_todo_id"
        private const val CHANNEL_ID = "todo_reminder_channel"
        private const val NOTIFICATION_TAG = "todo_reminder"
    }
}
