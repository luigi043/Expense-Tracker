package com.expensetracker.ui.screens.dashboard

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.expensetracker.domain.model.DailyTrend
import com.expensetracker.domain.model.ExpenseCategory
import com.expensetracker.ui.components.*
import com.expensetracker.ui.navigation.Screen
import com.expensetracker.ui.theme.AppColors
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.route) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Transaction") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Dashboard.route)
        }
    ) { padding ->
        when {
            state.isLoading -> FullScreenLoading(modifier = Modifier.padding(padding))
            state.error != null -> ErrorState(message = state.error!!, modifier = Modifier.padding(padding))
            else -> {
                val summary = state.summary ?: return@Scaffold
                val monthLabel = Month.of(state.selectedMonth)
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " ${state.selectedYear}"

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    item {
                        GradientBalanceHeader(
                            balance = summary.balance,
                            totalIncome = summary.totalIncome,
                            totalExpense = summary.totalExpense,
                            currency = state.currency,
                            monthLabel = monthLabel
                        )
                    }

                    item {
                        MonthSelector(
                            label = monthLabel,
                            onPrevious = viewModel::previousMonth,
                            onNext = viewModel::nextMonth,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Text("Totals and charts include ${state.currency} transactions only. Other currencies remain in history.",
                            modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall)
                    }

                    if (summary.categoryBreakdown.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Spending by Category",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        item {
                            ExpensePieChart(
                                data = summary.categoryBreakdown,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }

                    if (summary.dailyTrend.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Daily Trend",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        item {
                            DailyBarChart(
                                data = summary.dailyTrend,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }

                    item {
                        SectionHeader(
                            title = "Recent Transactions",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            action = {
                                TextButton(onClick = { navController.navigate(Screen.History.route) }) {
                                    Text("See all")
                                }
                            }
                        )
                    }

                    if (summary.recentTransactions.isEmpty()) {
                        item {
                            EmptyState(
                                emoji = "💸",
                                title = "No transactions yet",
                                subtitle = "Tap + to log your first expense or income"
                            )
                        }
                    } else {
                        items(summary.recentTransactions, key = { it.id }) { expense ->
                            TransactionItem(
                                expense = expense,
                                currency = state.currency,
                                onClick = {
                                    navController.navigate(Screen.EditExpense.createRoute(expense.id))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
fun ExpensePieChart(
    data: Map<ExpenseCategory, Double>,
    modifier: Modifier = Modifier
) {
    val chartColors = AppColors.Chart.map { it.toArgb() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PieChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 48f
                transparentCircleRadius = 52f
                setDrawCenterText(true)
                centerText = "Expenses"
                legend.isEnabled = true
                setEntryLabelTextSize(11f)
                setUsePercentValues(true)
                animateY(1000)
            }
        },
        update = { chart ->
            val entries = data.entries.map { (cat, amount) ->
                PieEntry(amount.toFloat(), "${cat.emoji} ${cat.label}")
            }
            val dataSet = PieDataSet(entries, "").apply {
                colors = chartColors.take(entries.size)
                valueTextSize = 10f
                sliceSpace = 2f
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun DailyBarChart(
    data: List<DailyTrend>,
    modifier: Modifier = Modifier
) {
    val barColor = AppColors.Emerald.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.isEnabled = false
                setPinchZoom(false)
                setDrawGridBackground(false)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                }
                axisLeft.setDrawGridLines(true)
                axisRight.isEnabled = false
                animateY(800)
            }
        },
        update = { chart ->
            val labels = data.map { it.date.dayOfMonth.toString() }
            val entries = data.mapIndexed { i, trend ->
                BarEntry(i.toFloat(), trend.amount.toFloat())
            }
            val dataSet = BarDataSet(entries, "Daily Spending").apply {
                color = barColor
                setDrawValues(false)
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = BarData(dataSet).apply { barWidth = 0.6f }
            chart.invalidate()
        }
    )
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AppBottomBar(navController: NavController, currentRoute: String) {
    val items = listOf(
        Triple(Screen.Dashboard.route, Icons.Default.Home, "Home"),
        Triple(Screen.History.route, Icons.Default.History, "History"),
        Triple(Screen.Budget.route, Icons.Default.DonutLarge, "Budget"),
        Triple(Screen.Settings.route, Icons.Default.Settings, "Settings"),
    )
    NavigationBar {
        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
