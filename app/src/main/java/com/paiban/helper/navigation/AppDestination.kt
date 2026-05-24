package com.paiban.helper.navigation

enum class AppDestination(
    val route: String,
    val label: String,
) {
    Editor(route = "editor", label = "编辑"),
    History(route = "history", label = "历史"),
    Templates(route = "templates", label = "模板"),
    AiAssistant(route = "ai", label = "AI 辅助"),
    Settings(route = "settings", label = "设置"),
    PreviewEditor(route = "preview/editor", label = "预览"),
    PreviewHistory(route = "preview/history/{historyId}", label = "历史预览");

    companion object {
        /** 底部导航栏显示的三个标签 */
        fun topLevelDestinations(): List<AppDestination> = listOf(Editor, History, Settings)

        fun previewEditorRoute(): String = PreviewEditor.route

        fun previewHistoryRoute(historyId: Long): String = "preview/history/$historyId"
    }
}
