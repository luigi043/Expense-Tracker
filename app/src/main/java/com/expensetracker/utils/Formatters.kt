package com.expensetracker.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object Formatters {
    private val shortDate = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)
    private val fullDate  = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    fun LocalDate.toShortDate(): String  = format(shortDate)
    fun LocalDate.toFullDate(): String   = format(fullDate)
    fun LocalDate.toMonthYear(): String  = format(monthYear)

    fun LocalDate.dayOfWeekShort(): String =
        dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

    fun Double.toCurrencyString(currency: String = "USD"): String =
        "%s %.2f".format(currencySymbol(currency), this)

    fun currencySymbol(code: String): String = when (code) {
        "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY" -> "¥"
        "BRL" -> "R$"; "CAD" -> "CA$"; "AUD" -> "A$"; "CHF" -> "CHF"
        "CNY" -> "¥"; "INR" -> "₹"; "MXN" -> "MX$"; "SEK" -> "kr"
        else -> code
    }
}

val SUPPORTED_CURRENCIES = listOf(
    "USD", "EUR", "GBP", "JPY", "BRL", "CAD", "AUD", "CHF", "CNY", "INR", "MXN", "SEK"
)

val RECURRING_INTERVALS = listOf(
    1  to "Daily",
    7  to "Weekly",
    14 to "Every 2 Weeks",
    30 to "Every 30 Days",
    90 to "Every 90 Days",
    365 to "Every 365 Days"
)
