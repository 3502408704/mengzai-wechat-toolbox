package com.paiban.helper.data.repository

import com.paiban.helper.data.db.AiConfigDao
import com.paiban.helper.data.db.AiConfigEntity
import com.paiban.helper.domain.ai.AiSecretCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AiSettingsRepositoryTest {
    @Test
    fun builtInDeepSeekConfigIsAlwaysPresentAndReadOnly() = runTest {
        val repo = AiSettingsRepository(
            FakeAiConfigDao(),
            AiSecretCrypto(keyProvider = { fixedKey() }),
        )

        val configs = repo.observeConfigs().first()

        assertTrue(configs.any { it.id == AiSettingsRepository.BUILT_IN_CONFIG_ID })
        assertTrue(configs.single { it.id == AiSettingsRepository.BUILT_IN_CONFIG_ID }.isBuiltIn)
    }

    private class FakeAiConfigDao : AiConfigDao {
        override fun observeAll(): Flow<List<AiConfigEntity>> = flowOf(emptyList())

        override suspend fun findById(id: Long): AiConfigEntity? = null

        override suspend fun upsert(entity: AiConfigEntity) = Unit

        override suspend fun deleteById(id: Long) = Unit
    }

    private fun fixedKey(): SecretKey {
        return SecretKeySpec(ByteArray(32) { 7 }, "AES")
    }
}
