package com.ivandev.todolist

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ToDoApp : Application() {
    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()
        )
    }
}