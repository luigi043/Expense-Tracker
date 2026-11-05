package com.expensetracker.utils

import com.expensetracker.utils.Formatters.toCurrencyString
import com.expensetracker.utils.Formatters.toFullDate
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {
    @Test fun usesEnglishDateLabelsAndDecimalFormattingWithAPortugueseDeviceLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("pt-PT"))
            assertEquals("05 Nov 2026", LocalDate.of(2026, 11, 5).toFullDate())
            assertEquals("€ 12.50", 12.5.toCurrencyString("EUR"))
        } finally { Locale.setDefault(previous) }
    }
}
