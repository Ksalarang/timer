package com.example.timer.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager

class Utils {
    companion object {
        fun isOreoOrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        fun isNougatOrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        fun isMarshmallowOrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        fun hideKeyboard(context: Context, view: View) {
            (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}