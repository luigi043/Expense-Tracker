package com.expensetracker.ui.screens.addexpense

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.ui.components.AppTopBar
import com.expensetracker.ui.theme.AppColors
import com.expensetracker.utils.SUPPORTED_CURRENCIES
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    navController: NavController,
    expenseId: Long?,
    viewModel: AddEditExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(expenseId) {
        expenseId?.let { viewModel.loadExpense(it) }
    }

    LaunchedEffect(state.isSaved, state.isDeleted) {
        if (state.isSaved || state.isDeleted) navController.popBackStack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.toEpochDay() * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        viewModel.onDateChange(
                            LocalDate.ofEpochDay(ms / 86_400_000L)
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("This cannot be undone. Delete this transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    expenseId?.let { viewModel.delete(it) }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    SUPPORTED_CURRENCIES.forEach { code ->
                        TextButton(
                            onClick = {
                                viewModel.onCurrencyChange(code)
                                showCurrencyPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(code, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = if (state.isEditMode) "Edit Transaction" else "Add Transaction",
                onBack = { navController.popBackStack() },
                actions = {
                    if (state.isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TypeToggle(
                selected = state.type,
                onSelect = viewModel::onTypeChange
            )

            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount") },
                leadingIcon = {
                    Text(
                        text = state.currency,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                trailingIcon = {
                    TextButton(onClick = { showCurrencyPicker = true }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Change currency")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val categories = if (state.type == TransactionType.INCOME)
                ExpenseCategory.incomeCategories else ExpenseCategory.expenseCategories
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    CategoryChip(
                        category = cat,
                        isSelected = state.category == cat,
                        onClick = { viewModel.onCategoryChange(cat) }
                    )
                }
            }

            OutlinedTextField(
                value = state.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                onValueChange = {},
                label = { Text("Date") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Pick date")
                    }
                }
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                supportingText = { Text("${state.note.length}/500") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mark as Recurring",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Set up automatic logging in Recurring screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isRecurring,
                    onCheckedChange = viewModel::onRecurringChange
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.save(expenseId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        if (state.isEditMode) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (state.isEditMode) "Save Changes" else "Add Transaction",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeToggle(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionType.values().forEach { type ->
            val isSelected = selected == type
            val color = if (type == TransactionType.INCOME) AppColors.Success else AppColors.Error
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(type) },
                label = {
                    Text(
                        text = if (type == TransactionType.INCOME) "💰 Income" else "💸 Expense",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text("${category.emoji} ${category.label}") },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}