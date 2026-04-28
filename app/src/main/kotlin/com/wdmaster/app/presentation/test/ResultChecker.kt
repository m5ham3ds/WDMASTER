package com.wdmaster.app.presentation.test

import android.webkit.WebView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ResultChecker {

    suspend fun check(
        webView: WebView,
        successIndicator: String,
        failureIndicator: String
    ): Result = suspendCancellableCoroutine { cont ->
        val js = """
            (function() {
                var html = document.documentElement.outerHTML;
                if (html.indexOf('$successIndicator') !== -1) return 'success';
                if (html.indexOf('$failureIndicator') !== -1) return 'failure';
                return 'unknown';
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            val cleanResult = result.trim('"').trim()
            val finalResult = when {
                cleanResult == "success" -> Result.Success
                cleanResult == "failure" -> Result.Failure
                else -> Result.Unknown
            }
            cont.resume(finalResult)
        }
    }

    sealed class Result {
        object Success : Result()
        object Failure : Result()
        object Unknown : Result()
    }
}
