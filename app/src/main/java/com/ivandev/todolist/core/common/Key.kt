package com.ivandev.todolist.core.common

import java.util.*

object Key {
    fun genKey(): String {
        return UUID.randomUUID().toString()
    }
}