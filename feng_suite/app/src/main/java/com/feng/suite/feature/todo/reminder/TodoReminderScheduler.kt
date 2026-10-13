package com.feng.suite.feature.todo.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.feng.suite.feature.todo.data.TodoDatabase
import com.feng.suite.feature.todo.data.TodoItem

/**
 * Todo 到点提醒调度：按 Todo 当前状态设置或取消 AlarmManager 定时广播
 */
class TodoReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 开启提醒、未完成且提醒时间在未来 → 设置；否则取消
     */
    fun apply(todo: TodoItem) {
        val reminderTime = todo.reminderTime
        if (!todo.hasReminder || todo.isCompleted || reminderTime == null ||
            reminderTime <= System.currentTimeMillis()
        ) {
            cancel(todo.id)
            return
        }

        val pendingIntent = buildPendingIntent(todo.id, PendingIntent.FLAG_UPDATE_CURRENT)!!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        }
    }

    fun cancel(todoId: Long) {
        val pendingIntent = buildPendingIntent(todoId, PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * 开机、应用升级、系统时间/时区变化后恢复全部待触发提醒（AlarmManager 定时会被系统清空）
     */
    suspend fun restoreAll() {
        TodoDatabase.getDatabase(context).todoDao().getPendingReminders().forEach { apply(it) }
    }

    private fun buildPendingIntent(todoId: Long, flag: Int): PendingIntent? {
        val intent = Intent(context, TodoReminderReceiver::class.java)
            .setAction(TodoReminderReceiver.ACTION_TODO_REMINDER)
            .putExtra(TodoReminderReceiver.EXTRA_TODO_ID, todoId)
        return PendingIntent.getBroadcast(
            context,
            todoId.toInt(),
            intent,
            flag or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
