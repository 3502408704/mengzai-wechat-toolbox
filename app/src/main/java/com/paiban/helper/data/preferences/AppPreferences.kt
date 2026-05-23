package com.paiban.helper.data.preferences

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val editorFontScale: Float = 1f,
    val showLineNumbers: Boolean = true,
    val developerMode: Boolean = false,
    val onboardingCompleted: Boolean = false,
)
