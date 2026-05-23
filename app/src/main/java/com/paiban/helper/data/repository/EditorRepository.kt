package com.paiban.helper.data.repository

import com.paiban.helper.data.db.DraftDao
import com.paiban.helper.data.db.DraftEntity
import com.paiban.helper.data.db.HistoryDao
import com.paiban.helper.data.db.HistoryEntity
import kotlinx.coroutines.flow.Flow

class EditorRepository(
    private val draftDao: DraftDao,
    private val historyDao: HistoryDao,
) {
    fun observeDrafts(): Flow<List<DraftEntity>> = draftDao.observeAll()

    suspend fun findDraftById(id: Long): DraftEntity? = draftDao.findById(id)

    suspend fun deleteDraftById(id: Long) {
        draftDao.deleteById(id)
    }

    fun observeHistory(): Flow<List<HistoryEntity>> = historyDao.observeAll()

    suspend fun findHistoryById(id: Long): HistoryEntity? = historyDao.findById(id)

    suspend fun saveDraft(entity: DraftEntity) = draftDao.upsert(entity)

    suspend fun saveHistory(entity: HistoryEntity) = historyDao.upsert(entity)

    suspend fun deleteHistoryById(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun restoreHistoryToDraft(historyId: Long, draftId: Long = historyId): DraftEntity? {
        val history = historyDao.findById(historyId) ?: return null
        val draft = DraftEntity(
            id = draftId,
            title = history.title,
            rawContent = history.rawContent,
            lastRenderedHtml = history.lastRenderedHtml,
            contentType = history.contentType,
            templateId = history.templateId,
            createdAt = history.createdAt,
            updatedAt = history.updatedAt,
        )
        draftDao.upsert(draft)
        return draft
    }

    suspend fun clearHistory() {
        historyDao.clear()
    }

    suspend fun replaceHistory(items: List<HistoryEntity>, limit: Int) {
        clearHistory()
        historyDao.upsertAll(trimHistory(items, limit))
    }

    companion object {
        @JvmStatic
        fun trimHistory(items: List<HistoryEntity>, limit: Int): List<HistoryEntity> {
            if (limit <= 0 || items.isEmpty()) {
                return emptyList()
            }
            if (items.size <= limit) {
                return items
            }

            val favorites = items.filter { it.isFavorite }
            val nonFavorites = items.filterNot { it.isFavorite }
                .sortedByDescending { it.updatedAt }
            val keepCount = (limit - favorites.size).coerceAtLeast(0)
            val selected = LinkedHashMap<Long, HistoryEntity>()

            favorites.forEach { selected[it.id] = it }
            nonFavorites.take(keepCount).forEach { selected[it.id] = it }

            return selected.values.sortedByDescending { it.updatedAt }
        }
    }
}
