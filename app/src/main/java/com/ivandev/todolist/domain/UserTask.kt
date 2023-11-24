package com.ivandev.todolist.domain

import java.util.*

data class UserTask(
    val id: String,
    val name: String,
    val date: Calendar,
    val ampm: String,
    var made: Boolean,
    var notify: Boolean,
    var isSelected: Boolean
)
