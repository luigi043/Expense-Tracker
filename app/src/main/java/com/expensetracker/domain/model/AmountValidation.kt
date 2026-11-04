package com.expensetracker.domain.model

/** Reject overflow and IEEE special values before they enter financial totals. */
fun Double.isPositiveAmount(): Boolean = isFinite() && this > 0.0

fun String.parsePositiveAmount(): Double? = toDoubleOrNull()?.takeIf { it.isPositiveAmount() }
