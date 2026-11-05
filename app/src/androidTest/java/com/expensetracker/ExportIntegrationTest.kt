package com.expensetracker

import androidx.test.platform.app.InstrumentationRegistry
import com.expensetracker.domain.model.*
import com.expensetracker.utils.ExportManager
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class ExportIntegrationTest {
    @Test fun exportsPrivateReportsWithContentUrisAndSeparateCurrencyTotals() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = ExportManager(context)
        val entries = listOf(
            Expense(amount = 12.5, category = ExpenseCategory.FOOD, date = LocalDate.of(2026, 11, 4), type = TransactionType.EXPENSE, note = "=1+1"),
            Expense(amount = 9.0, category = ExpenseCategory.SHOPPING, date = LocalDate.of(2026, 11, 4), type = TransactionType.EXPENSE, currency = "EUR")
        )
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            val csv = manager.exportToCsv(entries).getOrThrow()
            assertTrue(csv.readText().contains("12.50"))
            assertTrue(csv.readText().contains("'=1+1"))
            val uri = manager.uriFor(csv)
            assertEquals("content", uri.scheme)
            context.contentResolver.openInputStream(uri)!!.use { assertTrue(it.read() >= 0) }
            val pdf = manager.exportToPdf(entries).getOrThrow()
            val reader = PdfReader(pdf.absolutePath)
            val text = PdfTextExtractor.getTextFromPage(reader, 1)
            reader.close()
            assertTrue(text.contains("USD | Income: 0.00 | Expenses: 12.50"))
            assertTrue(text.contains("EUR | Income: 0.00 | Expenses: 9.00"))
            csv.delete(); pdf.delete()
        } finally { Locale.setDefault(previous) }
    }
}
