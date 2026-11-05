package com.expensetracker

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.expensetracker.data.local.datastore.AppPreferences
import com.expensetracker.ui.navigation.AppNavigation
import com.expensetracker.ui.theme.ExpenseTrackerTheme
import com.expensetracker.utils.withEnglishUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withEnglishUi())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by appPreferences.isDarkMode.collectAsState(initial = isSystemInDarkTheme())

            SideEffect {
                val style = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                ) { isDarkMode }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            ExpenseTrackerTheme(darkTheme = isDarkMode) {
                AppNavigation()
            }
        }
    }
}
