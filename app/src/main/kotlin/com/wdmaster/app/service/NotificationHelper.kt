package com.wdmaster.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.wdmaster.app.R
import com.wdmaster.app.presentation.MainActivity
import com.wdmaster.app.util.Constants

class NotificationHelper(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    private fun createChannels() {
        val testChannel = NotificationChannel(
            Constants.CHANNEL_TEST,
            context.getString(R.string.notification_channel_test),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Testing status notifications"
        }

        val resultChannel = NotificationChannel(
            Constants.CHANNEL_RESULT,
            context.getString(R.string.notification_channel_result),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Test result notifications"
        }

        notificationManager.createNotificationChannel(testChannel)
        notificationManager.createNotificationChannel(resultChannel)
    }

    fun buildForegroundNotification(
        title: String,
        progress: Int,
        max: Int,
        isPaused: Boolean = false
    ): android.app.Notification {

        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // إجراء الإيقاف المؤقت / الاستئناف
        val pauseResumeAction = if (isPaused) {
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_RESUME
            }
        } else {
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_PAUSE
            }
        }
        val pauseResumePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            pauseResumeAction,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // إجراء الإلغاء
        val cancelIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_TEST)
            .setContentTitle(title)
            .setContentText("$progress / $max")
            .setSmallIcon(R.drawable.ic_test)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .setProgress(max, progress, false)
            .addAction(
                R.drawable.ic_pause,
                if (isPaused) context.getString(R.string.notification_resume)
                else context.getString(R.string.notification_pause),
                pauseResumePendingIntent
            )
            .addAction(
                R.drawable.ic_stop,
                context.getString(R.string.notification_cancel),
                cancelPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    fun showResultNotification(
        title: String,
        body: String,
        isSuccess: Boolean
    ) {
        val icon = if (isSuccess) R.drawable.ic_success else R.drawable.ic_failure

        val openIntent = PendingIntent.getActivity(
            context,
            3,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_RESULT)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        notificationManager.notify(Constants.NOTIFICATION_RESULT_ID, notification)
    }

    fun cancelTestNotification() {
        notificationManager.cancel(Constants.NOTIFICATION_TEST_ID)
    }
}