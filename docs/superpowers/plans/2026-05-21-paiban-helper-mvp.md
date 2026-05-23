# 排版助手 Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 使用 Kotlin + Jetpack Compose 全量重构现有原型，交付“排版助手” Phase 1 APK，支持 HTML/CSS/Markdown 编辑、安全预览、复制富文本、历史记录、剪贴板导入、文件导入导出、主题与无障碍，并包含 `AI 辅助` 底部导航灰态占位。

**Architecture:** 使用 Kotlin + Jetpack Compose + 单 Activity 多页面结构，Room 保存草稿与历史，DataStore 保存设置，WebView 负责受控预览渲染。领域层拆分为内容识别、Markdown 转换、HTML 清洗、预览文档构建、剪贴板识别与导入导出服务，以便优先测试核心行为。底部导航包含编辑、预览、历史、AI 辅助、设置，其中 AI 页仅实现实验室占位。

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose Material 3, Navigation Compose, Room, DataStore, Hilt, Coroutines, WebView, JUnit, AndroidX Test

---

## 预估文件结构

### 项目级

- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`

### 应用入口与依赖注入

- Create: `app/src/main/java/com/paiban/helper/PaibanApplication.kt`
- Create: `app/src/main/java/com/paiban/helper/MainActivity.kt`
- Create: `app/src/main/java/com/paiban/helper/di/AppModule.kt`
- Create: `app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt`
- Create: `app/src/main/java/com/paiban/helper/navigation/AppDestination.kt`

### 数据层

- Create: `app/src/main/java/com/paiban/helper/data/db/AppDatabase.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/DraftEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/HistoryEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/DraftDao.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/HistoryDao.kt`
- Create: `app/src/main/java/com/paiban/helper/data/preferences/AppPreferences.kt`
- Create: `app/src/main/java/com/paiban/helper/data/preferences/ThemeMode.kt`
- Create: `app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt`
- Create: `app/src/main/java/com/paiban/helper/data/repository/SettingsRepository.kt`

### 领域层

- Create: `app/src/main/java/com/paiban/helper/domain/model/ContentType.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/model/PreviewPayload.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/analysis/ContentClassifier.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/render/MarkdownConverter.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/clipboard/ClipboardInspector.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/files/ImportExportManager.kt`

### UI 层

- Create: `app/src/main/java/com/paiban/helper/ui/theme/Color.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/theme/Type.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/common/AccessibleSnackbarHost.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/editor/EditorViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/history/HistoryViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/settings/SettingsViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/onboarding/OnboardingScreen.kt`

### 测试

- Create: `app/src/test/java/com/paiban/helper/domain/analysis/ContentClassifierTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/render/MarkdownConverterTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/clipboard/ClipboardInspectorTest.kt`
- Create: `app/src/test/java/com/paiban/helper/data/repository/HistoryRetentionTest.kt`

## Task 1: 初始化 Kotlin + Compose 工程骨架

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 创建项目级 Gradle 配置**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PaibanHelper"
include(":app")
```

```kotlin
// build.gradle.kts
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
}
```

- [ ] **Step 2: 创建应用模块构建文件**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}
```

- [ ] **Step 3: 添加 AndroidManifest 与应用入口声明**

```xml
<manifest package="com.paiban.helper">
    <uses-permission android:name="android.permission.INTERNET" />
    <application android:name=".PaibanApplication" />
