package com.paiban.helper.ui.history

import com.paiban.helper.data.db.DraftDao
import com.paiban.helper.data.db.DraftEntity
import com.paiban.helper.data.db.HistoryDao
import com.paiban.helper.data.db.HistoryEntity
import com.paiban.helper.data.repository.EditorRepository
import com.paiban.helper.domain.analysis.ContentClassifier
import com.paiban.helper.domain.files.ImportExportManager
import com.paiban.helper.domain.render.HtmlSanitizer
import com.paiban.helper.domain.render.InlineArticleRenderer
import com.paiban.helper.domain.render.MarkdownConverter
import com.paiban.helper.domain.render.PreviewDocumentBuilder
import com.paiban.helper.domain.template.ArticleTemplateRepository
import com.paiban.helper.test.MainDispatcherRule
import com.paiban.helper.ui.preview.HistoryPreviewSelection
import com.paiban.helper.ui.preview.PreviewViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelActionTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun clearPreviewSelection() {
        HistoryPreviewSelection.clear()
    }

    @Test
    fun editHistoryRestoresDraftForEditor() = runTest {
        val history = historyEntity(
            id = 8L,
            title = "",
            rawContent = "# 标题",
        )
        val draftDao = FakeDraftDao()
        val historyDao = FakeHistoryDao(history)
        val repository = EditorRepository(draftDao, historyDao)
        val viewModel = HistoryViewModel(repository)

        viewModel.editHistory(history)
        advanceUntilIdle()

        assertEquals("# 标题", draftDao.savedDraft?.rawContent)
    }

    @Test
    fun previewHistorySelectsHistoryForPreviewRoute() = runTest {
        val draftDao = FakeDraftDao(
            DraftEntity(
                id = 1L,
                title = "工作草稿",
                rawContent = "# 草稿标题\n\n草稿正文",
                lastRenderedHtml = "",
                contentType = "Markdown",
                templateId = "minimalist-0",
                createdAt = 1L,
                updatedAt = 1L,
            )
        )
        val history = historyEntity(
            id = 11L,
            title = "",
            rawContent = "# 历史标题\n\n历史正文",
            templateId = "business-0",
        )
        val repository = EditorRepository(draftDao, FakeHistoryDao(history))
        val historyViewModel = HistoryViewModel(repository)
        val previewViewModel = PreviewViewModel(
            editorRepository = repository,
            builder = previewBuilder(),
            importExportManager = ImportExportManager(),
        )

        advanceUntilIdle()
        historyViewModel.previewHistory(history)
        advanceUntilIdle()

        assertEquals(11L, HistoryPreviewSelection.selectedHistory.value?.id)
        assertEquals("business-0", previewViewModel.uiState.value.templateId)
        assertTrue(previewViewModel.uiState.value.publishHtml.contains("历史标题"))
    }

    @Test
    fun deletingSelectedHistoryClearsPreviewSelection() = runTest {
        val history = historyEntity(
            id = 15L,
            title = "导入文章",
            rawContent = "# 标题\n\n正文",
        )
        val repository = EditorRepository(FakeDraftDao(), FakeHistoryDao(history))
        val viewModel = HistoryViewModel(repository)

        viewModel.previewHistory(history)
        advanceUntilIdle()
        viewModel.deleteHistory(history)
        advanceUntilIdle()

        assertNull(HistoryPreviewSelection.selectedHistory.value)
    }

    private fun historyEntity(
        id: Long,
        title: String,
        rawContent: String,
        templateId: String = "minimalist-0",
    ) = HistoryEntity(
        id = id,
        title = title,
        rawContent = rawContent,
        lastRenderedHtml = "",
        contentType = "Markdown",
        templateId = templateId,
        isFavorite = false,
        createdAt = 1L,
        updatedAt = 2L,
    )

    private fun previewBuilder() = PreviewDocumentBuilder(
        classifier = ContentClassifier(),
        markdownConverter = MarkdownConverter(),
        sanitizer = HtmlSanitizer(),
        templateRepository = ArticleTemplateRepository { SAMPLE_JSON },
        inlineArticleRenderer = InlineArticleRenderer(),
    )

    private class FakeDraftDao(
        initialDraft: DraftEntity? = null,
    ) : DraftDao {
        private val drafts = MutableStateFlow(initialDraft?.let(::listOf) ?: emptyList())

        var savedDraft: DraftEntity? = initialDraft
            private set

        override fun observeAll(): Flow<List<DraftEntity>> = drafts.asStateFlow()

        override suspend fun findById(id: Long): DraftEntity? = drafts.value.firstOrNull { it.id == id }

        override suspend fun upsert(entity: DraftEntity) {
            savedDraft = entity
            drafts.value = listOf(entity)
        }

        override suspend fun deleteById(id: Long) {
            if (savedDraft?.id == id) {
                savedDraft = null
            }
            drafts.value = drafts.value.filterNot { it.id == id }
        }
    }

    private class FakeHistoryDao(
        history: HistoryEntity? = null,
    ) : HistoryDao {
        private val saved = MutableStateFlow(history?.let(::listOf) ?: emptyList())

        override fun observeAll(): Flow<List<HistoryEntity>> = saved.asStateFlow()

        override suspend fun findById(id: Long): HistoryEntity? = saved.value.firstOrNull { it.id == id }

        override suspend fun upsert(entity: HistoryEntity) {
            saved.value = saved.value.filterNot { it.id == entity.id } + entity
        }

        override suspend fun upsertAll(entities: List<HistoryEntity>) {
            saved.value = entities
        }

        override suspend fun deleteById(id: Long) {
            saved.value = saved.value.filterNot { it.id == id }
        }

        override suspend fun clear() {
            saved.value = emptyList()
        }
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
                "id":"business-0","name":"商务·经典","description":"商务风模板","category":"business","themeColor":"#1e40af","backgroundColor":"#ffffff",
                "containerStyle":"padding:20px;","h1Style":"color:#1e40af;","h2Style":"background-color:#1e40af;color:#ffffff;","h3Style":"color:#1e40af;",
                "pStyle":"line-height:1.8;text-indent:2em;","blockquoteStyle":"border-left:6px solid #1e40af;","blockquoteInnerBefore":"","blockquoteInnerAfter":"",
                "listStyle":"margin:0;","listItemStyle":"margin:0 0 10px 0;","listIconHtml":"<section>■</section>","strongStyle":"font-weight:bold;color:#1e40af;",
                "emStyle":"font-style:italic;","codeContainerStyle":"border:1px solid #475569;","codeHeaderStyle":"background-color:#334155;",
                "codeBlockStyle":"font-family:monospace;color:#f8fafc;","imgStyle":"max-width:100%;","hrStyle":"border-top:2px dashed #1e40af;","linkStyle":"color:#1e40af;",
                "tableStyle":"width:100%;","thStyle":"background-color:#dbeafe;","tdStyle":"background-color:#ffffff;","delStyle":"opacity:0.6;"
              }
            ]
        """
    }
}
