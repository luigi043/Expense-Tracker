package com.expensetracker.ui.screens.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.domain.model.RecurringExpense
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.model.parsePositiveAmount
import com.expensetracker.ui.components.*
import com.expensetracker.ui.theme.AppColors
import com.expensetracker.utils.RECURRING_INTERVALS
import com.expensetracker.utils.Formatters.toFullDate
import com.expensetracker.utils.Formatters.toCurrencyString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    navController: NavController,
    viewModel: RecurringExpensesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<RecurringExpense?>(null) }
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHost.showSnackbar(it); viewModel.clearError() }
    }
    pendingDelete?.let { entry ->
        AlertDialog(onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Recurring Entry") },
            text = { Text("Future automatic logging will stop. Existing transactions will remain.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(entry); pendingDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } })
    }

    if (showAddDialog) {
        AddRecurringDialog(
            currency = state.currency,
            onDismiss = { showAddDialog = false },
            onSave = { amount, cat, type, note, intervalDays, currency ->
                viewModel.add(amount, cat, type, note, intervalDays, currency) { showAddDialog = false }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            AppTopBar(
                title = "Recurring Expenses",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add recurring")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🔄",
                        title = "No recurring expenses",
                        subtitle = "Add recurring entries like rent, subscriptions, or salaries"
                    )
                }
            } else {
                items(state.items, key = { it.id }) { item ->
                    RecurringCard(
                        item = item,
                        currency = state.currency,
                        onToggle = { viewModel.toggle(item) },
                        onDelete = { pendingDelete = item }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecurringCard(
    item: RecurringExpense,
    currency: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(item.category.emoji, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.category.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.amount.toCurrencyString(item.currency)} ${item.currency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.type == TransactionType.INCOME) AppColors.Success else AppColors.Error,
                    fontWeight = FontWeight.SemiBold
                )
                val intervalLabel = RECURRING_INTERVALS.find { it.first == item.intervalDays }?.second
                    ?: "Every ${item.intervalDays} days"
                Text(
                    text = "$intervalLabel · Next: ${item.nextDueDate.toFullDate()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = item.isActive, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete recurring entry", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecurringDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Double, ExpenseCategory, TransactionType, String, Int, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf(ExpenseCategory.HOUSING) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var note by remember { mutableStateOf("") }
    var intervalIndex by remember { mutableStateOf(3) }
    var catExpanded by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.values().forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(if (t == TransactionType.INCOME) "Income" else "Expense") }
                        )
                    }
                }
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it; amountError = false },
                    label = { Text("Amount") }, singleLine = true,
                    isError = amountError,
                    supportingText = if (amountError) { { Text("Enter a valid positive amount") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(
                        value = "${selectedCat.emoji} ${selectedCat.label}", onValueChange = {},
                        readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        ExpenseCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.emoji} ${cat.label}") },
                                onClick = { selectedCat = cat; catExpanded = false }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = intervalExpanded, onExpandedChange = { intervalExpanded = it }) {
                    OutlinedTextField(
                        value = RECURRING_INTERVALS[intervalIndex].second, onValueChange = {},
                        readOnly = true, label = { Text("Repeats") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(intervalExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = intervalExpanded, onDismissRequest = { intervalExpanded = false }) {
                        RECURRING_INTERVALS.forEachIndexed { i, (_, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { intervalIndex = i; intervalExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = note, onValueChange = { note = it.take(500) },
                    label = { Text("Note (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val a = amount.parsePositiveAmount()
                if (a == null) { amountError = true; return@Button }
                onSave(a, selectedCat, selectedType, note, RECURRING_INTERVALS[intervalIndex].first, currency)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
