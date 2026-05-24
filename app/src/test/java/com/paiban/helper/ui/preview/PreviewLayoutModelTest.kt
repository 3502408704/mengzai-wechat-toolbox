package com.paiban.helper.ui.preview

import androidx.compose.ui.unit.dp
import com.paiban.helper.ui.editor.pageSnackbarBottomPadding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewLayoutModelTest {
    @Test
    fun previewSubtitleReflectsCurrentSource() {
        assertEquals("来自当前草稿", previewSourceSubtitle(PreviewRouteSource.Editor))
        assertEquals("来自历史记录", previewSourceSubtitle(PreviewRouteSource.History(8L)))
    }

    @Test

    @Test
    fun previewRegionStateDescriptionReflectsZoom() {
        assertEquals("只读，可滚动，当前缩放 120%", previewRegionStateDescription(120))
    }

    @Test
    fun previewZoomLabelFormatsAsPercentage() {
        assertEquals("100%", previewZoomLabel(100))
        assertEquals("85%", previewZoomLabel(85))
        assertEquals("150%", previewZoomLabel(150))
    }

    @Test
    fun previewSnackbarKeepsConsistentBottomPadding() {
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
