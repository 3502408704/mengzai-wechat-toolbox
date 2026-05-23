package com.paiban.helper.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class AiAssistantContractTest {
    @Test
    fun aiPageShowsExperimentalStatusInSubtitle() {
        assertEquals("实验室功能，即将开放", aiAssistantSubtitle())
    }

    @Test
    fun aiCardAccessibilityCopyMatchesVisibleStatusWording() {
        assertEquals("AI 辅助，即将开放", aiAssistantCardContentDescription())
    }
}
