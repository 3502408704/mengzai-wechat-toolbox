package com.paiban.helper.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.paiban.helper.data.preferences.AppPreferences
import com.paiban.helper.data.preferences.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    fun observePreferences(): Flow<AppPreferences> = dataStore.data.map { it.toAppPreferences() }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.themeMode] = themeMode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.dynamicColor] = enabled }
    }

    suspend fun updateEditorFontScale(scale: Float) {
        dataStore.edit { it[Keys.editorFontScale] = scale }
    }

    suspend fun updateShowLineNumbers(show: Boolean) {
        dataStore.edit { it[Keys.showLineNumbers] = show }
    }

    suspend fun updateDeveloperMode(enabled: Boolean) {
        dataStore.edit { it[Keys.developerMode] = enabled }
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.onboardingCompleted] = completed }
    }

    private fun Preferences.toAppPreferences(): AppPreferences {
        return AppPreferences(
            themeMode = ThemeMode.entries.firstOrNull { it.name == this[Keys.themeMode] } ?: ThemeMode.System,
            dynamicColor = this[Keys.dynamicColor] ?: true,
            editorFontScale = this[Keys.editorFontScale] ?: 1f,
            showLineNumbers = this[Keys.showLineNumbers] ?: true,
            developerMode = this[Keys.developerMode] ?: false,
            onboardingCompleted = this[Keys.onboardingCompleted] ?: false,
        )
    }

    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val editorFontScale = floatPreferencesKey("editor_font_scale")
        val showLineNumbers = booleanPreferencesKey("show_line_numbers")
        val developerMode = booleanPreferencesKey("developer_mode")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }

    companion object {
        fun createDataStore(context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile("app_preferences.preferences_pb") }
            )
        }
    }
}
