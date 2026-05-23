package com.paiban.helper.data.repository

import com.paiban.helper.data.db.DraftDao
import com.paiban.helper.data.db.DraftEntity
import com.paiban.helper.data.db.HistoryDao
import com.paiban.helper.data.db.HistoryEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditorRepositoryRestoreTest {
    @Test
    fun restoreHistoryToDraftReturnsNullWhenMissing() = runBlocking {
        val repository = EditorRepository(
            draftDao = FakeDraftDao(),
            historyDao = FakeHistoryDao(),
        )

        val result = repository.restoreHistoryToDraft(historyId = 42)

        assertNull(result)
    }

    @Test
    fun restoreHistoryToDraftSavesMappedDraft() = runBlocking {
        val draftDao = FakeDraftDao()
        val historyDao = FakeHistoryDao(
            history = HistoryEntity(7, "title", "body", "<p>body</p>", "Markdown", "minimalist-0", false, 100, 200),
        )
        val repository = EditorRepository(draftDao = draftDao, historyDao = historyDao)

        val result = repository.restoreHistoryToDraft(historyId = 7, draftId = 99)

        assertEquals(DraftEntity(99, "title", "body", "<p>body</p>", "Markdown", "minimalist-0", 100, 200), result)
        assertEquals(result, draftDao.savedDraft)
    }

    @Test
    fun restoreHistoryToDraftCopiesTemplateId() = runBlocking {
        val draftDao = FakeDraftDao()
        val historyDao = FakeHistoryDao(
            history = HistoryEntity(10, "article", "# Title", "<p>Rendered</p>", "Markdown", "business-1", false, 1, 2),
        )
        val repository = EditorRepository(draftDao = draftDao, historyDao = historyDao)

        val restored = repository.restoreHistoryToDraft(historyId = 10, draftId = 1)

        assertEquals("business-1", restored?.templateId)
    }

    private class FakeDraftDao : DraftDao {
        var savedDraft: DraftEntity? = null

        override fun observeAll(): Flow<List<DraftEntity>> = flowOf(emptyList())

        override suspend fun findById(id: Long): DraftEntity? = savedDraft?.takeIf { it.id == id }

        override suspend fun upsert(entity: DraftEntity) {
            savedDraft = entity
        }

        override suspend fun deleteById(id: Long) {
            if (savedDraft?.id == id) savedDraft = null
        }
    }

    private class FakeHistoryDao(
        private val history: HistoryEntity? = null,
    ) : HistoryDao {
        override fun observeAll(): Flow<List<HistoryEntity>> = flowOf(history?.let { listOf(it) } ?: emptyList())

        override suspend fun findById(id: Long): HistoryEntity? = history?.takeIf { it.id == id }

        override suspend fun upsert(entity: HistoryEntity) = Unit

        override suspend fun upsertAll(entities: List<HistoryEntity>) = Unit

        override suspend fun deleteById(id: Long) = Unit

        override suspend fun clear() = Unit
    }
}
