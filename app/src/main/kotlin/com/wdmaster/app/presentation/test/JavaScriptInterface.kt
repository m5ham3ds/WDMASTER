package com.wdmaster.app.presentation.test

import android.webkit.JavascriptInterface

class JavaScriptInterface(private val viewModel: TestViewModel) {

    @JavascriptInterface
    fun onResult(result: String) {
        // يتم استدعاؤها من JavaScript عند اكتمال التحقق
        viewModel.onJsResult(result)
    }

    @JavascriptInterface
    fun onError(error: String) {
        viewModel.onJsError(error)
    }
}

// دوال إضافية في TestViewModel لاستقبال نتائج JavaScript
fun TestViewModel.onJsResult(result: String) {
    // معالجة النتيجة من WebView
}

fun TestViewModel.onJsError(error: String) {
    // معالجة الخطأ من WebView
}