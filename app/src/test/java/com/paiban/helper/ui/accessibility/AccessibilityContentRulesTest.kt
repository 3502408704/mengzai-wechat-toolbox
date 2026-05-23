package com.paiban.helper.ui.accessibility

import com.paiban.helper.ui.editor.editorHeaderSubtitle
import com.paiban.helper.ui.history.historyPageTitle
import com.paiban.helper.ui.ai.aiAssistantCardContentDescription
import com.paiban.helper.ui.ai.aiAssistantSubtitle
import com.paiban.helper.ui.preview.PreviewRouteSource
import com.paiban.helper.ui.preview.previewSourceSubtitle
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityContentRulesTest {
    @Test
    fun pageHeadersExposeStableAccessibleCopy() {
        assertEquals("历史", historyPageTitle())
        assertEquals("未命名草稿", editorHeaderSubtitle(""))
        assertEquals("未命名草稿", editorHeaderSubtitle("   "))
        assertEquals("来自当前草稿", previewSourceSubtitle(PreviewRouteSource.Editor))
        assertEquals("来自历史记录", previewSourceSubtitle(PreviewRouteSource.History(1L)))
    }

    @Test
    fun aiPageExposesStableAccessibleCopy() {
        assertEquals("实验室功能，即将开放", aiAssistantSubtitle())
        assertEquals("AI 辅助，即将开放", aiAssistantCardContentDescription())
    }
}
