package com.fossify.clock.data

import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    suspend fun getEnabledAlarms(): List<AlarmEntity> {
        return alarmDao.getEnabledAlarms()
    }

    suspend fun getAlarmById(id: Long): AlarmEntity? {
        return alarmDao.getAlarmById(id)
    }

    suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.deleteAlarm(alarm)
    }
}
