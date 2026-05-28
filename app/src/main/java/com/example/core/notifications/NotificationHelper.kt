package com.example.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS_ID = "dailyos_reminders"
        const val CHANNEL_FOCUS_ID = "dailyos_focus_timer"
        const val NOTIFICATION_ID_DAILY_REVIEW = 1001
        const val NOTIFICATION_ID_HABIT = 1002
        const val NOTIFICATION_ID_FOCUS = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersName = "Daily Reminders & Reviews"
            val remindersDesc = "Daily notifications to plan your day, log your habits, or review."
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                remindersName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = remindersDesc
            }

            val focusName = "Focus Timer Alerts"
            val focusDesc = "Alerts you when a Pomodoro focus session finishes."
            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS_ID,
                focusName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = focusDesc
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(remindersChannel)
            manager.createNotificationChannel(focusChannel)
        }
    }

    fun showDailyReviewReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for your Daily Review! 🌙")
            .setContentText("Reflect on today's logs, answer the gratitude prompt, and prepare for tomorrow.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_DAILY_REVIEW, builder.build())
        }
    }

    fun showFocusCompletedNotification(sessionTitle: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_FOCUS_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Focus Session Complete! ⏱️")
            .setContentText("Phenomenal job! You finished: \"$sessionTitle\". Take a well-earned 5-minute break.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_FOCUS, builder.build())
        }
    }

    fun showHabitReminder(habitName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.checkbox_on_background)
            .setContentTitle("Keep the streak alive! 🔥")
            .setContentText("It's time for your daily ritual: \"$habitName\". Consistency is progress.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_HABIT, builder.build())
        }
    }
}
