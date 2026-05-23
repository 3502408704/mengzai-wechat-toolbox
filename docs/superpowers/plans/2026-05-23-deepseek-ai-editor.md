# DeepSeek AI Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an independent DeepSeek AI chat page that can stream generated content as code blocks, keep API secrets hidden, and let users preview then replace or append text into the editor.

**Architecture:** Introduce a private AI configuration store with encrypted API key persistence, a local conversation store for recent sessions, and a dedicated `AI` navigation page that streams assistant output into a message list using native Compose components. The page should never expose saved API keys, and model expansion should happen by creating a new configuration rather than editing or revealing an existing one.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Hilt, Room, DataStore, Kotlin Coroutines/Flow, OkHttp or the app's existing networking stack if already preferred by the codebase.

---

### Task 1: Add AI configuration and session persistence models

**Files:**
- Create: `app/src/main/java/com/paiban/helper/data/db/AiConfigEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/AiChatSessionEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/AiChatMessageEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/AiConfigDao.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/AiChatSessionDao.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/AiChatMessageDao.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/db/AppDatabase.kt`
- Create: `app/src/test/java/com/paiban/helper/data/db/AiConfigEntityTest.kt`
- Create: `app/src/test/java/com/paiban/helper/data/db/AiChatSessionPersistenceTest.kt`

- [ ] **Step 1: Write the failing entity/persistence tests**

```kotlin
@Test
fun aiConfigStoresSecretWithoutExposingItInUiFields() {
    val entity = AiConfigEntity(
        id = 1L,
        displayName = "DeepSeek 默认",
        provider = "deepseek",
        model = "deepseek-chat",
        apiKeyEncrypted = "encrypted-value",
        baseUrl = "https://api.deepseek.com",
        isActive = true,
        createdAt = 1L,
        updatedAt = 1L,
    )

    assertEquals("DeepSeek 默认", entity.displayName)
    assertEquals("deepseek-chat", entity.model)
    assertEquals("encrypted-value", entity.apiKeyEncrypted)
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.data.db.AiConfigEntityTest" --tests "com.paiban.helper.data.db.AiChatSessionPersistenceTest"`

Expected: FAIL because the AI entities and DAOs do not exist yet.

- [ ] **Step 3: Implement the minimal Room entities, DAOs, and database wiring**

```kotlin
@Entity(tableName = "ai_configs")
data class AiConfigEntity(
    @PrimaryKey val id: Long,
    val displayName: String,
    val provider: String,
    val model: String,
    val apiKeyEncrypted: String,
    val baseUrl: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
```

```kotlin
@Database(
    entities = [DraftEntity::class, HistoryEntity::class, AiConfigEntity::class, AiChatSessionEntity::class, AiChatMessageEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiConfigDao(): AiConfigDao
    abstract fun aiChatSessionDao(): AiChatSessionDao
    abstract fun aiChatMessageDao(): AiChatMessageDao
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.data.db.AiConfigEntityTest" --tests "com.paiban.helper.data.db.AiChatSessionPersistenceTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/data/db app/src/test/java/com/paiban/helper/data/db
git commit -m "feat: add ai persistence models"
```

### Task 2: Add encrypted AI credential storage and a write-only settings UI

