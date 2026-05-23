package com.paiban.helper.ui.workbench

enum class WorkbenchMode {
    Create,
    Manage,
    Settings,
}

fun WorkbenchMode.label(): String = labelValue

val WorkbenchMode.labelValue: String
    get() = when (this) {
        WorkbenchMode.Create -> "创作"
        WorkbenchMode.Manage -> "管理"
        WorkbenchMode.Settings -> "设置"
    }

fun WorkbenchMode.subtitle(): String {
    return when (this) {
        WorkbenchMode.Create -> "输入 HTML / Markdown，并即时准备预览成品。"
        WorkbenchMode.Manage -> "回看历史快照，快速预览或恢复内容。"
        WorkbenchMode.Settings -> "调整外观与偏好，保持工作台节奏一致。"
    }
}

data class WorkbenchChromeModel(
    val mode: WorkbenchMode,
    val title: String,
    val subtitle: String,
    val primaryActionLabel: String,
    val primaryActionEnabled: Boolean = true,
)

fun workbenchPrimaryActionLabel(mode: WorkbenchMode): String {
    return when (mode) {
        WorkbenchMode.Create -> "预览成品"
        WorkbenchMode.Manage -> "继续创作"
        WorkbenchMode.Settings -> "返回创作"
    }
}

fun workbenchPrimaryActionTarget(mode: WorkbenchMode): WorkbenchMode? {
    return when (mode) {
        WorkbenchMode.Create -> null
        WorkbenchMode.Manage -> WorkbenchMode.Create
        WorkbenchMode.Settings -> WorkbenchMode.Create
    }
}

fun workbenchChromeModel(mode: WorkbenchMode): WorkbenchChromeModel {
    return WorkbenchChromeModel(
        mode = mode,
        title = "排版助手",
        subtitle = mode.subtitle(),
        primaryActionLabel = workbenchPrimaryActionLabel(mode),
    )
}

fun aiTeaserTitle(): String = "AI 辅助编辑，即将开放"

fun aiTeaserStateDescription(): String = "即将开放，不可用"

fun aiTeaserEnabled(): Boolean = false

fun workbenchManageModeUsesDirectHistoryPreview(): Boolean = true

fun workbenchManageModeUsesPersistentHistorySelection(): Boolean = false
