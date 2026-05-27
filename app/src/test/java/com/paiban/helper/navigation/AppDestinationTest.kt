package com.paiban.helper.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun editorReturnsAsPrimaryRoute() {
        assertEquals(
            "editor",
            AppDestination.Editor.route,
        )
    }

    @Test
    fun legacyTopLevelRoutesAreRestored() {
        assertEquals(
            listOf(
                "editor",
                "history",
                "templates",
                "ai",
                "settings",
                "preview/editor",
                "preview/history/{historyId}",
                "help",
            ),
            AppDestination.entries.map { it.route },
        )
    }
}
