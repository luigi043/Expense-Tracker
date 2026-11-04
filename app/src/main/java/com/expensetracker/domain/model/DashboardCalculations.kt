package com.expensetracker.domain.model

/** No implicit exchange rate: totals include only transactions in the selected currency. */
fun summarizeTransactions(expenses: List<Expense>, currency: String): DashboardSummary {
    val matching = expenses.filter { it.currency == currency }
    val spending = matching.filter { it.type == TransactionType.EXPENSE }
    val income = matching.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = spending.sumOf { it.amount }
    return DashboardSummary(
        totalIncome = income,
        totalExpense = totalExpense,
        balance = income - totalExpense,
        recentTransactions = expenses.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.id }).take(10),
        categoryBreakdown = spending.groupBy { it.category }.mapValues { (_, entries) -> entries.sumOf { it.amount } },
        dailyTrend = spending.groupBy { it.date }.toSortedMap().map { (date, entries) -> DailyTrend(date, entries.sumOf { it.amount }) }
    )
}
