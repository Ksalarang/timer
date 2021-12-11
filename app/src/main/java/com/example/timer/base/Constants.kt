package com.example.timer.base

const val CHANNEL_ID = "Timer channel id"
const val ACTION_TIMER_STATE_CHANGED = "com.example.timer.base.MainActivity.TIMER_STATE_CHANGED"

const val EXTRA_HOURS = "HOURS"
const val EXTRA_MINUTES = "MINUTES"
const val EXTRA_SECONDS = "SECONDS"
const val EXTRA_TIMER_STARTED = "TIMER STARTED"
const val EXTRA_TIMER_RESUMED = "TIMER RESUMED"

const val EXTRA_COMMAND = "COMMAND"
const val COMMAND_START = "START"
const val COMMAND_STOP = "STOP"
const val COMMAND_RESUME = "RESUME"
const val COMMAND_PAUSE = "PAUSE"
const val COMMAND_SEND_DATA = "SEND DATA"
const val COMMAND_DISMISS_NOTIFICATION = "DISMISS_NOTIFICATION"

const val EXTRA_RESULT_CODE = "RESULT CODE"
const val HOURS_RESULT_CODE = 100
const val MINUTES_RESULT_CODE = 200
const val SECONDS_RESULT_CODE = 300
const val TIMER_STARTED_RESULT_CODE = 400
const val TIMER_RESUMED_RESULT_CODE = 500
const val ALL_DATA_RESULT_CODE = 600

const val SUPPRESS_UNUSED_PARAMETER = "UNUSED_PARAMETER"

const val TAG = "myTag"