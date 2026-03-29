package com.expensetracker.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.local.datastore.AppPreferences
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.domain.usecase.GetBudgetsUseCase
import com.expensetracker.domain.usecase.SetBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BudgetUiState(
    val isLoading: Boolean = true,
    val budgets: List<Budget> = emptyList(),
    val currency: String = "USD",
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgets: GetBudgetsUseCase,
    private val setBudget: SetBudgetUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _month = MutableStateFlow(LocalDate.now().monthValue)
    private val _year  = MutableStateFlow(LocalDate.now().year)

    val uiState: StateFlow<BudgetUiState> = combine(
        _month, _year, appPreferences.baseCurrency
    ) { m, y, c -> Triple(m, y, c) }.flatMapLatest { (m, y, c) ->
        getBudgets(m, y).map { budgets ->
            BudgetUiState(isLoading = false, budgets = budgets, currency = c, month = m, year = y)
        }
    }.catch { e ->
        emit(BudgetUiState(isLoading = false, error = e.message))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    fun saveBudget(category: ExpenseCategory, limit: Double) {
        viewModelScope.launch {
            val budget = Budget(
                category = category,
                monthlyLimit = limit,
                month = _month.value,
                year = _year.value
            )
            setBudget(budget).fold(
                onSuccess = { },
                onFailure = { }
            )
        }
    }
}