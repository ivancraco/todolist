package com.ivandev.todolist.domain

enum class TaskDay(val day: Int) {
    TODAY(1),
    TOMORROW(2),
    OTHERS(3)
}