package com.example.timer.helpers

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import com.example.timer.base.SUPPRESS_DEPRECATION
import com.example.timer.utils.Utils
import java.util.*

class LocaleHelper {
    companion object {
        fun setLocale(context: Context, language: String): Context {
            return if (Utils.isNougatOrAbove()) {
                updateResources(context, language)
            } else {
                updateResourcesLegacy(context, language)
            }
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun updateResources(context: Context, language: String): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val configuration = context.resources.configuration.also {
                it.setLocale(locale)
                it.setLayoutDirection(locale)
            }
            return context.createConfigurationContext(configuration)
        }

        @Suppress(SUPPRESS_DEPRECATION)
        private fun updateResourcesLegacy(context: Context, language: String): Context {
            val resources = context.resources
            val locale = Locale(language)
            Locale.setDefault(locale)

            val configuration = resources.configuration.also {
                it.locale = locale
                it.setLayoutDirection(locale)
            }
            resources.updateConfiguration(configuration, resources.displayMetrics)

            return context
        }
    }
}