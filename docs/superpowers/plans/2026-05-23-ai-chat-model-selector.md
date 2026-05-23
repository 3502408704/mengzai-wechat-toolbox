# AI Chat Model Selector Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users choose an AI model directly on the chat page, while shipping one built-in DeepSeek configuration that is always available and never reveals its API key.

**Architecture:** Keep configuration data in the existing AI settings layer, but add a built-in read-only config record that the chat screen can always select. The chat screen owns active-model selection and passes the chosen config into the DeepSeek chat flow; settings continues to manage only user-created configs. Secrets remain write-only, accessibility stays native Compose, and the built-in config cannot be deleted or edited in a way that exposes its secret.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Hilt, Room, Kotlin Coroutines/Flow, existing DeepSeek client and AI domain models.

---

### Task 1: Extend AI configuration storage with a built-in default config

**Files:**
- Create: `app/src/main/java/com/paiban/helper/data/db/AiConfigEntity.kt`
- Create: `app/src/main/java/com/paiban/helper/data/db/AiConfigDao.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/db/AppDatabase.kt`
- Create: `app/src/main/java/com/paiban/helper/data/repository/AiSettingsRepository.kt`
- Create: `app/src/test/java/com/paiban/helper/data/repository/AiSettingsRepositoryTest.kt`

- [ ] **Step 1: Write the failing repository test for the built-in config**

```kotlin
@Test
fun builtInDeepSeekConfigIsAlwaysPresentAndReadOnly() = runTest {
    val repo = AiSettingsRepository(fakeDao, fakeCrypto)

    val configs = repo.observeConfigs().first()

    assertTrue(configs.any { it.id == AiSettingsRepository.BUILT_IN_CONFIG_ID })
    assertTrue(configs.single { it.id == AiSettingsRepository.BUILT_IN_CONFIG_ID }.isBuiltIn)
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.data.repository.AiSettingsRepositoryTest"`

Expected: FAIL because the built-in config and repository wiring do not exist yet.

- [ ] **Step 3: Implement the Room entity, DAO, and repository seed logic**

```kotlin
@Entity(tableName = "ai_configs")
data class AiConfigEntity(
    @PrimaryKey val id: Long,
    val displayName: String,
    val provider: String,
    val model: String,
    val baseUrl: String,
    val apiKeyEncrypted: String,
    val isBuiltIn: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
```

```kotlin
class AiSettingsRepository(
    private val dao: AiConfigDao,
    private val crypto: AiSecretCrypto,
) {
    companion object {
        const val BUILT_IN_CONFIG_ID = 1L
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.data.repository.AiSettingsRepositoryTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/data/db app/src/main/java/com/paiban/helper/data/repository app/src/test/java/com/paiban/helper/data/repository
git commit -m "feat: seed built-in ai config"
```

### Task 2: Surface model selection on the chat page

