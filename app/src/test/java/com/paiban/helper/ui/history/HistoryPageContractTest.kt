package com.paiban.helper.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPageContractTest {
    @Test
    fun historyAccessibilityActionsStayExplicitAndOrdered() {
        assertEquals(listOf("\u7f16\u8f91", "\u5220\u9664"), historyActionLabels())
    }
}
