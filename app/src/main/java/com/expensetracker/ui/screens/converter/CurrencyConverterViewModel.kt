package com.expensetracker.ui.screens.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.local.datastore.AppPreferences
import com.expensetracker.domain.usecase.ConvertCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConverterUiState(
    val amount: String = "1",
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val result: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CurrencyConverterViewModel @Inject constructor(
    private val convertCurrency: ConvertCurrencyUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()
    private var conversionVersion = 0L

    init {
        viewModelScope.launch {
            appPreferences.baseCurrency.take(1).collect { currency ->
                _uiState.update { it.copy(fromCurrency = currency) }
            }
        }
    }

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amount = value) }
        convert()
    }

    fun onFromCurrencyChange(currency: String) {
        _uiState.update { it.copy(fromCurrency = currency) }
        convert()
    }

    fun onToCurrencyChange(currency: String) {
        _uiState.update { it.copy(toCurrency = currency) }
        convert()
    }

    fun convert() {
        val version = ++conversionVersion
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull()
        if (amountValue == null || !amountValue.isFinite() || amountValue < 0) {
            _uiState.update { it.copy(result = null, isLoading = false, error = "Enter a valid non-negative amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }
            convertCurrency(amountValue, state.fromCurrency, state.toCurrency).fold(
                onSuccess = { res -> if (version == conversionVersion) _uiState.update { it.copy(isLoading = false, result = res) } },
                onFailure = { e -> if (version == conversionVersion) _uiState.update { it.copy(isLoading = false, result = null, error = e.message) } }
            )
        }
    }

    fun swapCurrencies() {
        _uiState.update { it.copy(fromCurrency = it.toCurrency, toCurrency = it.fromCurrency) }
        convert()
    }
}
