package com.wdmaster.app.presentation.test

import android.webkit.WebView

class ResultChecker {

    fun check(
        webView: WebView,
        successIndicator: String,
        failureIndicator: String,
        callback: (Result) -> Unit
    ) {
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
            when {
                cleanResult == "success" -> callback(Result.Success)
                cleanResult == "failure" -> callback(Result.Failure)
                else -> callback(Result.Unknown)
            }
        }
    }

    sealed class Result {
        object Success : Result()
        object Failure : Result()
        object Unknown : Result()
    }
}