package com.example.tasking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TaskService : Service() {

    private var timer: CountDownTimer? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "task_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_TIMER_UPDATE = "com.example.tasking.TIMER_UPDATE"
        const val EXTRA_TIME_LEFT = "time_left"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Sprint in progress...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startTimer()

        return START_NOT_STICKY
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()

                // Update system notification text on each tick
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification("Sprint Active: $secondsLeft s remaining")
                )

                // Broadcast updated seconds back to MainActivity
                val broadcastIntent = Intent(ACTION_TIMER_UPDATE).apply {
                    putExtra(EXTRA_TIME_LEFT, secondsLeft)
                }
                sendBroadcast(broadcastIntent)
            }

            override fun onFinish() {
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification("Sprint Complete!")
                )
                val broadcastIntent = Intent(ACTION_TIMER_UPDATE).apply {
                    putExtra(EXTRA_TIME_LEFT, 0)
                }
                sendBroadcast(broadcastIntent)
                stopSelf()
            }
        }.start()
    }

    private fun buildNotification(contentText: String): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tasking Engine")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Mandates Heads-Up Banner
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
    }

    // In TaskService.kt -> createNotificationChannel()
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tasking Sprint Channel",
                NotificationManager.IMPORTANCE_HIGH // Drops banner from top of screen
            ).apply {
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }




    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}