package com.ivandev.todolist.core.common

import com.ivandev.todolist.core.common.Key.generateKey
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Decode {
    fun decode(text: String, pass: String): String {
        //AES/CBC/PKCS5Padding
        val secretKey: SecretKeySpec = generateKey(pass)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        val decodedData = android.util.Base64.decode(text, android.util.Base64.DEFAULT)
        val desencryptedDataBytes = cipher.doFinal(decodedData)
        return String(desencryptedDataBytes)
    }
}