package com.paiban.helper.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppPreferencesTest {
    @Test
    fun defaultsMatchExpectedValues() {
        val preferences = AppPreferences()

        assertEquals(ThemeMode.System, preferences.themeMode)
        assertTrue(preferences.dynamicColor)
        assertEquals(1f, preferences.editorFontScale)
        assertTrue(preferences.showLineNumbers)
        assertFalse(preferences.developerMode)
        assertFalse(preferences.onboardingCompleted)
    }
}
