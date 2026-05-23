package com.paiban.helper.domain.ai

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertEquals
import org.junit.Test

class AiSecretCryptoTest {
    @Test
    fun encryptAndDecryptRoundTripWithStableKey() {
        val crypto = AiSecretCrypto(keyProvider = { fixedKey() })

        val cipherText = crypto.encrypt("sk-test-123")

        assertEquals("sk-test-123", crypto.decrypt(cipherText))
    }

    private fun fixedKey(): SecretKey {
        return SecretKeySpec(ByteArray(32) { 7 }, "AES")
    }
}
