package com.paiban.helper.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPageContractTest {
    @Test
    fun historyUsesPageTitleFromHeaderInsteadOfContentHeading() {
        assertEquals("历史", historyPageTitle())
    }

    @Test
    fun historyAccessibilityActionsStayExplicitAndOrdered() {
        assertEquals(listOf("预览", "编辑", "删除"), historyAccessibilityActionLabels())
    }
}
