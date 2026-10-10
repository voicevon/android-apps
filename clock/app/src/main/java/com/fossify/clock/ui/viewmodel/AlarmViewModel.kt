package com.fossify.clock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fossify.clock.alarm.AlarmScheduler
import com.fossify.clock.data.AlarmDatabase
import com.fossify.clock.data.AlarmEntity
import com.fossify.clock.data.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository
    private val scheduler: AlarmScheduler = AlarmScheduler(application)

    val alarms: StateFlow<List<AlarmEntity>>

    val nextAlarmCountdown: StateFlow<String?>

    init {
        val db = AlarmDatabase.getInstance(application)
        repository = AlarmRepository(db.alarmDao())

        alarms = repository.allAlarms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        nextAlarmCountdown = alarms.map { alarmList ->
            AlarmScheduler.formatNextAlarmCountdown(alarmList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                scheduler.schedule(updated)
            } else {
                scheduler.cancel(updated.id)
            }
        }
    }

    fun saveAlarm(
        id: Long = 0,
        hour: Int,
        minute: Int,
        label: String,
        daysBitmask: Int,
        isVibrate: Boolean,
        snoozeMinutes: Int
    ) {
        viewModelScope.launch {
            val alarm = AlarmEntity(
                id = id,
                hour = hour,
                minute = minute,
                isEnabled = true,
                daysBitmask = daysBitmask,
                label = label.trim(),
                isVibrate = isVibrate,
                snoozeMinutes = snoozeMinutes
            )

            if (id == 0L) {
                val newId = repository.insertAlarm(alarm)
                scheduler.schedule(alarm.copy(id = newId))
            } else {
                repository.updateAlarm(alarm)
                scheduler.schedule(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            scheduler.cancel(alarm.id)
            repository.deleteAlarm(alarm)
        }
    }
}
