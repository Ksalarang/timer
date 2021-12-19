package com.example.timer.model

class Duration {
    val hours: Int
    val minutes: Int
    val seconds: Int
    val secondsTotal: Int

    constructor(hours: Int, minutes: Int, seconds: Int) {
        this.hours = hours
        this.minutes = minutes
        this.seconds = seconds
        secondsTotal = hours * 3600 + minutes * 60 + seconds
    }

    constructor(secondsTotal: Int) {
        hours = secondsTotal / 3600
        val secondsLeft = secondsTotal % 3600
        minutes = secondsLeft / 60
        seconds = secondsLeft % 60
        this.secondsTotal = secondsTotal
    }
}