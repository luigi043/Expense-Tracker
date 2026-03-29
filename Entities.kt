package com.expensetracker.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.domain.model.TransactionType

@Entity(
    tableName = "expenses",
    indices = [
        Index("date"),
        Index("category"),
        Index("type")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,           // ExpenseCategory.name
    val date: String,               // ISO-8601 LocalDate e.g. "2024-11-15"
    val note: String = "",
    val type: String,               // TransactionType.name
    val currency: String = "USD",
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["category", "month", "year"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int
)

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String = "",
    val type: String,
    val currency: String = "USD",
    val intervalDays: Int,
    val nextDueDate: String,       // ISO-8601 LocalDate
    val isActive: Boolean = true
)