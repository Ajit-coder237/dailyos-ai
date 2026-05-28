package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_os_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_CURRENCY = stringPreferencesKey("currency")
        val KEY_REMINDER_TIME = stringPreferencesKey("reminder_time")
        val KEY_FOCUS_DURATION = intPreferencesKey("focus_duration")
        val KEY_MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: "Innovator"
    }

    val theme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "System"
    }

    val currency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY] ?: "NPR"
    }

    val reminderTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_REMINDER_TIME] ?: "21:00"
    }

    val focusDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_FOCUS_DURATION] ?: 25
    }

    val monthlyBudget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[KEY_MONTHLY_BUDGET] ?: 5000.0
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENCY] = currency
        }
    }

    suspend fun setReminderTime(timeString: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMINDER_TIME] = timeString
        }
    }

    suspend fun setFocusDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FOCUS_DURATION] = minutes
        }
    }

    suspend fun setMonthlyBudget(budget: Double) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MONTHLY_BUDGET] = budget
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
