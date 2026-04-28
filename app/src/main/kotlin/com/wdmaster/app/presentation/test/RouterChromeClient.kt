package com.wdmaster.app.presentation.test

import android.webkit.WebChromeClient
import android.webkit.WebView

class RouterChromeClient(private val viewModel: TestViewModel) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        // يمكن استخدامها لتحديث شريط التقدم
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        // يمكن استخدامها للتحقق من عنوان الصفحة
    }
}