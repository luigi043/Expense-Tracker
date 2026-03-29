package com.expensetracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.local.datastore.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val baseCurrency: String = "USD"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.isDarkMode, prefs.baseCurrency
    ) { dark, currency ->
        SettingsUiState(isDarkMode = dark, baseCurrency = currency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch { prefs.setBaseCurrency(currency) }
    }
}