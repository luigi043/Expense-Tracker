package com.expensetracker

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.expensetracker.data.local.database.AppDatabase
import com.expensetracker.data.local.repository.ExpenseRepositoryImpl
import com.expensetracker.data.local.repository.RecurringExpenseRepositoryImpl
import com.expensetracker.domain.model.*
import com.expensetracker.domain.usecase.ProcessDueRecurringExpensesUseCase
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RecurringProcessingTest {
    @Test fun processesDueTemplateAndDoesNotRepeatItBeforeNextDueDate() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val recurring = RecurringExpenseRepositoryImpl(database.recurringExpenseDao())
            val expenses = ExpenseRepositoryImpl(database.expenseDao())
            val today = LocalDate.now()
            val id = recurring.insertRecurring(RecurringExpense(amount = 25.0,
                category = ExpenseCategory.HOUSING, type = TransactionType.EXPENSE,
                currency = "EUR", intervalDays = 7, nextDueDate = today.minusDays(1)))
            val process = ProcessDueRecurringExpensesUseCase(recurring, expenses)
            assertEquals(1, process())
            assertEquals(0, process())
            val expense = expenses.getAllExpensesSnapshot().single()
            assertEquals(id, expense.recurringId)
            assertEquals("EUR", expense.currency)
            assertEquals(today, expense.date)
            assertTrue(expense.isRecurring)
            assertEquals(today.plusDays(7), recurring.getActiveRecurring().single().nextDueDate)
        } finally { database.close() }
    }
}
