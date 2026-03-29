package com.expensetracker.utils

import android.content.Context
import android.os.Environment
import com.expensetracker.domain.model.Expense
import com.expensetracker.domain.model.TransactionType
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun exportToCsv(expenses: List<Expense>): Result<File> = runCatching {
        val dir = getExportDir()
        val file = File(dir, "expenses_${System.currentTimeMillis()}.csv")

        CSVWriter(FileWriter(file)).use { writer ->
            writer.writeNext(
                arrayOf("ID", "Date", "Type", "Category", "Amount", "Currency", "Note", "Recurring")
            )
            expenses.forEach { e ->
                writer.writeNext(
                    arrayOf(
                        e.id.toString(),
                        e.date.format(dateFormatter),
                        e.type.name,
                        e.category.label,
                        "%.2f".format(e.amount),
                        e.currency,
                        e.note,
                        e.isRecurring.toString()
                    )
                )
            }
        }
        file
    }

    fun exportToPdf(expenses: List<Expense>): Result<File> = runCatching {
        val dir = getExportDir()
        val file = File(dir, "expenses_${System.currentTimeMillis()}.pdf")

        val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        val titleFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD, BaseColor(30, 30, 30))
        document.add(Paragraph("Expense Report", titleFont))
        document.add(Paragraph(" "))

        val totalExpense = expenses.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalIncome  = expenses.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount }
        val summaryFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, BaseColor(80, 80, 80))
        document.add(Paragraph("Total Income:  $${"%.2f".format(totalIncome)}", summaryFont))
        document.add(Paragraph("Total Expense: $${"%.2f".format(totalExpense)}", summaryFont))
        document.add(Paragraph("Balance:       $${"%.2f".format(totalIncome - totalExpense)}", summaryFont))
        document.add(Paragraph(" "))

        val table = PdfPTable(floatArrayOf(1.5f, 3f, 3f, 2f, 2f, 3f))
        table.widthPercentage = 100f

        val headerBg = BaseColor(45, 55, 72)
        val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)

        listOf("Date", "Type", "Category", "Amount", "Currency", "Note").forEach { header ->
            val cell = PdfPCell(Phrase(header, headerFont)).apply {
                backgroundColor = headerBg
                setPadding(6f)
                horizontalAlignment = Element.ALIGN_CENTER
            }
            table.addCell(cell)
        }

        val cellFont = Font(Font.FontFamily.HELVETICA, 9f)
        val altRowBg = BaseColor(245, 247, 250)

        expenses.forEachIndexed { index, e ->
            val bg = if (index % 2 == 0) BaseColor.WHITE else altRowBg
            listOf(
                e.date.format(dateFormatter),
                e.type.name.lowercase().replaceFirstChar { it.uppercase() },
                e.category.label,
                "%.2f".format(e.amount),
                e.currency,
                e.note.take(40)
            ).forEach { value ->
                val cell = PdfPCell(Phrase(value, cellFont)).apply {
                    backgroundColor = bg
                    setPadding(5f)
                }
                table.addCell(cell)
            }
        }

        document.add(table)
        document.close()
        file
    }

    private fun getExportDir(): File {
        val dir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ExpenseTracker")
        } else {
            @Suppress("DEPRECATION")
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "ExpenseTracker"
            )
        }
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}