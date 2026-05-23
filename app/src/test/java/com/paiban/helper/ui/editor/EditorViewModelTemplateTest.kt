package com.paiban.helper.ui.editor

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.paiban.helper.data.db.DraftDao
import com.paiban.helper.data.db.DraftEntity
import com.paiban.helper.data.db.HistoryDao
import com.paiban.helper.data.db.HistoryEntity
import com.paiban.helper.data.repository.EditorRepository
import com.paiban.helper.data.repository.SettingsRepository
import com.paiban.helper.domain.clipboard.ClipboardInspector
import com.paiban.helper.domain.template.ArticleTemplateRepository
import com.paiban.helper.test.MainDispatcherRule
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTemplateTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun selectTemplateStagesPendingChoiceUntilConfirmed() = runTest {
        val draftDao = FakeDraftDao(
            DraftEntity(
                id = 1L,
                title = "未命名草稿",
                rawContent = "",
                lastRenderedHtml = "",
                contentType = "PlainText",
                templateId = "minimalist-0",
                createdAt = 1L,
                updatedAt = 1L,
            )
        )
        val repository = EditorRepository(draftDao, FakeHistoryDao())
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { createTempPreferencesFile() },
        )
        val viewModel = EditorViewModel(
            editorRepository = repository,
            settingsRepository = SettingsRepository(dataStore),
            clipboardInspector = ClipboardInspector(),
            articleTemplateRepository = ArticleTemplateRepository { SAMPLE_JSON },
        )

        advanceUntilIdle()
        viewModel.selectTemplate("tech-0")
        advanceUntilIdle()

        assertEquals("minimalist-0", viewModel.uiState.value.selectedTemplateId)
        assertEquals("tech-0", viewModel.uiState.value.pendingTemplateId)
        assertEquals("minimalist-0", draftDao.savedDraft?.templateId)

        viewModel.confirmTemplateSelection()
        advanceUntilIdle()

        assertEquals("tech-0", viewModel.uiState.value.selectedTemplateId)
        assertEquals("科技·矩阵", viewModel.uiState.value.selectedTemplateName)
        assertEquals("科技风", viewModel.uiState.value.selectedTemplateCategory)
        assertEquals("tech-0", draftDao.savedDraft?.templateId)
    }

    private fun createTempPreferencesFile(): File {
        val directory = createTempDir(prefix = "editor-template-test")
        directory.deleteOnExit()
        return File(directory, "settings.preferences_pb").apply { deleteOnExit() }
    }

    private class FakeDraftDao(
        initialDraft: DraftEntity,
    ) : DraftDao {
        private val drafts = MutableStateFlow(listOf(initialDraft))
        var savedDraft: DraftEntity? = initialDraft

        override fun observeAll(): Flow<List<DraftEntity>> = drafts.asStateFlow()

        override suspend fun findById(id: Long): DraftEntity? = drafts.value.firstOrNull { it.id == id }

        override suspend fun upsert(entity: DraftEntity) {
            savedDraft = entity
            drafts.value = listOf(entity)
        }

        override suspend fun deleteById(id: Long) {
            drafts.value = drafts.value.filterNot { it.id == id }
            if (savedDraft?.id == id) {
                savedDraft = null
            }
        }
    }

    private class FakeHistoryDao : HistoryDao {
        override fun observeAll(): Flow<List<HistoryEntity>> = MutableStateFlow(emptyList<HistoryEntity>()).asStateFlow()

        override suspend fun findById(id: Long): HistoryEntity? = null

        override suspend fun upsert(entity: HistoryEntity) = Unit

        override suspend fun upsertAll(entities: List<HistoryEntity>) = Unit

        override suspend fun deleteById(id: Long) = Unit

        override suspend fun clear() = Unit
    }

    private companion object {
        const val SAMPLE_JSON = """
            [
              {
                "id":"minimalist-0","name":"极简·经典","description":"极简风模板","category":"minimalist","themeColor":"#3b82f6","backgroundColor":"#ffffff",
                "containerStyle":"padding:16px;","h1Style":"color:#3b82f6;","h2Style":"color:#111827;","h3Style":"color:#374151;",
                "pStyle":"line-height:1.8;","blockquoteStyle":"border-left:3px solid #3b82f6;","blockquoteInnerBefore":"","blockquoteInnerAfter":"",
                "listStyle":"margin:0;","listItemStyle":"margin:0 0 8px 0;","listIconHtml":"<section>•</section>","strongStyle":"font-weight:bold;",
                "emStyle":"font-style:italic;","codeContainerStyle":"border:1px solid #e5e7eb;","codeHeaderStyle":"background-color:#e2e8f0;",
                "codeBlockStyle":"font-family:monospace;","imgStyle":"max-width:100%;","hrStyle":"border-top:1px solid #e5e7eb;","linkStyle":"color:#3b82f6;",
                "tableStyle":"width:100%;","thStyle":"background-color:#f8fafc;","tdStyle":"background-color:#ffffff;","delStyle":"opacity:0.6;"
              },
              {
                "id":"tech-0","name":"科技·矩阵","description":"科技风模板","category":"tech","themeColor":"#0f766e","backgroundColor":"#ffffff",
                "containerStyle":"padding:18px;","h1Style":"color:#0f766e;","h2Style":"color:#115e59;","h3Style":"color:#134e4a;",
                "pStyle":"line-height:1.9;","blockquoteStyle":"border-left:4px solid #0f766e;","blockquoteInnerBefore":"","blockquoteInnerAfter":"",
                "listStyle":"margin:0;","listItemStyle":"margin:0 0 8px 0;","listIconHtml":"<section>◆</section>","strongStyle":"font-weight:bold;color:#0f766e;",
                "emStyle":"font-style:italic;","codeContainerStyle":"border:1px solid #0f766e;","codeHeaderStyle":"background-color:#ccfbf1;",
                "codeBlockStyle":"font-family:monospace;","imgStyle":"max-width:100%;","hrStyle":"border-top:1px solid #5eead4;","linkStyle":"color:#0f766e;",
                "tableStyle":"width:100%;","thStyle":"background-color:#ccfbf1;","tdStyle":"background-color:#ffffff;","delStyle":"opacity:0.6;"
              }
            ]
        """
    }
}
