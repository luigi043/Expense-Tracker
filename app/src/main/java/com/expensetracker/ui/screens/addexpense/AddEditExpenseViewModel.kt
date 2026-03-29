package com.expensetracker.ui.screens.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Expense
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.ExpenseRepository
import com.expensetracker.domain.usecase.AddExpenseUseCase
import com.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.expensetracker.domain.usecase.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditUiState(
    val isLoading: Boolean      = false,
    val isEditMode: Boolean     = false,
    val isSaved: Boolean        = false,
    val isDeleted: Boolean      = false,
    val error: String?          = null,
    val amount: String          = "",
    val amountError: String?    = null,
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val date: LocalDate         = LocalDate.now(),
    val note: String            = "",
    val type: TransactionType   = TransactionType.EXPENSE,
    val currency: String        = "USD",
    val isRecurring: Boolean    = false
)

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val addExpense: AddExpenseUseCase,
    private val updateExpense: UpdateExpenseUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun loadExpense(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val expense = repository.getExpenseById(id)
            if (expense != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading   = false,
                        isEditMode  = true,
                        amount      = expense.amount.toString(),
                        category    = expense.category,
                        date        = expense.date,
                        note        = expense.note,
                        type        = expense.type,
                        currency    = expense.currency,
                        isRecurring = expense.isRecurring
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Expense not found") }
            }
        }
    }

    fun onAmountChange(value: String) {
        _uiState.update {
            it.copy(
                amount = value,
                amountError = if (value.isBlank() || value.toDoubleOrNull() == null || (value.toDoubleOrNull() ?: 0.0) <= 0)
                    "Please enter a valid positive amount" else null
            )
        }
    }

    fun onCategoryChange(category: ExpenseCategory) =
        _uiState.update { it.copy(category = category) }

    fun onDateChange(date: LocalDate) =
        _uiState.update { it.copy(date = date) }

    fun onNoteChange(note: String) =
        _uiState.update { it.copy(note = note) }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { state ->
            val defaultCat = if (type == TransactionType.INCOME)
                ExpenseCategory.SALARY else ExpenseCategory.FOOD
            state.copy(
                type = type,
                category = if ((type == TransactionType.INCOME) != (state.type == TransactionType.INCOME))
                    defaultCat else state.category
            )
        }
    }

    fun onCurrencyChange(currency: String) =
        _uiState.update { it.copy(currency = currency) }

    fun onRecurringChange(isRecurring: Boolean) =
        _uiState.update { it.copy(isRecurring = isRecurring) }

    fun save(expenseId: Long?) {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = "Please enter a valid positive amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val expense = Expense(
                id       = expenseId ?: 0L,
                amount   = amount,
                category = state.category,
                date     = state.date,
                note     = state.note.trim(),
                type     = state.type,
                currency = state.currency,
                isRecurring = state.isRecurring
            )

            val result = if (expenseId != null) {
                updateExpense(expense)
            } else {
                addExpense(expense).map { }
            }

            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            deleteExpense(id).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isDeleted = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}