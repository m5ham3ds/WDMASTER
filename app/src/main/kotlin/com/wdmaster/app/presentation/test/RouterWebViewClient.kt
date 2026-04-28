package com.wdmaster.app.presentation.test

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class RouterWebViewClient(private val viewModel: TestViewModel) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        viewModel.onPageStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        viewModel.onPageFinished()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        error?.let {
            viewModel.onReceivedError(
                it.errorCode,
                it.description?.toString() ?: "Unknown error",
                request?.url?.toString()
            )
        }
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        // قبول الشهادات الذاتية (self-signed) لرواتر MikroTik
        handler?.proceed()
    }
}