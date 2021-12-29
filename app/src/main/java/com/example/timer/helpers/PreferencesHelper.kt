package com.example.timer.helpers

import androidx.appcompat.app.AppCompatActivity

class PreferencesHelper {
    companion object {
        fun getIntPreference(activity: AppCompatActivity, key: String, defaultValue: Int = 0): Int {
            return activity.getPreferences(AppCompatActivity.MODE_PRIVATE).getInt(key, defaultValue)
        }

        fun saveIntPreference(activity: AppCompatActivity, key: String, value: Int) {
            activity.getPreferences(AppCompatActivity.MODE_PRIVATE)
                .edit()
                .putInt(key, value)
                .apply()
        }

        fun getStringPreference(activity: AppCompatActivity, key: String, defaultValue: String? = null): String? {
            return activity.getPreferences(AppCompatActivity.MODE_PRIVATE).getString(key, defaultValue)
        }

        fun saveStringPreference(activity: AppCompatActivity, key: String, value: String) {
            activity.getPreferences(AppCompatActivity.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply()
        }
    }
}