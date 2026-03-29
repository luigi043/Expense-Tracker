package com.expensetracker.data.local.repository

import com.expensetracker.data.local.database.dao.BudgetDao
import com.expensetracker.data.local.database.dao.ExpenseDao
import com.expensetracker.data.local.database.dao.RecurringExpenseDao
import com.expensetracker.data.local.database.entity.BudgetEntity
import com.expensetracker.data.local.database.mapper.*
import com.expensetracker.domain.model.*
import com.expensetracker.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    override fun getExpensesByMonth(month: Int, year: Int): Flow<List<Expense>> =
        dao.getExpensesByMonth(month, year.toString()).map { it.map { e -> e.toDomain() } }

    override fun getExpensesByDateRange(from: LocalDate, to: LocalDate): Flow<List<Expense>> =
        dao.getExpensesByDateRange(from.toString(), to.toString())
            .map { it.map { e -> e.toDomain() } }

    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> =
        dao.getExpensesByCategory(category.name).map { it.map { e -> e.toDomain() } }

    override fun getExpensesByType(type: TransactionType): Flow<List<Expense>> =
        dao.getExpensesByType(type.name).map { it.map { e -> e.toDomain() } }

    override suspend fun getExpenseById(id: Long): Expense? =
        dao.getExpenseById(id)?.toDomain()

    override suspend fun insertExpense(expense: Expense): Long =
        dao.insertExpense(expense.toEntity())

    override suspend fun updateExpense(expense: Expense) =
        dao.updateExpense(expense.toEntity())

    override suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense.toEntity())

    override suspend fun deleteExpenseById(id: Long) =
        dao.deleteExpenseById(id)

    override fun getTotalByTypeAndMonth(type: TransactionType, month: Int, year: Int): Flow<Double> =
        dao.getTotalByTypeAndMonth(type.name, month, year.toString())

    override fun getCategoryBreakdown(month: Int, year: Int): Flow<Map<ExpenseCategory, Double>> =
        dao.getCategoryTotals(month, year.toString()).map { list ->
            list.associate { ExpenseCategory.fromString(it.category) to it.total }
        }

    override fun getDailyTrend(month: Int, year: Int): Flow<List<DailyTrend>> =
        dao.getDailyTotals(month, year.toString()).map { list ->
            list.map { DailyTrend(date = LocalDate.parse(it.date), amount = it.total) }
        }

    override suspend fun getAllExpensesSnapshot(): List<Expense> =
        dao.getAllExpensesSnapshot().map { it.toDomain() }
}

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao
) : BudgetRepository {

    override fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(month, year).map { it.map { b -> b.toDomain() } }

    override suspend fun getBudgetByCategory(
        category: ExpenseCategory, month: Int, year: Int
    ): Budget? = budgetDao.getBudgetByCategory(category.name, month, year)?.toDomain()

    override suspend fun insertOrUpdateBudget(budget: Budget) =
        budgetDao.insertOrUpdateBudget(budget.toEntity())

    override suspend fun deleteBudget(budget: Budget) =
        budgetDao.deleteBudget(budget.toEntity())

    override fun getBudgetWithSpending(month: Int, year: Int): Flow<List<Budget>> {
        val budgetsFlow = budgetDao.getBudgetsForMonth(month, year)
        val categoryTotalsFlow = expenseDao.getCategoryTotals(month, year.toString())

        return combine(budgetsFlow, categoryTotalsFlow) { budgets, totals ->
            val spendMap = totals.associate { it.category to it.total }
            budgets.map { entity ->
                entity.toDomain(spent = spendMap[entity.category] ?: 0.0)
            }
        }
    }
}

@Singleton
class RecurringExpenseRepositoryImpl @Inject constructor(
    private val dao: RecurringExpenseDao
) : RecurringExpenseRepository {

    override fun getAllRecurring(): Flow<List<RecurringExpense>> =
        dao.getAllRecurring().map { it.map { e -> e.toDomain() } }

    override suspend fun getActiveRecurring(): List<RecurringExpense> =
        dao.getActiveRecurring().map { it.toDomain() }

    override suspend fun insertRecurring(recurringExpense: RecurringExpense): Long =
        dao.insertRecurring(recurringExpense.toEntity())

    override suspend fun updateRecurring(recurringExpense: RecurringExpense) =
        dao.updateRecurring(recurringExpense.toEntity())

    override suspend fun deleteRecurring(recurringExpense: RecurringExpense) =
        dao.deleteRecurring(recurringExpense.toEntity())
}