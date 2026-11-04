package com.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvSafetyTest {
    @Test fun neutralizesSpreadsheetFormulaPrefixesIncludingWhitespace() {
        listOf("=1+1", "+SUM(A1)", "-1+2", "@SUM(A1)", "  =1", "\tvalue").forEach {
            assertEquals("'$it", spreadsheetSafeText(it))
        }
    }
    @Test fun preservesOrdinaryNotesAndEmptyText() {
        listOf("", "Lunch, client meeting", "Receipt \"123\"").forEach {
            assertEquals(it, spreadsheetSafeText(it))
        }
    }
}
