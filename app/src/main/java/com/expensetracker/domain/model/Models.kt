package com.expensetracker.domain.model

import java.time.LocalDate

enum class TransactionType { EXPENSE, INCOME }

enum class ExpenseCategory(val label: String, val emoji: String) {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health", "🏥"),
    EDUCATION("Education", "📚"),
    HOUSING("Housing", "🏠"),
    UTILITIES("Utilities", "💡"),
    TRAVEL("Travel", "✈️"),
    SALARY("Salary", "💼"),
    FREELANCE("Freelance", "💻"),
    INVESTMENTS("Investments", "📈"),
    OTHER("Other", "📦");

    companion object {
        fun fromString(name: String): ExpenseCategory = try {
            valueOf(name)
        } catch (e: Exception) {
            OTHER
        }

        val expenseCategories = values().filter {
            it !in listOf(SALARY, FREELANCE, INVESTMENTS)
        }
        val incomeCategories = listOf(SALARY, FREELANCE, INVESTMENTS, OTHER)
    }
}

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val date: LocalDate,
    val note: String = "",
    val type: TransactionType,
    val currency: String = "USD",
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Budget(
    val id: Long = 0,
    val category: ExpenseCategory,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int,
    val spent: Double = 0.0
) {
    val remaining: Double get() = monthlyLimit - spent
    val usagePercent: Float get() = if (monthlyLimit > 0) (spent / monthlyLimit).toFloat() else 0f
    val isOverBudget: Boolean get() = spent > monthlyLimit
    val isNearLimit: Boolean get() = usagePercent >= 0.8f && !isOverBudget
}

data class RecurringExpense(
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val note: String = "",
    val type: TransactionType,
    val currency: String = "USD",
    val intervalDays: Int,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true
)

data class DashboardSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val recentTransactions: List<Expense>,
    val categoryBreakdown: Map<ExpenseCategory, Double>,
    val dailyTrend: List<DailyTrend>
)

data class DailyTrend(
    val date: LocalDate,
    val amount: Double
)

data class ExchangeRate(
    val base: String,
    val rates: Map<String, Double>,
    val timestamp: Long = System.currentTimeMillis()
)