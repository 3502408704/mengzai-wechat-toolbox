package com.paiban.helper.ui.common

import com.paiban.helper.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun topLevelRoutesExposeLegacyPrimaryPagesAndPreviewChildren() {
        assertEquals(
            listOf("editor", "history", "templates", "ai", "settings", "preview/editor", "preview/history/{historyId}", "help"),
            AppDestination.entries.map { it.route },
        )
    }

    @Test
    fun editorReturnsAsThePrimaryTopLevelDestination() {
        assertEquals("editor", AppDestination.Editor.route)
        assertEquals("编辑", AppDestination.Editor.label)
    }

    @Test
    fun bottomNavigationRestoresPrimaryPageSetWithoutPreviewOrTemplates() {
        assertEquals(
            listOf(AppDestination.Editor, AppDestination.History, AppDestination.Settings),
            AppDestination.topLevelDestinations(),
        )
    }

    @Test
    fun previewRoutesExposeSourceSpecificPaths() {
        assertEquals("preview/editor", AppDestination.previewEditorRoute())
        assertEquals("preview/history/42", AppDestination.previewHistoryRoute(42L))
    }
}