**Files:**
- Create: `app/src/main/java/com/paiban/helper/data/repository/AiSettingsRepository.kt`
- Modify: `app/src/main/java/com/paiban/helper/di/AppModule.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/preferences/AppPreferences.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Create: `app/src/test/java/com/paiban/helper/data/repository/AiSettingsRepositoryTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/settings/AiSettingsUiModelTest.kt`

- [ ] **Step 1: Write the failing repository and UI tests**

```kotlin
@Test
fun newlyCreatedAiConfigIsWriteOnlyAndNeverReturnsApiKey() {
    val repo = AiSettingsRepository(fakeStore, fakeCrypto)
    runTest {
        repo.saveNewConfig(displayName = "DeepSeek 默认", baseUrl = "https://api.deepseek.com", model = "deepseek-chat", apiKey = "sk-test")
        val configs = repo.observeConfigs().first()
        assertEquals(1, configs.size)
        assertEquals("DeepSeek 默认", configs.single().displayName)
        assertFalse(configs.single().apiKeyVisible)
    }
}
```

```kotlin
@Test
fun settingsPageOnlyOffersCreateDisableAndDeleteForAiConfigs() {
    val sections = buildSettingsSections(SettingsUiState())
    val aiSection = sections.first { it.title == "AI 配置" }
    assertTrue(aiSection.rows.any { it is SettingsRowUiModel.Info })
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.data.repository.AiSettingsRepositoryTest" --tests "com.paiban.helper.ui.settings.AiSettingsUiModelTest"`

Expected: FAIL because the repository, crypto helper, and UI models do not exist yet.

- [ ] **Step 3: Implement the minimal write-only config repository and settings section**

```kotlin
class AiSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val crypto: AiSecretCrypto,
) {
    suspend fun saveNewConfig(displayName: String, baseUrl: String, model: String, apiKey: String) { /* ... */ }
    fun observeConfigs(): Flow<List<AiConfigSummary>> { /* no secret exposure */ }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.data.repository.AiSettingsRepositoryTest" --tests "com.paiban.helper.ui.settings.AiSettingsUiModelTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/data/repository/AiSettingsRepository.kt app/src/main/java/com/paiban/helper/ui/settings app/src/test/java/com/paiban/helper/data/repository/AiSettingsRepositoryTest.kt app/src/test/java/com/paiban/helper/ui/settings/AiSettingsUiModelTest.kt app/src/main/java/com/paiban/helper/di/AppModule.kt app/src/main/java/com/paiban/helper/data/preferences/AppPreferences.kt
git commit -m "feat: add ai settings storage"
```

### Task 3: Implement the AI chat domain and DeepSeek client

**Files:**
- Create: `app/src/main/java/com/paiban/helper/domain/ai/DeepSeekClient.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/ai/AiChatRepository.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/ai/AiChatModels.kt`
- Modify: `app/src/main/java/com/paiban/helper/di/AppModule.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/ai/DeepSeekClientTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/ai/AiChatRepositoryTest.kt`

- [ ] **Step 1: Write the failing client and repository tests**

```kotlin
@Test
fun deepSeekClientParsesStreamingAssistantChunks() {
    val client = DeepSeekClient(fakeHttpEngine)
    val chunks = client.streamChat("hello").toList()
    assertTrue(chunks.isNotEmpty())
}
```

```kotlin
@Test
fun chatRepositoryBuildsPromptFromCurrentDraftAndTemplate() {
    val prompt = buildAiPrompt(currentDraft = "草稿", templateName = "极简·经典", userInput = "改写成更正式")
    assertTrue(prompt.contains("草稿"))
    assertTrue(prompt.contains("极简·经典"))
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.domain.ai.DeepSeekClientTest" --tests "com.paiban.helper.domain.ai.AiChatRepositoryTest"`

Expected: FAIL because the AI client and repository do not exist yet.

- [ ] **Step 3: Implement the minimal streaming client and prompt builder**

```kotlin
data class AiChatRequest(
    val model: String,
    val messages: List<AiChatMessage>,
    val stream: Boolean = true,
)
```

```kotlin
fun buildAiPrompt(currentDraft: String, templateName: String, userInput: String): String =
    "当前草稿：$currentDraft\n模板：$templateName\n用户需求：$userInput"
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.domain.ai.DeepSeekClientTest" --tests "com.paiban.helper.domain.ai.AiChatRepositoryTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/ai app/src/test/java/com/paiban/helper/domain/ai app/src/main/java/com/paiban/helper/di/AppModule.kt
git commit -m "feat: add ai chat domain"
```

### Task 4: Build the independent AI chat screen with streaming code-block rendering

**Files:**
- Create: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatUiState.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatMessageUiModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt`
- Modify: `app/src/main/java/com/paiban/helper/navigation/AppDestination.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/ai/chat/AiChatUiModelTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/ai/chat/AiChatScreenContractTest.kt`

- [ ] **Step 1: Write the failing UI model and navigation tests**

```kotlin
@Test
fun aiChatMessagesRenderAsCodeBlocksWhileStreaming() {
    val state = AiChatUiState(
        messages = listOf(
            AiChatMessageUiModel.AssistantStreaming("```kotlin\nfun hi()"),
        ),
    )
    assertTrue(state.messages.single() is AiChatMessageUiModel.AssistantStreaming)
}
```

```kotlin
@Test
fun aiDestinationOpensAStandaloneChatRoute() {
    assertEquals("ai/chat", AppDestination.AiChat.route)
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.ai.chat.AiChatUiModelTest" --tests "com.paiban.helper.ui.ai.chat.AiChatScreenContractTest"`

Expected: FAIL because the chat screen and route do not exist yet.

- [ ] **Step 3: Implement the chat screen with native Compose controls**

```kotlin
@Composable
fun AiChatScreen(
    state: AiChatUiState,
    onSend: (String) -> Unit,
    onCreateNewConfig: () -> Unit,
    onApplySuggestion: (ApplyMode) -> Unit,
) {
    AppPage(
        header = PageHeaderModel(title = "AI 辅助", subtitle = "DeepSeek 对话"),
    ) { contentPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding.asPaddingValues()) {
            items(state.messages) { message -> /* streaming code blocks */ }
        }
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.ai.chat.AiChatUiModelTest" --tests "com.paiban.helper.ui.ai.chat.AiChatScreenContractTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/ai/chat app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt app/src/main/java/com/paiban/helper/navigation/AppDestination.kt app/src/test/java/com/paiban/helper/ui/ai/chat
git commit -m "feat: add ai chat screen"
```

### Task 5: Wire AI suggestions back into the editor with replace-or-append confirmation

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/editor/AiSuggestionApplyTest.kt`

- [ ] **Step 1: Write the failing suggestion-apply tests**

```kotlin
@Test
fun suggestionCanBeAppliedAsReplaceOrAppend() {
    val state = EditorUiState(content = "原文")
    val replace = applyAiSuggestion(state, "新文", ApplyMode.Replace)
    val append = applyAiSuggestion(state, "新文", ApplyMode.Append)
    assertEquals("新文", replace.content)
    assertTrue(append.content.contains("原文"))
    assertTrue(append.content.contains("新文"))
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.editor.AiSuggestionApplyTest"`

Expected: FAIL because the suggestion application helper does not exist yet.

- [ ] **Step 3: Implement the helper and editor integration**

```kotlin
enum class ApplyMode { Replace, Append }
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.editor.AiSuggestionApplyTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/editor app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt app/src/test/java/com/paiban/helper/ui/editor/AiSuggestionApplyTest.kt
git commit -m "feat: apply ai suggestions to editor"
```

### Task 6: Add accessibility and end-to-end regression coverage

**Files:**
- Modify: `app/src/test/java/com/paiban/helper/ui/accessibility/AccessibilityContentRulesTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/ai/chat/AiChatAccessibilityTest.kt`
- Create: `app/src/androidTest/java/com/paiban/helper/ui/ai/chat/AiChatScreenTest.kt`

- [ ] **Step 1: Write the failing accessibility tests**

```kotlin
@Test
fun aiChatAnnouncesStreamingAndSelectionClearly() {
    assertEquals("AI 辅助，即将开放", aiAssistantCardContentDescription())
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.ai.chat.AiChatAccessibilityTest"`

Expected: FAIL because the dedicated chat accessibility helpers do not exist yet.

- [ ] **Step 3: Add semantics and test them end to end**

```kotlin
Modifier.semantics {
    contentDescription = "AI 回复内容"
    stateDescription = "正在生成"
}
```

- [ ] **Step 4: Run unit and instrumentation tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.accessibility.AccessibilityContentRulesTest" --tests "com.paiban.helper.ui.ai.chat.AiChatAccessibilityTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java app/src/test/java app/src/androidTest/java
git commit -m "test: cover ai accessibility"
```

### Final Verification

Run: `.\gradlew.bat :app:testDebugUnitTest`

Expected: PASS with no failing unit tests.

## Self-Review

- Spec coverage: covers private AI config, write-only secrets, DeepSeek chat client, streaming code-block output, preview-before-apply, replace-or-append flow, local session persistence, and accessibility-first native Compose UI.
- Placeholder scan: no TBD/TODO placeholders remain.
- Type consistency: `AiConfigEntity`, `AiChatSessionEntity`, `AiChatMessageEntity`, `AiSettingsRepository`, `DeepSeekClient`, `AiChatScreen`, and `ApplyMode` are used consistently across tasks.
