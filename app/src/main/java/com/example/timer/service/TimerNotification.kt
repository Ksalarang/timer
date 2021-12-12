package com.example.timer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.timer.base.*
import com.example.timer.R

class TimerNotification(private val parentContext: Context) {
    var hours = 0
    var minutes = 0
    var seconds = 0

    private var notificationBuilder: NotificationCompat.Builder
    private var notification: Notification

    init {
        notificationBuilder = buildNotificationBuilder()
        notification = notificationBuilder.build()
    }

    fun initializeTimeUnits(hours: Int, minutes: Int, seconds: Int) {
        this.hours = hours
        this.minutes = minutes
        this.seconds = seconds
    }

    fun getNotification() = notification

    fun getUpdatedNotification() = notificationBuilder.setContentTitle(getTitle()).build()

    private fun buildNotificationBuilder(): NotificationCompat.Builder {
        val intentMainActivity = Intent(parentContext, MainActivity::class.java)

        val pIntentMainActivity = PendingIntent.getActivity(parentContext, 0, intentMainActivity, 0)

        val intentStopService = Intent(parentContext, TimerService::class.java)
            .putExtra(EXTRA_COMMAND, COMMAND_STOP)

        val pIntentStopService = PendingIntent.getService(
            parentContext,
            0,
            intentStopService,
            PendingIntent.FLAG_CANCEL_CURRENT)

        val actionStop = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            parentContext.getString(R.string.stop),
            pIntentStopService
        ).build()

        return NotificationCompat.Builder(parentContext, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getTitle())
            .setContentText(parentContext.getString(R.string.timer_running))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setContentIntent(pIntentMainActivity)
            .addAction(actionStop)
            .setSilent(true)
            .setShowWhen(false)
            .setAutoCancel(true)
    }

    fun getNotificationWhenFinished(): Notification {
        return notificationBuilder
            .setChannelId(TIMER_END_CHANNEL_ID)
            .setContentTitle("${parentContext.getString(R.string.time_up)} - ${getTitle()}")
            .setContentText("")
            .setSilent(false)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(LongArray(2) { 1000 })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .clearActions()
            .build()
    }

    private fun getTitle(): String {
        var title = ""

        title.apply {
            if (hours > 0) {
                title += formatTimeUnit(hours, ":")
            }
            title += formatTimeUnit(minutes, ":")
            title += formatTimeUnit(seconds)
        }

        return title
    }

    private fun formatTimeUnit(timeUnit: Int, divider: String = ""): String {
        return if (timeUnit < 10) {
            "0$timeUnit$divider"
        } else {
            "$timeUnit$divider"
        }
    }
}