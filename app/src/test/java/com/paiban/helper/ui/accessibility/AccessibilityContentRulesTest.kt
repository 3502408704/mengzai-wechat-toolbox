package com.paiban.helper.ui.accessibility

import com.paiban.helper.ui.editor.editorHeaderSubtitle
import com.paiban.helper.ui.preview.PreviewRouteSource
import com.paiban.helper.ui.preview.previewSourceSubtitle
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityContentRulesTest {
    @Test
    fun pageHeadersExposeStableAccessibleCopy() {
        assertEquals("\u672a\u547d\u540d\u8349\u7a3f", editorHeaderSubtitle(""))
        assertEquals("\u672a\u547d\u540d\u8349\u7a3f", editorHeaderSubtitle("   "))
        assertEquals("\u6765\u81ea\u5f53\u524d\u8349\u7a3f", previewSourceSubtitle(PreviewRouteSource.Editor))
        assertEquals("\u6765\u81ea\u5386\u53f2\u8bb0\u5f55", previewSourceSubtitle(PreviewRouteSource.History(1L)))
    }
}
