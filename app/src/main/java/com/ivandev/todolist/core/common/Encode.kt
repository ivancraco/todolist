package com.ivandev.todolist.core.common

import android.util.Base64
import com.ivandev.todolist.core.common.Key.generateKey
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Encode {
    fun encode(text: String, pass: String): String {
        val secretKey: SecretKeySpec = generateKey(pass)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        val encryptedDataBytes = cipher.doFinal(text.encodeToByteArray())
        return Base64.encodeToString(encryptedDataBytes, Base64.DEFAULT)
    }
}