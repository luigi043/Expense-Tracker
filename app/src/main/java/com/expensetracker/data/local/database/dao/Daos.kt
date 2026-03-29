package com.expensetracker.data.local.database.dao

import androidx.room.*
import com.expensetracker.data.local.database.entity.BudgetEntity
import com.expensetracker.data.local.database.entity.ExpenseEntity
import com.expensetracker.data.local.database.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query(
        """SELECT * FROM expenses
           WHERE strftime('%m', date) = printf('%02d', :month)
           AND   strftime('%Y', date) = :year
           ORDER BY date DESC"""
    )
    fun getExpensesByMonth(month: Int, year: String): Flow<List<ExpenseEntity>>

    @Query(
        """SELECT * FROM expenses
           WHERE date BETWEEN :from AND :to
           ORDER BY date DESC"""
    )
    fun getExpensesByDateRange(from: String, to: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC")
    fun getExpensesByType(type: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query(
        """SELECT COALESCE(SUM(amount), 0.0) FROM expenses
           WHERE type = :type
           AND strftime('%m', date) = printf('%02d', :month)
           AND strftime('%Y', date) = :year"""
    )
    fun getTotalByTypeAndMonth(type: String, month: Int, year: String): Flow<Double>

    @Query(
        """SELECT category, SUM(amount) as total FROM expenses
           WHERE type = 'EXPENSE'
           AND strftime('%m', date) = printf('%02d', :month)
           AND strftime('%Y', date) = :year
           GROUP BY category"""
    )
    fun getCategoryTotals(month: Int, year: String): Flow<List<CategoryTotal>>

    @Query(
        """SELECT date, SUM(amount) as total FROM expenses
           WHERE type = 'EXPENSE'
           AND strftime('%m', date) = printf('%02d', :month)
           AND strftime('%Y', date) = :year
           GROUP BY date
           ORDER BY date ASC"""
    )
    fun getDailyTotals(month: Int, year: String): Flow<List<DailyTotal>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesSnapshot(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}

data class CategoryTotal(val category: String, val total: Double)
data class DailyTotal(val date: String, val total: Double)

@Dao
interface BudgetDao {

    @Query(
        """SELECT * FROM budgets
           WHERE month = :month AND year = :year"""
    )
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query(
        """SELECT * FROM budgets
           WHERE category = :category AND month = :month AND year = :year
           LIMIT 1"""
    )
    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM recurring_expenses ORDER BY nextDueDate ASC")
    fun getAllRecurring(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1")
    suspend fun getActiveRecurring(): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(entity: RecurringExpenseEntity): Long

    @Update
    suspend fun updateRecurring(entity: RecurringExpenseEntity)

    @Delete
    suspend fun deleteRecurring(entity: RecurringExpenseEntity)
}