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

// ─────────────────────────────────────────────────────────────────────────────

package com.expensetracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.expensetracker.ui.components.AppTopBar
import com.expensetracker.ui.navigation.Screen
import com.expensetracker.ui.screens.dashboard.AppBottomBar
import com.expensetracker.utils.SUPPORTED_CURRENCIES
import com.expensetracker.utils.Formatters.currencySymbol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCurrencyDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Base Currency") },
            text = {
                Column {
                    SUPPORTED_CURRENCIES.forEach { code ->
                        TextButton(
                            onClick = { viewModel.setCurrency(code); showCurrencyDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${currencySymbol(code)} $code")
                                if (state.baseCurrency == code) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Settings") },
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Settings.route)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Appearance ─────────────────────────────────────────────────
            SettingsSectionTitle("Appearance")

            SettingsRow(
                icon = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                title = "Dark Mode",
                subtitle = if (state.isDarkMode) "Dark theme active" else "Light theme active",
                trailing = {
                    Switch(
                        checked = state.isDarkMode,
                        onCheckedChange = viewModel::toggleDarkMode
                    )
                }
            )

            // ── Localisation ───────────────────────────────────────────────
            SettingsSectionTitle("Currency")

            SettingsRow(
                icon = Icons.Default.AttachMoney,
                title = "Base Currency",
                subtitle = "${currencySymbol(state.baseCurrency)} ${state.baseCurrency}",
                trailing = {
                    TextButton(onClick = { showCurrencyDialog = true }) { Text("Change") }
                }
            )

            // ── Tools ──────────────────────────────────────────────────────
            SettingsSectionTitle("Tools")

            SettingsRow(
                icon = Icons.Default.CurrencyExchange,
                title = "Currency Converter",
                subtitle = "Convert between currencies in real-time",
                trailing = {
                    IconButton(onClick = { navController.navigate(Screen.CurrencyConverter.route) }) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            )

            SettingsRow(
                icon = Icons.Default.Repeat,
                title = "Recurring Expenses",
                subtitle = "Manage automatic recurring transactions",
                trailing = {
                    IconButton(onClick = { navController.navigate(Screen.RecurringExpenses.route) }) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            )

            // ── About ──────────────────────────────────────────────────────
            SettingsSectionTitle("About")

            SettingsRow(
                icon = Icons.Default.Info,
                title = "Version",
                subtitle = "1.0.0 – Production Build",
                trailing = {}
            )
        }
    }
}

// ── Settings UI helpers ────────────────────────────────────────────────────

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing()
        }
    }
}