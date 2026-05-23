package com.paiban.helper.ui.workbench

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkbenchContractTest {
    @Test
    fun workbenchModesExposeStableLabelsInOrder() {
        assertEquals(
            listOf("创作", "管理", "设置"),
            WorkbenchMode.entries.map(WorkbenchMode::label),
        )
    }

    @Test
    fun createModePrimaryActionRemainsPreview() {
        assertEquals("预览成品", workbenchPrimaryActionLabel(WorkbenchMode.Create))
    }

    @Test
    fun manageAndSettingsModesReturnToCreate() {
        assertEquals("继续创作", workbenchPrimaryActionLabel(WorkbenchMode.Manage))
        assertEquals("返回创作", workbenchPrimaryActionLabel(WorkbenchMode.Settings))
    }

    @Test
    fun aiTeaserAccessibilityCopyStaysExplicitlyUnavailable() {
        assertEquals("AI 辅助编辑，即将开放", aiTeaserTitle())
        assertEquals("即将开放，不可用", aiTeaserStateDescription())
        assertFalse(aiTeaserEnabled())
    }

    @Test
    fun historyTapStillOwnsPreviewWhileManageCtaReturnsToCreate() {
        assertTrue(workbenchManageModeUsesDirectHistoryPreview())
        assertFalse(workbenchManageModeUsesPersistentHistorySelection())
    }
}
