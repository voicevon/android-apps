package com.feng.suite.feature.clock.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

/**
 * 闹钟数据实体
 *
 * @param daysBitmask 位掩码表示重复日：
 *   bit 0 (1): 周日 (SUNDAY)
 *   bit 1 (2): 周一 (MONDAY)
 *   bit 2 (4): 周二 (TUESDAY)
 *   bit 3 (8): 周三 (WEDNESDAY)
 *   bit 4 (16): 周四 (THURSDAY)
 *   bit 5 (32): 周五 (FRIDAY)
 *   bit 6 (64): 周六 (SATURDAY)
 *   0: 仅响一次 (一次性闹钟)
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysBitmask: Int = 0,
    val label: String = "",
    val isVibrate: Boolean = true,
    val snoozeMinutes: Int = 10
) {
    /**
     * 判断周几是否激活 (Calendar.SUNDAY=1 ... SATURDAY=7)
     */
    fun isDayEnabled(calendarDayOfWeek: Int): Boolean {
        val bit = 1 shl (calendarDayOfWeek - 1)
        return (daysBitmask and bit) != 0
    }

    /**
     * 重复类型描述文字
     */
    fun getRepeatDaysSummary(): String {
        if (daysBitmask == 0) return "仅响一次"
        if (daysBitmask == 0b1111111) return "每天"
        // 0b0111110 = 周一至周五 (bit 1..5)
        if (daysBitmask == 0b0111110) return "工作日 (周一至周五)"
        // 0b1000001 = 周日(bit 0) 与 周六(bit 6)
        if (daysBitmask == 0b1000001) return "周末"

        val dayNames = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val activeDays = mutableListOf<String>()
        for (i in 0..6) {
            if ((daysBitmask and (1 shl i)) != 0) {
                activeDays.add(dayNames[i])
            }
        }
        return activeDays.joinToString(", ")
    }

    /**
     * 计算下一个实际触发的毫秒时间戳
     */
    fun calculateNextTriggerTime(fromTimeMillis: Long = System.currentTimeMillis()): Long {
        val safeFromTime = fromTimeMillis + 2000L // 预留 2 秒安全裕度，防止同一分钟毫秒抖动导致的过去时间误判
        val calendar = Calendar.getInstance().apply {
            timeInMillis = safeFromTime
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (daysBitmask == 0) {
            // 一次性闹钟：如果设置的时间在过去，则排期为明天的这个时间
            if (calendar.timeInMillis <= safeFromTime) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        // 重复闹钟：寻找未来最近的匹配星期几
        for (i in 0..7) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (isDayEnabled(dayOfWeek)) {
                if (calendar.timeInMillis > safeFromTime) {
                    return calendar.timeInMillis
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
