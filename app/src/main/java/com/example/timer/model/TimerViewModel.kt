package com.example.timer.model

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val TIME_UNIT_MAX = 59
private const val SECOND_IN_MILLIS = 1000L

class TimerViewModel : ViewModel() {
    private var _hours = MutableLiveData(0)
    private var _minutes = MutableLiveData(0)
    private var _seconds = MutableLiveData(0)

    private var _timerStarted = MutableLiveData(false)
    private var _timerResumed = MutableLiveData(false)
    private var _timerIsUp = MutableLiveData(false)

    val hours: LiveData<Int> = _hours
    val minutes: LiveData<Int> = _minutes
    val seconds: LiveData<Int> = _seconds
    val timerStarted: LiveData<Boolean> = _timerStarted
    val timerResumed: LiveData<Boolean> = _timerResumed
    val timerIsUp: LiveData<Boolean> = _timerIsUp

    private lateinit var timer: CountDownTimer
    private var hoursTemp = 0
    private var minutesTemp = 0
    private var secondsTemp = 0

    fun secondsTotal() = _hours.value!! * 3600 + _minutes.value!! * 60 + _seconds.value!!

    fun startTimer(hours: Int, minutes: Int, seconds: Int) {
        initialize(hours, minutes, seconds)

        // since there's no need to start a timer if the total time is zero
        if (secondsTotal() == 0) return

        saveTimeValues() // to restore them when the timer's stopped

        timer = createTimer().start()
        _timerStarted.value = true
        _timerResumed.value = true
        _timerIsUp.value = false
    }

    fun pauseTimer() {
        timer.cancel()
        _timerResumed.value = false
    }

    fun resumeTimer() {
        timer = createTimer().start()
        _timerResumed.value = true
    }

    fun stopTimer() {
        if (_timerStarted.value!!) {
            timer.cancel()
        }
        resetTimer()
    }

    private fun initialize(hours: Int, minutes: Int, seconds: Int) {
        _hours.value = hours
        _minutes.value = minutes
        _seconds.value = seconds
    }

    private fun resetTimer() {
        /* _timerStarted variable must be set to false first, then all the other observable data,
         * because all time unit observers depend on the state of the _timerStarted variable.
         * See the TimerService.addObserversToModel() method.
         */
        _timerStarted.value = false
        _timerResumed.value = false
        _hours.value = hoursTemp
        _minutes.value = minutesTemp
        _seconds.value = secondsTemp
    }

    // Save the values to restore them when timer's up or stopped in resetTimer() method.
    private fun saveTimeValues() {
        hoursTemp = _hours.value!!
        minutesTemp = _minutes.value!!
        secondsTemp = _seconds.value!!
    }

    private fun createTimer(): CountDownTimer {
        /* Added one second to millisInFuture var of the timer and skipped one-second countdown
        to add a delay when starting the timer so that seconds value wouldn't instantly decrease
        one second down when the timer has started. */
        return object: CountDownTimer(secondsTotal() * SECOND_IN_MILLIS, SECOND_IN_MILLIS) {
            var onStart = true
            override fun onTick(millisUntilFinished: Long) {
                if (onStart) {
                    onStart = false
                } else {
                    countDownSecond()
                }
            }
            override fun onFinish() {
                resetTimer()
                _timerIsUp.value = true
            }
        }
    }

    private fun countDownSecond() {
        if (_seconds.value != 0) {
            _seconds.value = _seconds.value!! - 1
        } else {
            _seconds.value = TIME_UNIT_MAX
            countDownMinute()
        }
    }
    private fun countDownMinute() {
        if (_minutes.value != 0) {
            _minutes.value = _minutes.value!! - 1
        } else {
            _minutes.value = TIME_UNIT_MAX
            countDownHour()
        }
    }
    private fun countDownHour() {
        if (_hours.value != 0) {
            _hours.value = _hours.value!! - 1
        }
    }
}