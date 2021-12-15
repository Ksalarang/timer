package com.example.timer.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.timer.base.*
import com.example.timer.model.TimerViewModel

private const val NOTIFICATION_ID = 100

class TimerService : LifecycleService() {
    private val timerModel = TimerViewModel()
    private lateinit var notificationManager: NotificationManager
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var timerNotification: TimerNotification

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.apply {
            when (getStringExtra(EXTRA_COMMAND)) {
                COMMAND_START -> {
                    val h = getIntExtra(EXTRA_HOURS, 0)
                    val m = getIntExtra(EXTRA_MINUTES, 0)
                    val s = getIntExtra(EXTRA_SECONDS, 0)

                    timerNotification = TimerNotification(this@TimerService)
                        .also { it.initializeTimeUnits(h, m, s) }

                    addObserversToModel()
                    timerModel.startTimer(h, m, s)

                    startForeground(NOTIFICATION_ID, timerNotification.getNotification())
                }
                COMMAND_PAUSE -> timerModel.pauseTimer()
                COMMAND_RESUME -> timerModel.resumeTimer()
                COMMAND_STOP -> stopSelf()

                COMMAND_SEND_DATA -> {
                    if (isTimerStarted()) {
                        val allData = Intent(ACTION_TIMER_STATE_CHANGED)
                            .putExtra(EXTRA_RESULT_CODE, ALL_DATA_RESULT_CODE)
                            .putExtra(EXTRA_SECONDS, timerModel.seconds.value)
                            .putExtra(EXTRA_MINUTES, timerModel.minutes.value)
                            .putExtra(EXTRA_HOURS, timerModel.hours.value)
                            .putExtra(EXTRA_TIMER_STARTED, timerModel.timerStarted.value)
                            .putExtra(EXTRA_TIMER_RESUMED, timerModel.timerResumed.value)

                        localBroadcastManager.sendBroadcast(allData)
                    } else {
                        stopSelf()
                    }
                }
                // TODO: why do I need this?
//                COMMAND_DISMISS_NOTIFICATION -> notificationManager.cancel(NOTIFICATION_ID)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (timerModel.timerStarted.value!!) timerModel.stopTimer()
        super.onDestroy()
    }

    private fun addObserversToModel() {
        timerModel.hours.observe(this) { hours ->
            val tickIntent = Intent(ACTION_TIMER_STATE_CHANGED)
                .putExtra(EXTRA_RESULT_CODE, HOURS_RESULT_CODE)
                .putExtra(EXTRA_HOURS, hours)

            localBroadcastManager.sendBroadcast(tickIntent)

            timerNotification.hours = hours

            if (isTimerStarted()) {
                notificationManager.notify(NOTIFICATION_ID, timerNotification.getUpdatedNotification())
            }
        }

        timerModel.minutes.observe(this) { minutes ->
            val tickIntent = Intent(ACTION_TIMER_STATE_CHANGED)
                .putExtra(EXTRA_RESULT_CODE, MINUTES_RESULT_CODE)
                .putExtra(EXTRA_MINUTES, minutes)

            localBroadcastManager.sendBroadcast(tickIntent)

            timerNotification.minutes = minutes

            if (isTimerStarted()) {
                notificationManager.notify(NOTIFICATION_ID, timerNotification.getUpdatedNotification())
            }
        }

        timerModel.seconds.observe(this) { seconds ->
            val tickIntent = Intent(ACTION_TIMER_STATE_CHANGED)
                .putExtra(EXTRA_RESULT_CODE, SECONDS_RESULT_CODE)
                .putExtra(EXTRA_SECONDS, seconds)

            localBroadcastManager.sendBroadcast(tickIntent)

            timerNotification.seconds = seconds

            if (isTimerStarted()) {
                notificationManager.notify(NOTIFICATION_ID, timerNotification.getUpdatedNotification())
            }
        }

        timerModel.timerStarted.observe(this) { started ->
            val timerStartedIntent = Intent(ACTION_TIMER_STATE_CHANGED)
                .putExtra(EXTRA_RESULT_CODE, TIMER_STARTED_RESULT_CODE)
                .putExtra(EXTRA_TIMER_STARTED, started)

            localBroadcastManager.sendBroadcast(timerStartedIntent)

            if (!started) { stopSelf() }
        }

        timerModel.timerResumed.observe(this) { resumed ->
            val timerResumedIntent = Intent(ACTION_TIMER_STATE_CHANGED)
                .putExtra(EXTRA_RESULT_CODE, TIMER_RESUMED_RESULT_CODE)
                .putExtra(EXTRA_TIMER_RESUMED, resumed)

            localBroadcastManager.sendBroadcast(timerResumedIntent)
        }

        timerModel.timerIsUp.observe(this) { timerIsUp ->
            if (timerIsUp) {
                notificationManager.notify(NOTIFICATION_ID, timerNotification.getNotificationWhenFinished())
            }
        }
    }

    private fun isTimerStarted() = timerModel.timerStarted.value!!
}