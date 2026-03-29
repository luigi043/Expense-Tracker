package com.expensetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.expensetracker.data.local.database.dao.BudgetDao
import com.expensetracker.data.local.database.dao.ExpenseDao
import com.expensetracker.data.local.database.dao.RecurringExpenseDao
import com.expensetracker.data.local.database.entity.BudgetEntity
import com.expensetracker.data.local.database.entity.ExpenseEntity
import com.expensetracker.data.local.database.entity.RecurringExpenseEntity

@Database(
    entities = [
        ExpenseEntity::class,
        BudgetEntity::class,
        RecurringExpenseEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
    }
}