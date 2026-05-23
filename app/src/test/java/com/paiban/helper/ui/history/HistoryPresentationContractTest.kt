package com.paiban.helper.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPresentationContractTest {
    @Test
    fun historyActionsExposePreviewEditDeleteInThatOrder() {
        assertEquals(listOf("预览", "编辑", "删除"), historyAccessibilityActionLabels())
    }
}
