package com.wdmaster.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val serviceIntent = Intent(context, TestService::class.java).apply {
            action = when (intent?.action) {
                ACTION_PAUSE -> TestService.ACTION_PAUSE
                ACTION_RESUME -> TestService.ACTION_RESUME
                ACTION_CANCEL -> TestService.ACTION_CANCEL
                else -> return
            }
        }
        context.startService(serviceIntent)
    }

    companion object {
        const val ACTION_PAUSE = "com.wdmaster.app.action.PAUSE"
        const val ACTION_RESUME = "com.wdmaster.app.action.RESUME"
        const val ACTION_CANCEL = "com.wdmaster.app.action.CANCEL"
    }
}