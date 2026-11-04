package com.expensetracker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
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
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun uriFor(file: File): Uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", file)

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
                        String.format(Locale.ROOT, "%.2f", e.amount),
                        e.currency,
                        spreadsheetSafeText(e.note),
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

        val summaryFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, BaseColor(80, 80, 80))
        expenses.groupBy { it.currency }.toSortedMap().forEach { (currency, entries) ->
            val income = entries.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val spending = entries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            document.add(Paragraph(String.format(Locale.ROOT,
                "%s | Income: %.2f | Expenses: %.2f | Balance: %.2f", currency, income, spending, income - spending), summaryFont))
        }
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
                String.format(Locale.ROOT, "%.2f", e.amount),
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
        val dir = File(context.filesDir, "exports")
        check(dir.isDirectory || dir.mkdirs()) { "Could not create the report directory" }
        return dir
    }
}
