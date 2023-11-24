package com.ivandev.todolist.domain

interface Repository {
    fun deleteTask(userKey: String, task: UserTask)
    fun getAllTasks(userKey: String, callback: (List<UserTask>) -> Unit)
    fun saveTask(task: UserTask, userKey: String)
    fun updateCheck(userKey: String, task: UserTask)
    fun updateNotify(userKey: String, taskKey: String)
}