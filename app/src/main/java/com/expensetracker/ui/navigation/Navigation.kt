package com.expensetracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expensetracker.ui.screens.addexpense.AddEditExpenseScreen
import com.expensetracker.ui.screens.budget.BudgetScreen
import com.expensetracker.ui.screens.converter.CurrencyConverterScreen
import com.expensetracker.ui.screens.dashboard.DashboardScreen
import com.expensetracker.ui.screens.history.HistoryScreen
import com.expensetracker.ui.screens.recurring.RecurringExpensesScreen
import com.expensetracker.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard          : Screen("dashboard")
    object AddExpense         : Screen("add_expense")
    object EditExpense        : Screen("edit_expense/{expenseId}") {
        fun createRoute(id: Long) = "edit_expense/$id"
    }
    object History            : Screen("history")
    object Budget             : Screen("budget")
    object RecurringExpenses  : Screen("recurring")
    object CurrencyConverter  : Screen("converter")
    object Settings           : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        enterTransition = {
            fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220))
        },
        exitTransition = {
            fadeOut(tween(220)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220))
        },
        popEnterTransition = {
            fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(220))
        },
        popExitTransition = {
            fadeOut(tween(220)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(220))
        }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.AddExpense.route) {
            AddEditExpenseScreen(navController = navController, expenseId = null)
        }
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("expenseId")
            AddEditExpenseScreen(navController = navController, expenseId = id)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.Budget.route) {
            BudgetScreen(navController = navController)
        }
        composable(Screen.RecurringExpenses.route) {
            RecurringExpensesScreen(navController = navController)
        }
        composable(Screen.CurrencyConverter.route) {
            CurrencyConverterScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}