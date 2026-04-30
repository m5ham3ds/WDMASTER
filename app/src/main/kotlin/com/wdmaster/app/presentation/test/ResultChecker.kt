package com.wdmaster.app.presentation.test

import android.webkit.WebView
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ResultChecker {

    suspend fun check(webView: WebView, success: String, failure: String): Result {
        // انتظر قليلاً ليتم تحديث DOM بعد الحقن
        delay(1500)
        return suspendCancellableCoroutine { cont ->
            val js = """
                (function() {
                    var html = document.documentElement.innerHTML;
                    if (html.indexOf('$success') !== -1) return 'success';
                    if (html.indexOf('$failure') !== -1) return 'failure';
                    var bodyText = document.body.innerText || '';
                    if (bodyText.indexOf('نجاح') !== -1) return 'success';
                    if (bodyText.indexOf('خطأ') !== -1 || bodyText.indexOf('فشل') !== -1) return 'failure';
                    return 'unknown';
                })();
            """.trimIndent()
            webView.evaluateJavascript(js) { result ->
                val clean = result.trim('"').trim()
                cont.resume(
                    when (clean) {
                        "success" -> Result.Success
                        "failure" -> Result.Failure
                        else -> Result.Unknown
                    }
                )
            }
        }
    }

    sealed class Result {
        object Success : Result()
        object Failure : Result()
        object Unknown : Result()
    }
}