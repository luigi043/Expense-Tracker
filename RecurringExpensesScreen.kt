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

    val uiState: StateFlow<RecurringUiState> = combine(
        repository.getAllRecurring(),
        appPreferences.baseCurrency
    ) { items, currency ->
        RecurringUiState(isLoading = false, items = items, currency = currency)
    }.catch { e ->
        emit(RecurringUiState(isLoading = false, error = e.message))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringUiState())

    fun add(
        amount: Double,
        category: ExpenseCategory,
        type: TransactionType,
        note: String,
        intervalDays: Int,
        currency: String
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
            addRecurring(recurring)
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
}

// ─────────────────────────────────────────────────────────────────────────────

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

    if (showAddDialog) {
        AddRecurringDialog(
            currency = state.currency,
            onDismiss = { showAddDialog = false },
            onSave = { amount, cat, type, note, intervalDays, currency ->
                viewModel.add(amount, cat, type, note, intervalDays, currency)
                showAddDialog = false
            }
        )
    }

    Scaffold(
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
                        onDelete = { viewModel.delete(item) }
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
                    text = item.amount.toCurrencyString(currency),
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
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
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
    var selectedCat by remember { mutableStateOf(ExpenseCategory.HOUSING) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var note by remember { mutableStateOf("") }
    var intervalIndex by remember { mutableStateOf(3) } // Monthly by default
    var catExpanded by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type toggle
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
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Amount") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Category
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(
                        value = "${selectedCat.emoji} ${selectedCat.label}", onValueChange = {},
                        readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
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
                // Interval
                ExposedDropdownMenuBox(expanded = intervalExpanded, onExpandedChange = { intervalExpanded = it }) {
                    OutlinedTextField(
                        value = RECURRING_INTERVALS[intervalIndex].second, onValueChange = {},
                        readOnly = true, label = { Text("Repeats") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(intervalExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
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
                    value = note, onValueChange = { note = it },
                    label = { Text("Note (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val a = amount.toDoubleOrNull() ?: return@Button
                onSave(a, selectedCat, selectedType, note, RECURRING_INTERVALS[intervalIndex].first, currency)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}