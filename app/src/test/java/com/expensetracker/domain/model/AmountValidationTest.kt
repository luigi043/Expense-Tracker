package com.expensetracker.domain.model

import org.junit.Assert.*
import org.junit.Test

class AmountValidationTest {
    @Test fun rejectsSpecialValuesOverflowAndNonPositiveAmounts() {
        listOf("NaN", "Infinity", "-Infinity", "1e309", "0", "-0.01", "", "abc").forEach {
            assertNull("Accepted invalid amount: $it", it.parsePositiveAmount())
        }
    }

    @Test fun acceptsOrdinaryDecimalAmounts() {
        assertEquals(12.50, "12.50".parsePositiveAmount()!!, 0.0)
        assertEquals(0.01, "0.01".parsePositiveAmount()!!, 0.0)
    }
}
