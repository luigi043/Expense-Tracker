package com.expensetracker.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Keep app resources and Material date-picker controls in the supported UI language. */
fun Context.withEnglishUi(): Context {
    val configuration = Configuration(resources.configuration).apply {
        setLocale(Locale.ENGLISH)
        setLayoutDirection(Locale.ENGLISH)
    }
    return createConfigurationContext(configuration)
}
