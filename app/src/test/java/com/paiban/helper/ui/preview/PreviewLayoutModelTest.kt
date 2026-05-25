package com.paiban.helper.ui.preview

import androidx.compose.ui.unit.dp
import com.paiban.helper.ui.editor.pageSnackbarBottomPadding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewLayoutModelTest {
    @Test
    fun previewSubtitleReflectsCurrentSource() {
        assertEquals("\u6765\u81ea\u5f53\u524d\u8349\u7a3f", previewSourceSubtitle(PreviewRouteSource.Editor))
        assertEquals("\u6765\u81ea\u5386\u53f2\u8bb0\u5f55", previewSourceSubtitle(PreviewRouteSource.History(8L)))
    }

    @Test
    fun previewRegionStateDescriptionReflectsZoom() {
        assertEquals("\u53ea\u8bfb\uff0c\u53ef\u6eda\u52a8\uff0c\u5f53\u524d\u7f29\u653e 120%", previewRegionStateDescription(120))
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
