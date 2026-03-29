package com.expensetracker.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val isDarkMode: Flow<Boolean> =
        context.dataStore.data.map { it[DARK_MODE] ?: false }

    val baseCurrency: Flow<String> =
        context.dataStore.data.map { it[BASE_CURRENCY] ?: "USD" }

    val isOnboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setBaseCurrency(currency: String) {
        context.dataStore.edit { it[BASE_CURRENCY] = currency }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[ONBOARDING_DONE] = done }
    }
}