package com.paiban.helper.ui.preview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PreviewCopyPayloadTest {
    @Test
    fun previewCopyUsesPublishHtmlAndCanBeDisabledWhenUnavailable() {
        val state = PreviewUiState(
            htmlDocument = "<html><body><div>Doc</div></body></html>",
            publishHtml = "",
            plainText = "Doc",
            isEmpty = false,
            isCopyEnabled = false,
        )

        assertFalse(state.isCopyEnabled)
        assertEquals("", state.clipboardHtml())
    }

    @Test
    fun previewCopyUsesPublishHtmlPayload() {
        val state = PreviewUiState(
            htmlDocument = "<html><body><div>Doc</div></body></html>",
            publishHtml = "<style>.x{color:red}</style><div class=\"x\">Doc</div>",
            plainText = "Doc",
            isEmpty = false,
            isCopyEnabled = true,
        )

        assertEquals("<style>.x{color:red}</style><div class=\"x\">Doc</div>", state.clipboardHtml())
    }
}
