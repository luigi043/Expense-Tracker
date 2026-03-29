package com.expensetracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.local.datastore.AppPreferences
import com.expensetracker.domain.model.DashboardSummary
import com.expensetracker.domain.usecase.GetDashboardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val error: String? = null,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int  = LocalDate.now().year,
    val currency: String   = "USD"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    private val _selectedYear  = MutableStateFlow(LocalDate.now().year)

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedMonth,
        _selectedYear,
        appPreferences.baseCurrency
    ) { month, year, currency ->
        Triple(month, year, currency)
    }.flatMapLatest { (month, year, currency) ->
        getDashboardSummary(month, year).map { summary ->
            DashboardUiState(
                isLoading = false,
                summary = summary,
                selectedMonth = month,
                selectedYear = year,
                currency = currency
            )
        }
    }.catch { e ->
        emit(DashboardUiState(isLoading = false, error = e.message))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState()
    )

    fun previousMonth() {
        viewModelScope.launch {
            val date = LocalDate.of(_selectedYear.value, _selectedMonth.value, 1).minusMonths(1)
            _selectedMonth.value = date.monthValue
            _selectedYear.value  = date.year
        }
    }

    fun nextMonth() {
        viewModelScope.launch {
            val now  = LocalDate.now()
            val currentSelected = LocalDate.of(_selectedYear.value, _selectedMonth.value, 1)
            val date = currentSelected.plusMonths(1)
            if (!date.isAfter(now.withDayOfMonth(1))) {
                _selectedMonth.value = date.monthValue
                _selectedYear.value  = date.year
            }
        }
    }
}