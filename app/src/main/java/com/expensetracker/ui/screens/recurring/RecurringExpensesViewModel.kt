package com.expensetracker.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.local.datastore.AppPreferences
import com.expensetracker.domain.model.*
import com.expensetracker.domain.repository.RecurringExpenseRepository
import com.expensetracker.domain.usecase.AddRecurringExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class RecurringUiState(
    val isLoading: Boolean = true,
    val items: List<RecurringExpense> = emptyList(),
    val currency: String = "USD",
    val error: String? = null
)

@HiltViewModel
class RecurringExpensesViewModel @Inject constructor(
    private val repository: RecurringExpenseRepository,
    private val addRecurring: AddRecurringExpenseUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val operationError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecurringUiState> = combine(
        repository.getAllRecurring(),
        appPreferences.baseCurrency
    ) { items, currency ->
        RecurringUiState(isLoading = false, items = items, currency = currency)
    }.catch { e ->
        emit(RecurringUiState(isLoading = false, error = e.message))
    }.combine(operationError) { state, error -> state.copy(error = error ?: state.error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringUiState())

    fun add(
        amount: Double,
        category: ExpenseCategory,
        type: TransactionType,
        note: String,
        intervalDays: Int,
        currency: String,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val recurring = RecurringExpense(
                amount = amount,
                category = category,
                type = type,
                note = note,
                intervalDays = intervalDays,
                currency = currency,
                nextDueDate = LocalDate.now().plusDays(intervalDays.toLong())
            )
            addRecurring(recurring).fold(
                onSuccess = { operationError.value = null; onSaved() },
                onFailure = { operationError.value = it.message ?: "Could not save the recurring entry" }
            )
        }
    }

    fun delete(item: RecurringExpense) {
        viewModelScope.launch { repository.deleteRecurring(item) }
    }

    fun toggle(item: RecurringExpense) {
        viewModelScope.launch {
            repository.updateRecurring(item.copy(isActive = !item.isActive))
        }
    }
    fun clearError() { operationError.value = null }
}
