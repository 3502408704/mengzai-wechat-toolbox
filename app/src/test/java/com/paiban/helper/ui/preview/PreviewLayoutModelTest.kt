package com.paiban.helper.ui.preview

import androidx.compose.ui.unit.dp
import com.paiban.helper.ui.editor.pageSnackbarBottomPadding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewLayoutModelTest {
    @Test
    fun previewPrimaryActionUsesPublishCopyLabel() {
        assertEquals("复制富文本", previewPrimaryActionLabel())
    }

    @Test
    fun previewSubtitleReflectsCurrentSource() {
        assertEquals("来自当前草稿", previewSourceSubtitle(PreviewRouteSource.Editor))
        assertEquals("来自历史记录", previewSourceSubtitle(PreviewRouteSource.History(8L)))
    }

    @Test
    fun previewTitlebarExposesBackNavigationLabel() {
        assertEquals("返回", previewBackNavigationContentDescription())
    }

    @Test
    fun previewReadingRegionUsesResultBrowsingSemantics() {
        val semantics = previewReadingRegionSemantics()

        assertEquals("公众号排版效果预览", semantics.label)
        assertTrue(semantics.isScrollable)
        assertFalse(semantics.isEditable)
        assertTrue(semantics.isZoomable)
        assertFalse(semantics.interceptsExploreByTouch)
    }

    @Test
    fun previewZoomUsesDiscreteAccessibilityFriendlyStops() {
        assertEquals(listOf(85, 100, 115, 130, 150, 175), previewZoomSteps())
    }

    @Test
    fun previewOverflowMenuCollectsLowFrequencyActions() {
        assertEquals(listOf("分享", "导出", "恢复原始比例"), previewOverflowActionLabels())
    }

    @Test
    fun immersivePreviewHidesTopActionsButKeepsZoomAvailable() {
        val state = previewChromeState(
            immersiveMode = true,
            topActionsRevealed = false,
            isAtBottom = false,
            hasContent = true,
        )

        assertFalse(state.showTopActions)
        assertTrue(state.showZoomStrip)
        assertFalse(state.showCopyAction)
    }

    @Test
    fun immersivePreviewShowsTopActionsAfterFastDownwardReveal() {
        assertTrue(shouldRevealPreviewTopActions(scrollDeltaY = -96))
        assertFalse(shouldRevealPreviewTopActions(scrollDeltaY = -24))
    }

    @Test
    fun copyActionAppearsOnlyAtBottomWhenContentExists() {
        assertFalse(
            previewChromeState(
                immersiveMode = false,
                topActionsRevealed = true,
                isAtBottom = false,
                hasContent = true,
            ).showCopyAction
        )
        assertTrue(
            previewChromeState(
                immersiveMode = false,
                topActionsRevealed = true,
                isAtBottom = true,
                hasContent = true,
            ).showCopyAction
        )
    }

    @Test
    fun previewSnackbarAlsoClearsPersistentBottomAction() {
        assertEquals(112.dp, pageSnackbarBottomPadding())
    }

    @Test
    fun copyCanBeDisabledWhenPublishHtmlIsUnavailable() {
        val state = PreviewUiState(
            htmlDocument = "<html><body>Doc</body></html>",
            publishHtml = "",
            isCopyEnabled = false,
        )

        assertTrue(state.clipboardHtml().isEmpty())
    }
}
