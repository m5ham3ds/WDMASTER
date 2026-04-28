package com.wdmaster.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val newIntent = Intent().apply {
            action = when (intent?.action) {
                ACTION_PAUSE -> "com.wdmaster.app.action.PAUSE"
                ACTION_RESUME -> "com.wdmaster.app.action.RESUME"
                ACTION_CANCEL -> "com.wdmaster.app.action.CANCEL"
                else -> return
            }
        }
        context.sendBroadcast(newIntent)
    }

    companion object {
        const val ACTION_PAUSE = "com.wdmaster.app.action.PAUSE"
        const val ACTION_RESUME = "com.wdmaster.app.action.RESUME"
        const val ACTION_CANCEL = "com.wdmaster.app.action.CANCEL"
    }
}