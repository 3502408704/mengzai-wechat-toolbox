package com.paiban.helper

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.paiban.helper.navigation.AppNavGraph
import com.paiban.helper.ui.common.captureSystemBarInsets
import com.paiban.helper.ui.onboarding.OnboardingScreen
import com.paiban.helper.ui.settings.SettingsViewModel
import com.paiban.helper.ui.theme.PaibanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        findViewById<View>(android.R.id.content)?.captureSystemBarInsets { _, _ ->
            // Compose pages consume these insets dynamically. This listener is kept as a
            // concrete View-based reference so future hybrid screens avoid hardcoded bar sizes.
        }
        setContent {
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    PaibanTheme(
        themeMode = settingsState.preferences.themeMode,
        dynamicColor = settingsState.preferences.dynamicColor,
    ) {
        if (settingsState.preferences.onboardingCompleted) {
            AppNavGraph(navController = navController)
        } else {
            OnboardingScreen(onFinish = settingsViewModel::completeOnboarding)
        }
    }
}
