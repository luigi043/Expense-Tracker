package com.expensetracker.domain.repository

import com.expensetracker.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByMonth(month: Int, year: Int): Flow<List<Expense>>
    fun getExpensesByDateRange(from: LocalDate, to: LocalDate): Flow<List<Expense>>
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>
    fun getExpensesByType(type: TransactionType): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
    fun getTotalByTypeAndMonth(type: TransactionType, month: Int, year: Int): Flow<Double>
    fun getCategoryBreakdown(month: Int, year: Int): Flow<Map<ExpenseCategory, Double>>
    fun getDailyTrend(month: Int, year: Int): Flow<List<DailyTrend>>
    suspend fun getAllExpensesSnapshot(): List<Expense>
}

interface BudgetRepository {
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>
    suspend fun getBudgetByCategory(category: ExpenseCategory, month: Int, year: Int): Budget?
    suspend fun insertOrUpdateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
    fun getBudgetWithSpending(month: Int, year: Int): Flow<List<Budget>>
}

interface RecurringExpenseRepository {
    fun getAllRecurring(): Flow<List<RecurringExpense>>
    suspend fun getActiveRecurring(): List<RecurringExpense>
    suspend fun insertRecurring(recurringExpense: RecurringExpense): Long
    suspend fun updateRecurring(recurringExpense: RecurringExpense)
    suspend fun deleteRecurring(recurringExpense: RecurringExpense)
}

interface ExchangeRateRepository {
    suspend fun getLatestRates(base: String): Result<ExchangeRate>
    suspend fun getCachedRates(): ExchangeRate?
    suspend fun convert(amount: Double, from: String, to: String): Double?
}