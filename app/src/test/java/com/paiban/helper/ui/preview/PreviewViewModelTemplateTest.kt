package com.paiban.helper.ui.preview

import com.paiban.helper.data.db.DraftDao
import com.paiban.helper.data.db.DraftEntity
import com.paiban.helper.data.db.HistoryDao
import com.paiban.helper.data.db.HistoryEntity
import com.paiban.helper.data.repository.EditorRepository
import com.paiban.helper.domain.files.ImportExportManager
import com.paiban.helper.domain.render.HtmlSanitizer
import com.paiban.helper.domain.render.InlineArticleRenderer
import com.paiban.helper.domain.render.MarkdownConverter
import com.paiban.helper.domain.render.PreviewDocumentBuilder
import com.paiban.helper.domain.analysis.ContentClassifier
import com.paiban.helper.domain.template.ArticleTemplateRepository
import com.paiban.helper.test.MainDispatcherRule
import org.junit.After
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
class PreviewViewModelTemplateTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @After
    fun clearSelectedHistory() {
        HistoryPreviewSelection.clear()
    }

    @Test
    fun refreshUsesDraftTemplateIdForPayloadAndHistory() = runTest {
        HistoryPreviewSelection.clear()
        val draftDao = FakeDraftDao(
            DraftEntity(
                id = 1L,
                title = "模板文章",
                rawContent = "# 标题\n\n正文",
                lastRenderedHtml = "",
                contentType = "Markdown",
                templateId = "business-0",
                createdAt = 1L,
                updatedAt = 2L,
            )
        )
        val historyDao = FakeHistoryDao()
        val repository = EditorRepository(draftDao, historyDao)
        val builder = PreviewDocumentBuilder(
            classifier = ContentClassifier(),
            markdownConverter = MarkdownConverter(),
            sanitizer = HtmlSanitizer(),
            templateRepository = ArticleTemplateRepository { SAMPLE_JSON },
            inlineArticleRenderer = InlineArticleRenderer(),
        )
        val viewModel = PreviewViewModel(
            editorRepository = repository,
            builder = builder,
            importExportManager = ImportExportManager(),
        )

        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals("business-0", viewModel.uiState.value.templateId)
        assertTrue(
            viewModel.uiState.value.publishHtml.contains("text-indent:2em") ||
                viewModel.uiState.value.publishHtml.contains("text-indent: 2em"),
        )
        assertEquals("business-0", historyDao.saved.single().templateId)
    }

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

    private class FakeHistoryDao : HistoryDao {
        val saved = mutableListOf<HistoryEntity>()

        override fun observeAll(): Flow<List<HistoryEntity>> = MutableStateFlow(saved.toList()).asStateFlow()

        override suspend fun findById(id: Long): HistoryEntity? = saved.firstOrNull { it.id == id }

        override suspend fun upsert(entity: HistoryEntity) {
            saved.removeAll { it.id == entity.id }
            saved += entity
        }

        override suspend fun upsertAll(entities: List<HistoryEntity>) {
            saved.clear()
            saved += entities
        }

        override suspend fun deleteById(id: Long) {
            saved.removeAll { it.id == id }
        }

        override suspend fun clear() {
            saved.clear()
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
