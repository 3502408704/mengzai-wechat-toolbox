package com.paiban.helper.ui.workbench

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkbenchRouteModelTest {
    @Test
    fun manageAndSettingsPrimaryActionsReturnToCreate() {
        assertEquals(WorkbenchMode.Create, workbenchPrimaryActionTarget(WorkbenchMode.Manage))
        assertEquals(WorkbenchMode.Create, workbenchPrimaryActionTarget(WorkbenchMode.Settings))
    }

    @Test
    fun createModeKeepsPreviewAsPrimaryActionTarget() {
        assertEquals(null, workbenchPrimaryActionTarget(WorkbenchMode.Create))
    }

    @Test
    fun manageModeDescriptionExplainsDirectPreviewFlow() {
        assertTrue(WorkbenchMode.Manage.subtitle().contains("预览"))
    }
}
