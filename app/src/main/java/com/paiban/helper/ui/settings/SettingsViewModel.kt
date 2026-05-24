package com.paiban.helper.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paiban.helper.data.preferences.AppPreferences
import com.paiban.helper.data.preferences.ThemeMode
import com.paiban.helper.data.repository.AiSettingsRepository
import com.paiban.helper.data.repository.AiConfigSummary
import com.paiban.helper.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val aiConfigs: List<AiConfigSummary> = emptyList(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val aiSettingsRepository: AiSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observePreferences().collect { preferences ->
                _uiState.update { it.copy(preferences = preferences) }
            }
        }
        viewModelScope.launch {
            aiSettingsRepository.observeConfigs().collect { configs ->
                _uiState.update { it.copy(aiConfigs = configs) }
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

    // ── AI 配置管理 ──

    fun addAiConfig(
        displayName: String,
        provider: String,
        model: String,
        baseUrl: String,
        apiKey: String,
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            aiSettingsRepository.upsertConfig(
                summary = AiConfigSummary(
                    id = now,
                    displayName = displayName,
                    provider = provider,
                    model = model,
                    baseUrl = baseUrl,
                    isBuiltIn = false,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                ),
                apiKeyPlainText = apiKey,
            )
        }
    }

    fun deleteAiConfig(id: Long) {
        viewModelScope.launch { aiSettingsRepository.deleteConfig(id) }
    }
}

