package com.ivandev.todolist.core.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ivandev.todolist.R
import com.ivandev.todolist.domain.Repository
import com.ivandev.todolist.ui.introduction.IntroductionActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.random.Random

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val respository: Repository
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString(context.getString(R.string.notification_title))
        val notificationMessage =
            inputData.getString(context.getString(R.string.notification_message))
        val userKey = inputData.getString(context.getString(R.string.user_key))
        val taskKey = inputData.getString(context.getString(R.string.task_key))

        val mainIntent = Intent(context, IntroductionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flag = PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flag)
        val id = Random.nextInt(1000)
        val soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        createNotification(id, title, notificationMessage, pendingIntent, soundURI)

        if (userKey != null && taskKey != null) {
            respository.updateNotify(userKey, taskKey)
        }
        return Result.success()
    }

    private fun createNotification(
        id: Int,
        title: String?,
        notificationMessage: String?,
        pendingIntent: PendingIntent,
        soundURI: Uri
    ) {
        val channelID = context.getString(R.string.channel_id)
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(notificationMessage)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundURI)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }

}