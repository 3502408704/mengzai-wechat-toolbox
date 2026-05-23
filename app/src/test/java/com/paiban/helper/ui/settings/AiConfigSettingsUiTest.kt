package com.paiban.helper.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AiConfigSettingsUiTest {
    @Test
    fun builtInConfigIsReadOnlyWithoutSecretActions() {
        val rows = buildAiConfigRows(
            listOf(
                AiConfigRowInput(
                    id = 1L,
                    displayName = "DeepSeek 默认",
                    model = "deepseek-chat",
                    isBuiltIn = true,
                    apiKeyMasked = "••••••••",
                )
            )
        )

        val row = rows.single()

        assertEquals(1L, row.id)
        assertFalse(row.canDelete)
        assertFalse(row.canRevealApiKey)
        assertFalse(row.exposesApiKey)
    }
}