**Files:**
- Create: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatScreen.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatViewModel.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatUiState.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/ai/chat/AiChatUiStateTest.kt`

- [ ] **Step 1: Write the failing UI-state test for model selection**

```kotlin
@Test
fun chatScreenExposesSelectedModelAndAvailableConfigs() {
    val state = AiChatUiState(
        configs = listOf(
            AiConfigSummary(id = 1L, displayName = "DeepSeek 默认", model = "deepseek-chat", isBuiltIn = true),
            AiConfigSummary(id = 2L, displayName = "DeepSeek 代码", model = "deepseek-coder", isBuiltIn = false),
        ),
        selectedConfigId = 1L,
    )

    assertEquals(1L, state.selectedConfigId)
    assertEquals("deepseek-chat", state.selectedConfig()?.model)
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.ai.chat.AiChatUiStateTest"`

Expected: FAIL because the new chat state and selector helpers do not exist yet.

- [ ] **Step 3: Implement the chat state and native Compose selector**

```kotlin
@Composable
fun AiChatScreen(
    state: AiChatUiState,
    onSelectConfig: (Long) -> Unit,
    onSend: (String) -> Unit,
) {
    // Use Material3 controls only: exposed dropdown menu, text fields, buttons, and cards.
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.ai.chat.AiChatUiStateTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/ai app/src/test/java/com/paiban/helper/ui/ai
git commit -m "feat: add ai chat model selector"
```

### Task 3: Route the selected config into DeepSeek streaming

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/ai/AiChatRepository.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/ai/DeepSeekClient.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/ai/AiChatModels.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/ai/AiChatRepositoryTest.kt`

- [ ] **Step 1: Write the failing prompt/client test for config-aware streaming**

```kotlin
@Test
fun streamUsesSelectedModelAndBaseUrl() = runTest {
    val config = DeepSeekConfig(apiKeyValue = "sk-test", baseUrl = "https://api.deepseek.com", model = "deepseek-chat")
    val request = buildDeepSeekRequest(config, emptyList())

    assertEquals("deepseek-chat", request.model)
    assertEquals("https://api.deepseek.com", request.baseUrl)
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.domain.ai.AiChatRepositoryTest"`

Expected: FAIL because the request builder and selected-config flow are not wired yet.

- [ ] **Step 3: Implement the config-aware request builder and pass the selected config through the repository**

```kotlin
fun buildDeepSeekRequest(config: DeepSeekConfig, messages: List<DeepSeekMessage>): DeepSeekRequest =
    DeepSeekRequest(
        model = config.model,
        baseUrl = config.baseUrl,
        apiKey = config.apiKeyHeaderValue(),
        messages = messages,
    )
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.domain.ai.AiChatRepositoryTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/ai app/src/test/java/com/paiban/helper/domain/ai
git commit -m "feat: wire ai config into chat streaming"
```

### Task 4: Keep settings focused on user-created configs only

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsViewModel.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/settings/AiConfigSettingsUiTest.kt`

- [ ] **Step 1: Write the failing UI test for built-in config restrictions**

```kotlin
@Test
fun builtInConfigDoesNotShowDeleteOrRevealActions() {
    val rows = buildAiConfigRows(
        listOf(
            AiConfigSummary(id = 1L, displayName = "DeepSeek 默认", model = "deepseek-chat", isBuiltIn = true),
        )
    )

    assertTrue(rows.single().isReadOnly)
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.settings.AiConfigSettingsUiTest"`

Expected: FAIL because the settings model does not yet distinguish built-in configs.

- [ ] **Step 3: Update the settings UI model to hide edit/delete/reveal affordances for the built-in item**

```kotlin
data class AiConfigRowUiModel(
    val id: Long,
    val title: String,
    val subtitle: String,
    val isReadOnly: Boolean,
)
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.settings.AiConfigSettingsUiTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/settings app/src/test/java/com/paiban/helper/ui/settings
git commit -m "feat: lock down built-in ai config"
```

### Task 5: Verify accessibility and end-to-end behavior

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/ai/chat/AiChatScreen.kt`
- Create: `app/src/androidTest/java/com/paiban/helper/ui/ai/chat/AiChatScreenTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/ai/chat/AiChatAccessibilityTest.kt`

- [ ] **Step 1: Write the accessibility test for the selector and streaming state**

```kotlin
@Test
fun chatModelSelectorAnnouncesCurrentSelection() {
    assertEquals("当前模型", aiChatModelSelectorLabel())
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.ai.chat.AiChatAccessibilityTest"`

Expected: FAIL because the accessibility labels are not in place yet.

- [ ] **Step 3: Add semantics and confirm the screen remains native Compose only**

```kotlin
Modifier.semantics {
    contentDescription = "当前模型"
    stateDescription = "已选择 DeepSeek 默认"
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.paiban.helper.ui.ai.chat.AiChatAccessibilityTest" --tests "com.paiban.helper.ui.settings.AiConfigSettingsUiTest" --tests "com.paiban.helper.domain.ai.AiChatRepositoryTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/ai/chat app/src/androidTest/java/com/paiban/helper/ui/ai/chat app/src/test/java/com/paiban/helper/ui/ai/chat
git commit -m "test: cover ai model selection accessibility"
```

### Final Verification

Run: `.\gradlew.bat :app:testDebugUnitTest`

Expected: PASS with the new built-in config, chat-page model selector, and read-only default configuration behavior.

## Self-Review

- Spec coverage: covers the chat-page model selector, the always-present built-in DeepSeek config, write-only secrets, settings restrictions for built-in configs, config-aware streaming, and accessibility.
- Placeholder scan: no TBD/TODO placeholders remain.
- Type consistency: `AiConfigEntity`, `AiConfigDao`, `AiSettingsRepository`, `AiChatUiState`, `AiConfigSummary`, and `DeepSeekConfig` are used consistently across tasks.
