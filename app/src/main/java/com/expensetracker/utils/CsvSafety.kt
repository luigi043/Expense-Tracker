package com.expensetracker.utils

/** Quoting CSV fields alone does not stop spreadsheet formula evaluation. */
fun spreadsheetSafeText(value: String): String =
    if (value.trimStart().firstOrNull() in listOf('=', '+', '-', '@') ||
        value.firstOrNull() in listOf('\t', '\r', '\n')) "'$value" else value
