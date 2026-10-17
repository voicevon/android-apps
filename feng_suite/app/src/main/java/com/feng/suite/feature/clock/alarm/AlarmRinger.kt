package com.feng.suite.feature.clock.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.feng.suite.feature.clock.receiver.AlarmReceiver
import com.feng.suite.feature.clock.ui.screens.RingingActivity

/**
 * 闹钟前台响铃核心服务
 *
 * 运行于 :alarm 独立保护子进程。
 * 通过 startForeground 挂载媒体播放前台服务与最高优先级通知，
 * 彻底防止 Android 深度休眠 (Doze Mode) 及厂商激进策略冻结进程与掐断铃声。
 */
class AlarmRinger : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        if (action == ACTION_STOP) {
            stopRingingAndSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L
        val hour = intent?.getIntExtra(AlarmReceiver.EXTRA_ALARM_HOUR, 0) ?: 0
        val minute = intent?.getIntExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, 0) ?: 0
        val label = intent?.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: ""
        val isVibrate = intent?.getBooleanExtra(AlarmReceiver.EXTRA_IS_VIBRATE, true) ?: true
        val snoozeMinutes = intent?.getIntExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, 10) ?: 10

        // 1. 创建前台通知并在 5 秒内启动前台服务（满足 Android 8.0+ / 14+ 严格时限）
        val notification = buildForegroundNotification(alarmId, hour, minute, label, isVibrate, snoozeMinutes)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // 2. 唤醒点亮屏幕并拉起全屏 RingingActivity
        turnOnScreenAndLaunchActivity(alarmId, hour, minute, label, isVibrate, snoozeMinutes)

        // 3. 循环播放高优先级闹钟铃声与振动
        startSound(this)
        if (isVibrate) {
            startVibration()
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "FengSuite:AlarmRingerWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 最多持有 10 分钟唤醒锁安全阀
            }
        } catch (_: Exception) {
        }
    }

    private fun turnOnScreenAndLaunchActivity(
        alarmId: Long,
        hour: Int,
        minute: Int,
        label: String,
        isVibrate: Boolean,
        snoozeMinutes: Int
    ) {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            @Suppress("DEPRECATION")
            val screenLock = pm?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                "FengSuite:ScreenWakeLock"
            )
            screenLock?.acquire(3000L) // 瞬间点亮屏幕
        } catch (_: Exception) {
        }

        try {
            val activityIntent = Intent(this, RingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, hour)
                putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, minute)
                putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
                putExtra(AlarmReceiver.EXTRA_IS_VIBRATE, isVibrate)
                putExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            }
            startActivity(activityIntent)
        } catch (_: Exception) {
        }
    }

    private fun buildForegroundNotification(
        alarmId: Long,
        hour: Int,
        minute: Int,
        label: String,
        isVibrate: Boolean,
        snoozeMinutes: Int
    ): Notification {
        val channelId = CHANNEL_ID
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            val channel = NotificationChannel(
                channelId,
                "闹钟提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "精确响铃与全屏闹钟通知"
                enableVibration(isVibrate)
                setSound(alertUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val timeFormatted = String.format("%02d:%02d", hour, minute)
        val titleText = if (label.isNotBlank()) label else "闹钟响铃"

        // 全屏启动 Intent
        val fullScreenIntent = Intent(this, RingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, hour)
            putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, minute)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmReceiver.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 贪睡 Action
        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmReceiver.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            (alarmId + 50000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 关闭 Action
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            (alarmId + 60000).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titleText)
            .setContentText("$timeFormatted - 点击或滑动处理")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "贪睡 ($snoozeMinutes 分钟)", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "关闭", dismissPendingIntent)
            .build()
    }

    private fun startSound(context: Context) {
        val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        var mediaPlayerStarted = false
        if (alertUri != null) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                            .build()
                    )
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                    setDataSource(context, alertUri)
                    isLooping = true
                    setVolume(1.0f, 1.0f)
                    prepare()
                    start()
                }
                mediaPlayerStarted = true
            } catch (_: Exception) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

        if (!mediaPlayerStarted && alertUri != null) {
            try {
                ringtone = RingtoneManager.getRingtone(context, alertUri)?.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                            .build()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        isLooping = true
                    }
                    play()
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 800, 800, 800, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {
        }
    }

    private fun stopRingingAndSelf() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {
        }

        try {
            ringtone?.stop()
            ringtone = null
        } catch (_: Exception) {
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (_: Exception) {
        }

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (_: Exception) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopRingingAndSelf()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.feng.suite.feature.clock.alarm.ACTION_START"
        const val ACTION_STOP = "com.feng.suite.feature.clock.alarm.ACTION_STOP"
        const val CHANNEL_ID = "fossify_alarm_channel_v2"
        const val NOTIFICATION_ID = 10001

        fun start(
            context: Context,
            alarmId: Long,
            hour: Int,
            minute: Int,
            label: String,
            isVibrate: Boolean,
            snoozeMinutes: Int
        ) {
            val intent = Intent(context, AlarmRinger::class.java).apply {
                action = ACTION_START
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, hour)
                putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, minute)
                putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
                putExtra(AlarmReceiver.EXTRA_IS_VIBRATE, isVibrate)
                putExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun start(context: Context, isVibrate: Boolean = true) {
            start(context, -1L, 0, 0, "", isVibrate, 10)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlarmRinger::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
