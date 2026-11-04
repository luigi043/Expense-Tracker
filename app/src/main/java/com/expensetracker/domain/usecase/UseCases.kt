package com.expensetracker.domain.usecase

import com.expensetracker.domain.model.*
import com.expensetracker.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Long> = runCatching {
        require(expense.amount.isPositiveAmount()) { "Enter a finite amount greater than zero" }
        require(expense.note.length <= 500) { "Note must be 500 characters or fewer" }
        repository.insertExpense(expense)
    }
}

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> = runCatching {
        require(expense.amount.isPositiveAmount()) { "Enter a finite amount greater than zero" }
        require(expense.note.length <= 500) { "Note must be 500 characters or fewer" }
        repository.updateExpense(expense)
    }
}

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching {
        repository.deleteExpenseById(id)
    }
}

class GetExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    fun byMonth(month: Int, year: Int): Flow<List<Expense>> =
        repository.getExpensesByMonth(month, year)

    fun byDateRange(from: LocalDate, to: LocalDate): Flow<List<Expense>> =
        repository.getExpensesByDateRange(from, to)

    fun all(): Flow<List<Expense>> = repository.getAllExpenses()

    fun byCategory(category: ExpenseCategory): Flow<List<Expense>> =
        repository.getExpensesByCategory(category)
}

class GetDashboardSummaryUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(month: Int, year: Int, currency: String = "USD"): Flow<DashboardSummary> =
        expenseRepository.getExpensesByMonth(month, year).map { summarizeTransactions(it, currency) }
}
class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(month: Int, year: Int, currency: String = "USD"): Flow<List<Budget>> =
        combine(budgetRepository.getBudgetsForMonth(month, year), expenseRepository.getExpensesByMonth(month, year)) { budgets, expenses ->
            budgets.map { budget -> budget.copy(spent = expenses.filter {
                it.type == TransactionType.EXPENSE && it.category == budget.category && it.currency == currency
            }.sumOf { it.amount }) }
        }
}

class SetBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Unit> = runCatching {
        require(budget.monthlyLimit.isPositiveAmount()) { "Enter a finite budget limit greater than zero" }
        budgetRepository.insertOrUpdateBudget(budget)
    }
}

class AddRecurringExpenseUseCase @Inject constructor(
    private val repository: RecurringExpenseRepository
) {
    suspend operator fun invoke(recurringExpense: RecurringExpense): Result<Long> = runCatching {
        require(recurringExpense.amount.isPositiveAmount()) { "Enter a finite amount greater than zero" }
        require(recurringExpense.note.length <= 500) { "Note must be 500 characters or fewer" }
        require(recurringExpense.intervalDays > 0) { "Interval must be at least 1 day" }
        repository.insertRecurring(recurringExpense)
    }
}

class ProcessDueRecurringExpensesUseCase @Inject constructor(
    private val recurringRepo: RecurringExpenseRepository,
    private val expenseRepo: ExpenseRepository
) {
    suspend operator fun invoke(): Int {
        val today = LocalDate.now()
        val dueItems = recurringRepo.getActiveRecurring()
            .filter { !it.nextDueDate.isAfter(today) }

        dueItems.forEach { recurring ->
            val expense = Expense(
                amount = recurring.amount,
                category = recurring.category,
                date = today,
                note = recurring.note,
                type = recurring.type,
                currency = recurring.currency,
                isRecurring = true,
                recurringId = recurring.id
            )
            expenseRepo.insertExpense(expense)
            val updated = recurring.copy(
                nextDueDate = today.plusDays(recurring.intervalDays.toLong())
            )
            recurringRepo.updateRecurring(updated)
        }
        return dueItems.size
    }
}

class ConvertCurrencyUseCase @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    suspend operator fun invoke(amount: Double, from: String, to: String): Result<Double> =
        runCatching {
            require(amount.isFinite() && amount >= 0) { "Enter a finite, non-negative amount" }
            if (from == to) return@runCatching amount
            exchangeRateRepository.convert(amount, from, to)
                ?: error("Exchange rates not available")
        }
}