</manifest>
```

- [ ] **Step 4: 验证 Gradle 配置文件存在且结构完整**

Run: `Get-ChildItem -Recurse app,settings.gradle.kts,build.gradle.kts,gradle.properties`
Expected: 列出项目级与 app 模块文件

## Task 2: 先写失败的核心领域测试

**Files:**
- Create: `app/src/test/java/com/paiban/helper/domain/analysis/ContentClassifierTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/render/MarkdownConverterTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/clipboard/ClipboardInspectorTest.kt`
- Create: `app/src/test/java/com/paiban/helper/data/repository/HistoryRetentionTest.kt`

- [ ] **Step 1: 为内容识别写失败测试**

```kotlin
class ContentClassifierTest {
    @Test
    fun classify_markdown_input_as_markdown() {
        val classifier = ContentClassifier()

        val result = classifier.classify("# 标题\n\n- 列表项")

        assertEquals(ContentType.Markdown, result)
    }
}
```

- [ ] **Step 2: 为 Markdown 转换写失败测试**

```kotlin
class MarkdownConverterTest {
    @Test
    fun convert_heading_and_bold_markdown_to_html() {
        val converter = MarkdownConverter()

        val html = converter.convert("# 标题\n\n**加粗**")

        assertTrue(html.contains("<h1>标题</h1>"))
        assertTrue(html.contains("<strong>加粗</strong>"))
    }
}
```

- [ ] **Step 3: 为 HTML 清洗写失败测试**

```kotlin
class HtmlSanitizerTest {
    @Test
    fun remove_script_tag_and_inline_event() {
        val sanitizer = HtmlSanitizer()

        val sanitized = sanitizer.sanitize("<p onclick='x()'>hi</p><script>alert(1)</script>")

        assertFalse(sanitized.contains("script"))
        assertFalse(sanitized.contains("onclick"))
        assertTrue(sanitized.contains("<p>hi</p>"))
    }
}
```

- [ ] **Step 4: 为剪贴板识别写失败测试**

```kotlin
class ClipboardInspectorTest {
    @Test
    fun detect_markup_like_content() {
        val inspector = ClipboardInspector()

        val result = inspector.shouldSuggestImport("<h1>标题</h1><p>正文</p>")

        assertTrue(result)
    }
}
```

- [ ] **Step 5: 为历史淘汰策略写失败测试**

```kotlin
class HistoryRetentionTest {
    @Test
    fun keep_favorite_items_when_trimming_history() {
        val items = listOf(
            HistoryEntity(id = 1, title = "A", rawContent = "a", lastRenderedHtml = "", contentType = "Markdown", isFavorite = false, createdAt = 1, updatedAt = 1),
            HistoryEntity(id = 2, title = "B", rawContent = "b", lastRenderedHtml = "", contentType = "Markdown", isFavorite = true, createdAt = 2, updatedAt = 2)
        )

        val trimmed = EditorRepository.trimHistory(items, limit = 1)

        assertEquals(listOf(2L), trimmed.map { it.id })
    }
}
```

- [ ] **Step 6: 运行测试，确认失败**

Run: `.\gradlew testDebugUnitTest`
Expected: 编译失败或测试失败，错误指出相关类未定义或行为未实现

## Task 3: 实现内容识别、Markdown 转换、HTML 清洗与预览构建

**Files:**
- Create: `app/src/main/java/com/paiban/helper/domain/model/ContentType.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/model/PreviewPayload.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/analysis/ContentClassifier.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/render/MarkdownConverter.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/clipboard/ClipboardInspector.kt`

- [ ] **Step 1: 实现内容类型枚举与识别器**

```kotlin
enum class ContentType {
    Html,
    Markdown,
    Mixed,
    PlainText
}
```

```kotlin
class ContentClassifier {
    fun classify(input: String): ContentType
}
```

- [ ] **Step 2: 实现最小 Markdown 转换器**

```kotlin
class MarkdownConverter {
    fun convert(markdown: String): String
}
```

- [ ] **Step 3: 实现 HTML 清洗器、预览载荷与剪贴板识别器**

```kotlin
data class PreviewPayload(
    val htmlDocument: String,
    val plainText: String,
    val contentType: ContentType
)
```

```kotlin
class HtmlSanitizer {
    fun sanitize(rawHtml: String): String
}
```

```kotlin
class ClipboardInspector {
    fun shouldSuggestImport(text: String): Boolean
}
```

- [ ] **Step 4: 实现预览文档构建器**

```kotlin
class PreviewDocumentBuilder(
    private val classifier: ContentClassifier,
    private val markdownConverter: MarkdownConverter,
    private val sanitizer: HtmlSanitizer
) {
    fun build(rawInput: String): PreviewPayload
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `.\gradlew testDebugUnitTest --tests "*ContentClassifierTest" --tests "*MarkdownConverterTest" --tests "*HtmlSanitizerTest" --tests "*ClipboardInspectorTest"`
Expected: 目标测试通过

## Task 4: 实现 Room、DataStore 与仓储层

**Files:**
- Create: `app/src/main/java/com/paiban/helper/data/db/AppDatabase.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/DraftEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/HistoryEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/DraftDao.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/HistoryDao.kt`
- Create: `app/src/main/java/com/paiban/helper/data/preferences/AppPreferences.kt`
- Create: `app/src/main/java/com/paiban/helper/data/preferences/ThemeMode.kt`
- Create: `app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt`
- Create: `app/src/main/java/com/paiban/helper/data/repository/SettingsRepository.kt`

- [ ] **Step 1: 建立草稿与历史实体、DAO**

```kotlin
@Entity(tableName = "drafts")
data class DraftEntity(...)

@Entity(tableName = "history")
data class HistoryEntity(...)
```

- [ ] **Step 2: 为历史裁剪逻辑实现最小代码**

```kotlin
companion object {
    fun trimHistory(items: List<HistoryEntity>, limit: Int): List<HistoryEntity>
}
```

- [ ] **Step 3: 建立 DataStore 偏好保存**

```kotlin
data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val editorFontScale: Float = 1f,
    val showLineNumbers: Boolean = true,
    val developerMode: Boolean = false,
    val onboardingCompleted: Boolean = false
)
```

- [ ] **Step 4: 运行历史策略测试**

Run: `.\gradlew testDebugUnitTest --tests "*HistoryRetentionTest"`
Expected: 测试通过

## Task 5: 创建应用入口、依赖注入与底部导航结构

**Files:**
- Create: `app/src/main/java/com/paiban/helper/PaibanApplication.kt`
- Create: `app/src/main/java/com/paiban/helper/MainActivity.kt`
- Create: `app/src/main/java/com/paiban/helper/di/AppModule.kt`
- Create: `app/src/main/java/com/paiban/helper/navigation/AppDestination.kt`
- Create: `app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt`

- [ ] **Step 1: 创建 Hilt Application 与模块**

```kotlin
@HiltAndroidApp
class PaibanApplication : Application()
```

- [ ] **Step 2: 建立 MainActivity 与 Compose 容器**

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

- [ ] **Step 3: 创建底部导航目的地**

```kotlin
sealed class AppDestination(
    val route: String
) {
    data object Editor : AppDestination("editor")
    data object Preview : AppDestination("preview")
    data object History : AppDestination("history")
    data object AiAssistant : AppDestination("ai_assistant")
    data object Settings : AppDestination("settings")
}
```

- [ ] **Step 4: 创建导航图与底部导航壳层**

```kotlin
@Composable
fun AppNavGraph(...)
```

- [ ] **Step 5: 构建并验证主 Activity 可编译**

Run: `.\gradlew :app:assembleDebug`
Expected: Debug 构建通过或仅剩后续未实现类错误

## Task 6: 实现编辑页与编辑器交互

**Files:**
- Create: `app/src/main/java/com/paiban/helper/ui/editor/EditorViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt`

- [ ] **Step 1: 为 EditorViewModel 编写状态管理**

```kotlin
data class EditorUiState(...)
```

- [ ] **Step 2: 实现等宽编辑器、行号开关、字号调节与 Markdown 快捷栏**

```kotlin
@Composable
fun EditorScreen(...)
```

- [ ] **Step 3: 接入撤销、重做、自动保存与剪贴板导入提示**

```kotlin
fun onUndo()
fun onRedo()
fun importClipboardContent()
```

- [ ] **Step 4: 接入“前往预览”动作并验证状态保留**

Run: `.\gradlew :app:assembleDebug`
Expected: 切换到预览页后返回编辑页，输入内容仍保留

## Task 7: 实现预览页、WebView 渲染与复制分享

**Files:**
- Create: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/files/ImportExportManager.kt`

- [ ] **Step 1: 为预览页建立状态与刷新逻辑**

```kotlin
data class PreviewUiState(...)
```

- [ ] **Step 2: 实现受控 WebView 预览页**

```kotlin
@Composable
fun PreviewScreen(...)
```

- [ ] **Step 3: 实现复制 HTML+纯文本、导出 html/txt、系统分享**

```kotlin
fun copyPreviewToClipboard(...)
fun exportPreview(...)
fun sharePreview(...)
```

- [ ] **Step 4: 构建并人工验证预览链路**

Run: `.\gradlew :app:assembleDebug`
Expected: 构建通过，预览页能显示渲染结果

## Task 8: 实现历史页、AI 占位页与设置页

**Files:**
- Create: `app/src/main/java/com/paiban/helper/ui/history/HistoryViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/settings/SettingsViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/onboarding/OnboardingScreen.kt`

- [ ] **Step 1: 实现历史列表、恢复、删除、重命名、收藏**

```kotlin
@Composable
fun HistoryScreen(...)
```

- [ ] **Step 2: 实现 AI 辅助灰态占位页**

```kotlin
@Composable
fun AiAssistantScreen(...)
```

- [ ] **Step 3: 实现设置页主题、开发者模式与实验室条目**

```kotlin
@Composable
fun SettingsScreen(...)
```

- [ ] **Step 4: 实现首次引导页与跳过逻辑**

```kotlin
@Composable
fun OnboardingScreen(...)
```

- [ ] **Step 5: 构建验证导航完整性**

Run: `.\gradlew :app:assembleDebug`
Expected: 应用可在编辑、预览、历史、AI 占位、设置与首次引导间导航

## Task 9: 加入主题、无障碍与体验细节

**Files:**
- Create: `app/src/main/java/com/paiban/helper/ui/theme/Color.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/*`

- [ ] **Step 1: 实现 Material 3 主题与 Dynamic Color 降级**

```kotlin
@Composable
fun PaibanTheme(...)
```

- [ ] **Step 2: 为全部可交互控件补充 contentDescription 与最小触控区**

```kotlin
Modifier.semantics { contentDescription = "预览按钮" }
```

- [ ] **Step 3: 为 AI 占位页补充“即将推出，不可用”无障碍语义**

```kotlin
Modifier.semantics { stateDescription = "即将推出，不可用" }
```

- [ ] **Step 4: 用 Snackbar 和明确错误文案替换弱提示**

```kotlin
SnackbarHost(...)
```

- [ ] **Step 5: 构建并人工验证深浅色与基本无障碍**

Run: `.\gradlew :app:assembleDebug`
Expected: 构建通过，深浅色切换正常

## Task 10: 最终验证并产出 APK

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/paiban/helper/**/*`

- [ ] **Step 1: 运行完整单元测试**

Run: `.\gradlew testDebugUnitTest`
Expected: 所有单元测试通过

- [ ] **Step 2: 构建 Debug APK**

Run: `.\gradlew :app:assembleDebug`
Expected: 生成 `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 3: 若有设备则安装 APK 并做手工冒烟**

Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
Expected: 安装成功

- [ ] **Step 4: 汇总交付物**

Run: `Get-ChildItem app\\build\\outputs\\apk\\debug`
Expected: 显示 APK 文件路径

## Self-Review

- 规格覆盖检查：已覆盖 Phase 1 规格中的编辑、预览、复制、剪贴板导入、历史、导入导出、主题、无障碍、首次引导、开发者模式，以及 `AI 辅助` 灰态占位与设置页实验室条目。
- 占位检查：计划中未使用 TBD、TODO 或“后续补上”式模糊措辞。AI 相关能力只保留 Phase 1 必要占位，不混入真实 API 接入步骤。
- 类型一致性：`ContentType`、`PreviewPayload`、`EditorRepository.trimHistory`、`ThemeMode`、`AiAssistantScreen`、`AppDestination` 等关键名词在计划中保持一致。

Plan complete and saved to `docs/superpowers/plans/2026-05-21-paiban-helper-mvp.md`. Two execution options:

1. Subagent-Driven (recommended) - I dispatch a fresh subagent per task, review between tasks, fast iteration
2. Inline Execution - Execute tasks in this session using executing-plans, batch execution with checkpoints
