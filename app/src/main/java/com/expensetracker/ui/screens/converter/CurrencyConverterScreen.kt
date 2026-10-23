package com.expensetracker.ui.screens.converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.expensetracker.ui.components.AppTopBar
import com.expensetracker.utils.SUPPORTED_CURRENCIES
import com.expensetracker.utils.Formatters.toCurrencyString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    navController: NavController,
    viewModel: CurrencyConverterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        CurrencyPickerDialog(onDismiss = { showFromPicker = false }, onSelect = viewModel::onFromCurrencyChange)
    }
    if (showToPicker) {
        CurrencyPickerDialog(onDismiss = { showToPicker = false }, onSelect = viewModel::onToCurrencyChange)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Currency Converter", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencySelector(
                    code = state.fromCurrency,
                    label = "From",
                    onClick = { showFromPicker = true },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = viewModel::swapCurrencies) {
                    Icon(Icons.Default.SwapHoriz, "Swap", tint = MaterialTheme.colorScheme.primary)
                }
                CurrencySelector(
                    code = state.toCurrency,
                    label = "To",
                    onClick = { showToPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Result", style = MaterialTheme.typography.labelMedium)
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    } else if (state.error != null) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(
                            text = state.result?.toCurrencyString(state.toCurrency) ?: "—",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Button(
                onClick = viewModel::convert,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Refresh Rates")
            }
        }
    }
}

@Composable
private fun CurrencySelector(code: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CurrencyPickerDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                SUPPORTED_CURRENCIES.forEach { code ->
                    TextButton(onClick = { onSelect(code); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                        Text(code, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {}
    )
}