package com.paiban.helper.domain.ai

import java.nio.charset.Charset
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AiSecretCrypto(
    private val keyProvider: () -> SecretKey = { generateKey() },
) {
    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyProvider()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(cipher.iv + encrypted)
    }

    fun decrypt(cipherText: String): String {
        if (cipherText.isBlank()) return ""
        val decoded = Base64.getDecoder().decode(cipherText)
        val iv = decoded.copyOfRange(0, GCM_IV_LENGTH)
        val payload = decoded.copyOfRange(GCM_IV_LENGTH, decoded.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keyProvider(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return cipher.doFinal(payload).toString(Charset.forName("UTF-8"))
    }

    companion object {
        private const val KEY_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH_BITS = 128

        private fun generateKey(): SecretKey {
            val generator = KeyGenerator.getInstance(KEY_ALGORITHM)
            generator.init(256)
            return generator.generateKey()
        }
    }
}
