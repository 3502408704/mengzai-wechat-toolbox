package com.paiban.helper.ui.accessibility

import com.paiban.helper.ui.editor.editorHeaderSubtitle
import com.paiban.helper.ui.history.historyPageTitle
import com.paiban.helper.ui.preview.PreviewRouteSource
import com.paiban.helper.ui.preview.previewSourceSubtitle
import com.paiban.helper.ui.workbench.WorkbenchMode
import com.paiban.helper.ui.workbench.aiTeaserStateDescription
import com.paiban.helper.ui.workbench.aiTeaserTitle
import com.paiban.helper.ui.workbench.label
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
    fun workbenchAndAiTeaserExposeStableAccessibleCopy() {
        assertEquals("创作", WorkbenchMode.Create.label())
        assertEquals("管理", WorkbenchMode.Manage.label())
        assertEquals("设置", WorkbenchMode.Settings.label())
        assertEquals("AI 辅助编辑，即将开放", aiTeaserTitle())
        assertEquals("即将开放，不可用", aiTeaserStateDescription())
    }
}
