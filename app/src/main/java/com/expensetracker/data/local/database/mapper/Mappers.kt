package com.expensetracker.data.local.database.mapper

import com.expensetracker.data.local.database.entity.BudgetEntity
import com.expensetracker.data.local.database.entity.ExpenseEntity
import com.expensetracker.data.local.database.entity.RecurringExpenseEntity
import com.expensetracker.domain.model.*
import java.time.LocalDate

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    amount = amount,
    category = ExpenseCategory.fromString(category),
    date = LocalDate.parse(date),
    note = note,
    type = TransactionType.valueOf(type),
    currency = currency,
    isRecurring = isRecurring,
    recurringId = recurringId,
    createdAt = createdAt
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    amount = amount,
    category = category.name,
    date = date.toString(),
    note = note,
    type = type.name,
    currency = currency,
    isRecurring = isRecurring,
    recurringId = recurringId,
    createdAt = createdAt
)

fun BudgetEntity.toDomain(spent: Double = 0.0) = Budget(
    id = id,
    category = ExpenseCategory.fromString(category),
    monthlyLimit = monthlyLimit,
    month = month,
    year = year,
    spent = spent
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    category = category.name,
    monthlyLimit = monthlyLimit,
    month = month,
    year = year
)

fun RecurringExpenseEntity.toDomain() = RecurringExpense(
    id = id,
    amount = amount,
    category = ExpenseCategory.fromString(category),
    note = note,
    type = TransactionType.valueOf(type),
    currency = currency,
    intervalDays = intervalDays,
    nextDueDate = LocalDate.parse(nextDueDate),
    isActive = isActive
)

fun RecurringExpense.toEntity() = RecurringExpenseEntity(
    id = id,
    amount = amount,
    category = category.name,
    note = note,
    type = type.name,
    currency = currency,
    intervalDays = intervalDays,
    nextDueDate = nextDueDate.toString(),
    isActive = isActive
)