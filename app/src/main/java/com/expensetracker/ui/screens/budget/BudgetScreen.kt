package com.expensetracker.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.ui.components.*
import com.expensetracker.ui.navigation.Screen
import com.expensetracker.ui.screens.dashboard.AppBottomBar
import com.expensetracker.ui.theme.AppColors
import com.expensetracker.utils.Formatters.toCurrencyString
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddBudgetDialog(
            currency = state.currency,
            onDismiss = { showAddDialog = false },
            onSave = { category, limit ->
                viewModel.saveBudget(category, limit)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Budgets",
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add budget")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add budget")
            }
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Budget.route)
        }
    ) { padding ->
        val monthLabel = Month.of(state.month).getDisplayName(TextStyle.FULL, Locale.getDefault()) +
            " ${state.year}"

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Budgets for $monthLabel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                val totalLimit = state.budgets.sumOf { it.monthlyLimit }
                val totalSpent = state.budgets.sumOf { it.spent }
                BudgetOverviewCard(
                    totalLimit = totalLimit,
                    totalSpent = totalSpent,
                    currency = state.currency
                )
            }

            if (state.budgets.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "📊",
                        title = "No budgets set",
                        subtitle = "Tap + to set your first monthly budget and track spending"
                    )
                }
            } else {
                items(state.budgets, key = { "${it.category.name}_${it.month}_${it.year}" }) { budget ->
                    BudgetProgressCard(
                        category = budget.category.label,
                        emoji = budget.category.emoji,
                        spent = budget.spent,
                        limit = budget.monthlyLimit,
                        currency = state.currency
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetOverviewCard(
    totalLimit: Double,
    totalSpent: Double,
    currency: String
) {
    val remaining = totalLimit - totalSpent
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BudgetMetric("Total Limit", totalLimit, currency)
            VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            BudgetMetric("Spent", totalSpent, currency)
            VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            BudgetMetric(
                label = "Remaining",
                amount = remaining,
                currency = currency,
                color = if (remaining < 0) AppColors.Error else AppColors.Success
            )
        }
    }
}

@Composable
private fun BudgetMetric(
    label: String,
    amount: Double,
    currency: String,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = amount.toCurrencyString(currency),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseCategory, Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var limitText by remember { mutableStateOf("") }
    var limitError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.emoji} ${selectedCategory.label}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ExpenseCategory.expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.emoji} ${cat.label}") },
                                onClick = { selectedCategory = cat; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it
                        limitError = if (it.toDoubleOrNull() == null || (it.toDoubleOrNull() ?: 0.0) <= 0)
                            "Enter a valid amount" else null
                    },
                    label = { Text("Monthly Limit ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = limitError != null,
                    supportingText = limitError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull()
                    if (limit != null && limit > 0) {
                        onSave(selectedCategory, limit)
                    } else {
                        limitError = "Enter a valid amount"
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}