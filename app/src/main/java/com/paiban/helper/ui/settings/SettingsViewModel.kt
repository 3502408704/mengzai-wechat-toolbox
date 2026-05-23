package com.paiban.helper.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paiban.helper.data.preferences.AppPreferences
import com.paiban.helper.data.preferences.ThemeMode
import com.paiban.helper.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observePreferences().collect { preferences ->
                _uiState.value = SettingsUiState(preferences)
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.updateThemeMode(mode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateDynamicColor(enabled) }
    }

    fun updateDeveloperMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateDeveloperMode(enabled) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.updateOnboardingCompleted(true) }
    }
}
