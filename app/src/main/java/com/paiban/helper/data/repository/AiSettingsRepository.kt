package com.paiban.helper.data.repository

import com.paiban.helper.data.db.AiConfigDao
import com.paiban.helper.data.db.AiConfigEntity
import com.paiban.helper.domain.ai.AiSecretCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AiSettingsRepository(
    private val dao: AiConfigDao,
    private val crypto: AiSecretCrypto,
    private val builtInApiKey: String = "",
) {
    fun observeConfigs(): Flow<List<AiConfigSummary>> {
        return dao.observeAll().map { configs ->
            seedBuiltInConfig(configs).map { entity ->
                entity.toSummary()
            }
        }
    }

    suspend fun upsertConfig(summary: AiConfigSummary, apiKeyPlainText: String) {
        require(!summary.isBuiltIn) { "Built-in config is read-only" }
        dao.upsert(
            AiConfigEntity(
                id = summary.id,
                displayName = summary.displayName,
                provider = summary.provider,
                model = summary.model,
                baseUrl = summary.baseUrl,
                apiKeyEncrypted = crypto.encrypt(apiKeyPlainText),
                isBuiltIn = false,
                isActive = summary.isActive,
                createdAt = summary.createdAt,
                updatedAt = summary.updatedAt,
            )
        )
    }

    suspend fun deleteConfig(id: Long) {
        require(id != BUILT_IN_CONFIG_ID) { "Built-in config is read-only" }
        dao.deleteById(id)
    }

    private fun seedBuiltInConfig(configs: List<AiConfigEntity>): List<AiConfigEntity> {
        return if (configs.any { it.id == BUILT_IN_CONFIG_ID }) {
            configs.map { entity ->
                if (entity.id == BUILT_IN_CONFIG_ID) defaultBuiltInConfig().copy(
                    displayName = entity.displayName,
                    provider = entity.provider,
                    model = entity.model,
                    baseUrl = entity.baseUrl,
                    isActive = entity.isActive,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                ) else entity
            }
        } else {
            listOf(defaultBuiltInConfig()) + configs
        }
    }

    private fun defaultBuiltInConfig(): AiConfigEntity {
        val now = 0L
        return AiConfigEntity(
            id = BUILT_IN_CONFIG_ID,
            displayName = "DeepSeek 默认",
            provider = "deepseek",
            model = "deepseek-v4-flash",
            baseUrl = "https://api.deepseek.com",
            apiKeyEncrypted = builtInEncryptedSecret(),
            isBuiltIn = true,
            isActive = true,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun AiConfigEntity.toSummary(): AiConfigSummary {
        return AiConfigSummary(
            id = id,
            displayName = displayName,
            provider = provider,
            model = model,
            baseUrl = baseUrl,
            isBuiltIn = isBuiltIn,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        const val BUILT_IN_CONFIG_ID = 1L
    }

    private fun builtInEncryptedSecret(): String {
        if (builtInApiKey.isBlank()) return ""
        return crypto.encrypt(builtInApiKey)
    }
}

data class AiConfigSummary(
    val id: Long,
    val displayName: String,
    val provider: String = "deepseek",
    val model: String,
    val baseUrl: String = "https://api.deepseek.com",
    val isBuiltIn: Boolean,
    val isActive: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
