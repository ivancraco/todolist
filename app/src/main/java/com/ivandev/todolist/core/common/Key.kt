package com.ivandev.todolist.core.common

import java.security.MessageDigest
import java.util.*
import javax.crypto.spec.SecretKeySpec

object Key {
    fun genKey(): String {
        return UUID.randomUUID().toString()
    }

    fun generateKey(text: String): SecretKeySpec {
        val sha: MessageDigest = MessageDigest.getInstance("SHA-256")
        var key = text.encodeToByteArray()
        key = sha.digest(key)
        return SecretKeySpec(key, "AES")
    }
}