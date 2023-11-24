package com.ivandev.todolist.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("USER_PREFERENCES_KEY")
const val USER_KEY = "USER_KEY"
const val TASK_KEY = "TASK_KEY"
const val USER_DB = "USERS"
const val USER_TASKS = "USER_TASKS"
const val NOTIFICATION_TITLE = "NOTIFICATION_TITLE"
const val NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE"
