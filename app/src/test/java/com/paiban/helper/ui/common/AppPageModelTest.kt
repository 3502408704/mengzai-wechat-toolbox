package com.paiban.helper.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.paiban.helper.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPageModelTest {
    @Test
    fun pageHeaderDefaultsToNoContextWhenNotProvided() {
        val model = PageHeaderModel(title = "历史")

        assertEquals("历史", model.title)
        assertEquals(null, model.subtitle)
    }

    @Test
    fun pageHeaderExposesContextWhenProvided() {
        val model = PageHeaderModel(
            title = "预览",
            subtitle = "来自当前草稿",
        )

        assertEquals("来自当前草稿", model.subtitle)
    }

    @Test
    fun pageContentPaddingCombinesScaffoldInsetsWithSharedSpacing() {
        val model = AppPageContentPadding(
            scaffoldPadding = PaddingValues(start = 1.dp, top = 2.dp, end = 3.dp, bottom = 4.dp),
            contentPadding = pageContentPadding(),
        )

        val ltr = model.asPaddingValues()
        val rtl = model.asPaddingValues()
        val contentOnly = model.contentPaddingOnly()

        assertEquals(17.dp, ltr.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(14.dp, ltr.calculateTopPadding())
        assertEquals(19.dp, ltr.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(16.dp, ltr.calculateBottomPadding())

        assertEquals(19.dp, rtl.calculateLeftPadding(LayoutDirection.Rtl))
        assertEquals(17.dp, rtl.calculateRightPadding(LayoutDirection.Rtl))

        assertEquals(16.dp, contentOnly.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(12.dp, contentOnly.calculateTopPadding())
        assertEquals(16.dp, contentOnly.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(12.dp, contentOnly.calculateBottomPadding())
    }

    @Test
    fun pageContentPaddingUsesSharedTightSpacingDefaults() {
        val values = pageContentPadding()

        assertEquals(16.dp, values.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(12.dp, values.calculateTopPadding())
        assertEquals(16.dp, values.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(12.dp, values.calculateBottomPadding())
    }

    @Test
    fun destinationSelectionMatchesNestedRoutesWithoutMatchingSimilarPrefixes() {
        assertTrue(isDestinationSelected(currentRoute = "editor", destination = AppDestination.Editor))
        assertTrue(isDestinationSelected(currentRoute = "editor/details", destination = AppDestination.Editor))
        assertTrue(isDestinationSelected(currentRoute = "editor?mode=create", destination = AppDestination.Editor))

        assertFalse(isDestinationSelected(currentRoute = "editorial", destination = AppDestination.Editor))
        assertFalse(isDestinationSelected(currentRoute = "preview/editor", destination = AppDestination.Editor))
        assertFalse(isDestinationSelected(currentRoute = null, destination = AppDestination.Editor))
    }
}
