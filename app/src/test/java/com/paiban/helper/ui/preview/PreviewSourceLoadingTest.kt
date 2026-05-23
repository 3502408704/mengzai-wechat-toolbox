package com.paiban.helper.ui.preview

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreviewSourceLoadingTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun editorSourceLoadsWorkingDraftPreview() = runTest {
        val viewModel = buildPreviewViewModel(
            draft = draftEntity(raw = "# Title", templateId = "business-0"),
            history = emptyList(),
        )

        viewModel.loadSource(PreviewRouteSource.Editor)
        advanceUntilIdle()

        assertEquals("business-0", viewModel.uiState.value.templateId)
        assertEquals(PreviewSource.Draft, viewModel.uiState.value.source)
    }

    @Test
    fun editorSourceAlsoSavesHistorySnapshotWhenPreviewLoads() = runTest {
        val historyDao = RecordingHistoryDao(emptyList())
        val repository = EditorRepository(
            draftDao = FakeDraftDao(draftEntity(raw = "# Title", templateId = "business-0")),
            historyDao = historyDao,
        )
        val viewModel = PreviewViewModel(
            editorRepository = repository,
            builder = previewBuilder(),
            importExportManager = ImportExportManager(),
        )

        viewModel.loadSource(PreviewRouteSource.Editor)
        advanceUntilIdle()

        assertEquals(1, historyDao.savedHistory.size)
        assertTrue(historyDao.savedHistory.single().rawContent.contains("# Title"))
    }

    @Test
    fun historySourceLoadsRequestedHistoryRecord() = runTest {
        val history = historyEntity(
            id = 42L,
            raw = "<div>Saved</div>",
            templateId = "tech-0",
            lastRenderedHtml = "<html><body><div>Saved</div></body></html>",
        )
        val viewModel = buildPreviewViewModel(
            draft = draftEntity(raw = "# Draft", templateId = "business-0"),
            history = listOf(history),
        )

        viewModel.loadSource(PreviewRouteSource.History(42L))
        advanceUntilIdle()

        assertEquals("tech-0", viewModel.uiState.value.templateId)
        assertEquals(PreviewSource.History(historyId = 42L), viewModel.uiState.value.source)
    }

    @Test
    fun missingHistorySourceShowsUnavailableState() = runTest {
        val viewModel = buildPreviewViewModel(
            draft = draftEntity(raw = "# Draft", templateId = "minimalist-0"),
            history = emptyList(),
        )

        viewModel.loadSource(PreviewRouteSource.History(404L))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isUnavailable)
        assertEquals("这条历史记录已不存在或无法读取", viewModel.uiState.value.unavailableMessage)
    }

    private fun buildPreviewViewModel(
        draft: DraftEntity,
        history: List<HistoryEntity>,
    ): PreviewViewModel {
        val repository = EditorRepository(
            draftDao = FakeDraftDao(draft),
            historyDao = RecordingHistoryDao(history),
        )
        return PreviewViewModel(
            editorRepository = repository,
            builder = previewBuilder(),
            importExportManager = ImportExportManager(),
        )
    }

    private fun draftEntity(
        raw: String,
        templateId: String,
    ) = DraftEntity(
        id = 1L,
        title = "Draft",
        rawContent = raw,
        lastRenderedHtml = "",
        contentType = "Markdown",
        templateId = templateId,
        createdAt = 1L,
        updatedAt = 2L,
    )

    private fun historyEntity(
        id: Long,
        raw: String,
        templateId: String,
        lastRenderedHtml: String,
    ) = HistoryEntity(
        id = id,
        title = "History-$id",
        rawContent = raw,
        lastRenderedHtml = lastRenderedHtml,
        contentType = "Html",
        templateId = templateId,
        isFavorite = false,
        createdAt = 1L,
        updatedAt = 2L,
    )

    private class FakeDraftDao(
        initialDraft: DraftEntity,
    ) : DraftDao {
        private val drafts = MutableStateFlow(listOf(initialDraft))

        override fun observeAll(): Flow<List<DraftEntity>> = drafts.asStateFlow()

        override suspend fun findById(id: Long): DraftEntity? = drafts.value.firstOrNull { it.id == id }

        override suspend fun upsert(entity: DraftEntity) {
            drafts.value = listOf(entity)
        }

        override suspend fun deleteById(id: Long) {
            drafts.value = drafts.value.filterNot { it.id == id }
        }
    }

    private class RecordingHistoryDao(
        initialHistory: List<HistoryEntity>,
    ) : HistoryDao {
        private val history = MutableStateFlow(initialHistory)
        val savedHistory = mutableListOf<HistoryEntity>()

        override fun observeAll(): Flow<List<HistoryEntity>> = history.asStateFlow()

        override suspend fun findById(id: Long): HistoryEntity? = history.value.firstOrNull { it.id == id }

        override suspend fun upsert(entity: HistoryEntity) {
            savedHistory.removeAll { it.id == entity.id }
            savedHistory += entity
            history.value = history.value.filterNot { it.id == entity.id } + entity
        }

        override suspend fun upsertAll(entities: List<HistoryEntity>) {
            history.value = entities
        }

        override suspend fun deleteById(id: Long) {
            history.value = history.value.filterNot { it.id == id }
        }

        override suspend fun clear() {
            history.value = emptyList()
        }
    }

    private fun previewBuilder() = PreviewDocumentBuilder(
        classifier = ContentClassifier(),
        markdownConverter = MarkdownConverter(),
        sanitizer = HtmlSanitizer(),
        templateRepository = ArticleTemplateRepository { SAMPLE_JSON },
        inlineArticleRenderer = InlineArticleRenderer(),
    )

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
              },
              {
                "id":"tech-0","name":"科技·终端","description":"科技风模板","category":"tech","themeColor":"#2563eb","backgroundColor":"#0f172a",
                "containerStyle":"padding:20px;background-color:#0f172a;","h1Style":"color:#10b981;","h2Style":"border-left:6px solid #2563eb;","h3Style":"color:#2563eb;",
                "pStyle":"line-height:1.8;color:#cbd5e1;","blockquoteStyle":"border:1px solid #2563eb;background-color:#1e293b;","blockquoteInnerBefore":"<span>&gt;</span>","blockquoteInnerAfter":"",
                "listStyle":"margin:0;","listItemStyle":"margin:0 0 10px 0;","listIconHtml":"<section>/&gt;</section>","strongStyle":"border-bottom:1px solid #2563eb;color:#ffffff;",
                "emStyle":"text-decoration:underline;color:#2563eb;","codeContainerStyle":"border:1px solid #334155;background-color:#000000;","codeHeaderStyle":"background-color:#1e293b;",
                "codeBlockStyle":"font-family:monospace;color:#10b981;","imgStyle":"max-width:100%;border:2px solid #334155;","hrStyle":"border-top:1px solid #334155;","linkStyle":"color:#2563eb;",
                "tableStyle":"width:100%;background-color:#0f172a;","thStyle":"background-color:#1e293b;color:#ffffff;","tdStyle":"background-color:#0f172a;color:#cbd5e1;","delStyle":"opacity:0.5;"
              }
            ]
        """
    }
}
