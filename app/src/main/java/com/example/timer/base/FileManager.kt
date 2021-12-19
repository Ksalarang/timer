package com.example.timer.base

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

private const val FILENAME_PREV_STATE = "prev_state"
private const val SUFFIX_PREV_STATE = ".tmp"
private const val PREV_STATE_PARAMETER = "prevStateInSec"
private const val DELIMITER = "="

class FileManager(private val context: Context) {
    fun writeToCache(timeInSeconds: Int) {
        try {
            val cacheFile = File.createTempFile(FILENAME_PREV_STATE, SUFFIX_PREV_STATE, context.cacheDir)
            val line = PREV_STATE_PARAMETER + DELIMITER + timeInSeconds
            context.openFileOutput(cacheFile.name, Context.MODE_PRIVATE).write(line.toByteArray())
        } catch (e: IOException) {
            Log.d(TAG, "writeToCache - IOException")
        }
    }

    // Returns previous timer state from cache in seconds
    fun retrievePrevTimerStateFromCache(): Int {
        val cacheFile = File(context.cacheDir, FILENAME_PREV_STATE + SUFFIX_PREV_STATE)
        var prevStateInSeconds = 0
        val lines: MutableList<String>

        if (cacheFile.exists()) {
            lines = readFile(cacheFile)
            lines.forEach {
                val list = it.split(DELIMITER)
                if (list[0] == PREV_STATE_PARAMETER) {
                    prevStateInSeconds = Integer.parseInt(list[1])
                }
            }
        }
        return prevStateInSeconds
    }

    fun readFile(file: File): MutableList<String> {
        val linesFromFile = mutableListOf<String>()

        context.openFileInput(file.name).bufferedReader().useLines { lines ->
            lines.forEach { linesFromFile.add(it) }
        }
        return linesFromFile
    }
}