package com.wdmaster.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // يمكن إعادة جدولة الاختبارات المجدولة أو تنظيف البيانات
            // حاليًا، لا نبدأ الخدمة تلقائيًا
        }
    }
}