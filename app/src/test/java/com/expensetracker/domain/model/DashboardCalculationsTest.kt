package com.expensetracker.domain.model

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class DashboardCalculationsTest {
    @Test fun totalsAndChartsNeverAddDifferentCurrencies() {
        val date = LocalDate.of(2026, 11, 4)
        val entries = listOf(
            Expense(id = 1, amount = 100.0, category = ExpenseCategory.SALARY, date = date, type = TransactionType.INCOME),
            Expense(id = 2, amount = 20.0, category = ExpenseCategory.FOOD, date = date, type = TransactionType.EXPENSE),
            Expense(id = 3, amount = 900.0, category = ExpenseCategory.FOOD, date = date, type = TransactionType.EXPENSE, currency = "EUR")
        )
        val usd = summarizeTransactions(entries, "USD")
        assertEquals(80.0, usd.balance, 0.0)
        assertEquals(20.0, usd.categoryBreakdown[ExpenseCategory.FOOD]!!, 0.0)
        assertEquals(20.0, usd.dailyTrend.single().amount, 0.0)
        assertEquals(3, usd.recentTransactions.size)
        assertEquals(-900.0, summarizeTransactions(entries, "EUR").balance, 0.0)
    }
}
