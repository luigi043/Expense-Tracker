package com.expensetracker.ui.screens.history

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.ui.components.*
import com.expensetracker.ui.navigation.Screen
import com.expensetracker.ui.screens.dashboard.AppBottomBar
import com.expensetracker.utils.Formatters.toCurrencyString
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showFilters by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(state.exportStatus) {
        when (val s = state.exportStatus) {
            is ExportStatus.Success -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(s.uri, s.mimeType)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (_: android.content.ActivityNotFoundException) {
                    snackbarHostState.showSnackbar("Report created. Install an app that opens this file type to view it.")
                }
                viewModel.clearExportStatus()
            }
            is ExportStatus.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.clearExportStatus()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Transaction History",
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export CSV") },
                                onClick = { viewModel.exportCsv(); showExportMenu = false },
                                leadingIcon = { Icon(Icons.Default.TableChart, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export PDF") },
                                onClick = { viewModel.exportPdf(); showExportMenu = false },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.History.route)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.updateFilter(state.filter.copy(searchQuery = it))
                },
                placeholder = { Text("Search by category or note…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.updateFilter(state.filter.copy(searchQuery = ""))
                        }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            AnimatedVisibility(visible = showFilters) {
                FilterPanel(
                    filter = state.filter,
                    onFilterChange = viewModel::updateFilter,
                    onClear = { viewModel.clearFilter(); searchQuery = "" },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            val expenses = state.filteredExpenses
            if (expenses.isNotEmpty()) {
                val totals = expenses.groupBy { it.currency }.toSortedMap().map { (currency, entries) ->
                    val total = entries.sumOf { if (it.type == TransactionType.EXPENSE) -it.amount else it.amount }
                    "$currency ${total.toCurrencyString(currency)}"
                }.joinToString(" / ")
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${expenses.size} transactions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Net: $totals",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (state.exportStatus is ExportStatus.Exporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when {
                state.isLoading -> FullScreenLoading()
                expenses.isEmpty() -> {
                    EmptyState(
                        emoji = "🔍",
                        title = "No transactions found",
                        subtitle = "Try adjusting your filters or add a new transaction",
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val grouped = expenses.groupBy { it.date }
                        grouped.forEach { (date, items) ->
                            item(key = date.toString()) {
                                Text(
                                    text = date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(items, key = { it.id }) { expense ->
                                TransactionItem(
                                    expense = expense,
                                    currency = state.currency,
                                    onClick = {
                                        navController.navigate(Screen.EditExpense.createRoute(expense.id))
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPanel(
    filter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy")

    if (showFromDatePicker) {
        val dpState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { ms ->
                        onFilterChange(filter.copy(from = LocalDate.ofEpochDay(ms / 86_400_000L)))
                    }
                    showFromDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    if (showToDatePicker) {
        val dpState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { ms ->
                        onFilterChange(filter.copy(to = LocalDate.ofEpochDay(ms / 86_400_000L)))
                    }
                    showToDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Filters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null, TransactionType.EXPENSE, TransactionType.INCOME).forEach { type ->
                    FilterChip(
                        selected = filter.type == type,
                        onClick = { onFilterChange(filter.copy(type = type)) },
                        label = { Text(type?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All") }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showFromDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(filter.from?.format(fmt) ?: "From date")
                }
                OutlinedButton(
                    onClick = { showToDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(filter.to?.format(fmt) ?: "To date")
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filter.category == null,
                        onClick = { onFilterChange(filter.copy(category = null)) },
                        label = { Text("All") }
                    )
                }
                items(ExpenseCategory.values().toList()) { cat ->
                    FilterChip(
                        selected = filter.category == cat,
                        onClick = { onFilterChange(filter.copy(category = cat)) },
                        label = { Text("${cat.emoji} ${cat.label}") }
                    )
                }
            }

            TextButton(onClick = onClear) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Clear all filters")
            }
        }
    }
}
