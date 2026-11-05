package com.expensetracker

import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import com.expensetracker.utils.withEnglishUi
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class UiLocaleIntegrationTest {
    @Test fun englishUiOverridesPortugueseAndTurkishResourceContexts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("pt-PT", "tr-TR").forEach { language ->
            val foreign = context.createConfigurationContext(Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(language))
            })
            val english = foreign.withEnglishUi()
            assertEquals("en", english.resources.configuration.locales[0].language)
            assertEquals("Cancel", english.getString(android.R.string.cancel))
        }
    }
}
