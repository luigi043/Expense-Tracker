package com.expensetracker.ui.screens.history

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.local.datastore.AppPreferences
import com.expensetracker.domain.model.Expense
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.usecase.GetExpensesUseCase
import com.expensetracker.utils.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HistoryFilter(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val category: ExpenseCategory? = null,
    val type: TransactionType? = null,
    val searchQuery: String = ""
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val filter: HistoryFilter = HistoryFilter(),
    val currency: String = "USD",
    val exportStatus: ExportStatus = ExportStatus.Idle,
    val error: String? = null
)

sealed class ExportStatus {
    object Idle : ExportStatus()
    object Exporting : ExportStatus()
    data class Success(val uri: Uri, val mimeType: String) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getExpenses: GetExpensesUseCase,
    private val exportManager: ExportManager,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter())
    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)

    val uiState: StateFlow<HistoryUiState> = combine(
        getExpenses.all(),
        _filter,
        appPreferences.baseCurrency,
        _exportStatus
    ) { all, filter, currency, exportStatus ->
        val filtered = all.filter { e ->
            val afterFrom  = filter.from?.let { !e.date.isBefore(it) } ?: true
            val beforeTo   = filter.to?.let { !e.date.isAfter(it) } ?: true
            val catMatch   = filter.category?.let { e.category == it } ?: true
            val typeMatch  = filter.type?.let { e.type == it } ?: true
            val queryMatch = filter.searchQuery.isBlank() ||
                e.category.label.contains(filter.searchQuery, ignoreCase = true) ||
                e.note.contains(filter.searchQuery, ignoreCase = true)
            afterFrom && beforeTo && catMatch && typeMatch && queryMatch
        }.sortedByDescending { it.date }

        HistoryUiState(
            isLoading = false,
            expenses = all,
            filteredExpenses = filtered,
            filter = filter,
            currency = currency,
            exportStatus = exportStatus
        )
    }.catch { e ->
        emit(HistoryUiState(isLoading = false, error = e.message))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun updateFilter(filter: HistoryFilter) { _filter.value = filter }
    fun clearFilter() { _filter.value = HistoryFilter() }

    fun exportCsv() {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Exporting
            val expenses = uiState.value.filteredExpenses
            exportManager.exportToCsv(expenses).fold(
                onSuccess = { file ->
                    _exportStatus.value = ExportStatus.Success(
                        uri = Uri.fromFile(file),
                        mimeType = "text/csv"
                    )
                },
                onFailure = { e ->
                    _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
                }
            )
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Exporting
            val expenses = uiState.value.filteredExpenses
            exportManager.exportToPdf(expenses).fold(
                onSuccess = { file ->
                    _exportStatus.value = ExportStatus.Success(
                        uri = Uri.fromFile(file),
                        mimeType = "application/pdf"
                    )
                },
                onFailure = { e ->
                    _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
                }
            )
        }
    }

    fun clearExportStatus() { _exportStatus.value = ExportStatus.Idle }
}