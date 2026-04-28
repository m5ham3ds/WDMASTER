package com.wdmaster.app.presentation.test

import android.webkit.WebView
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InjectionManager {

    fun injectCredentials(
        webView: WebView,
        username: String,
        password: String,
        usernameSelector: String,
        passwordSelector: String,
        submitSelector: String
    ) {
        val js = """
            (function() {
                try {
                    var u = document.querySelector('$usernameSelector');
                    var p = document.querySelector('$passwordSelector');
                    var s = document.querySelector('$submitSelector');
                    if (u && p && s) {
                        u.value = '$username';
                        p.value = '$password';
                        s.click();
                        return 'injected';
                    } else {
                        return 'selectors not found';
                    }
                } catch(e) {
                    return 'error: ' + e.message;
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    suspend fun injectAndCheck(
        webView: WebView,
        router: RouterProfileEntity,
        card: String,
        customJs: String
    ): TestResult = suspendCancellableCoroutine { cont ->
        val js = if (customJs.isNotEmpty()) {
            customJs.replace("CARD_PLACEHOLDER", card)
        } else {
            """
            (function() {
                var u = document.querySelector('${router.usernameSelector}');
                var p = document.querySelector('${router.passwordSelector}');
                var s = document.querySelector('${router.submitSelector}');
                if (u && p && s) {
                    u.value = '$card';
                    p.value = '';
                    s.click();
                    return 'injected';
                }
                return 'selectors not found';
            })();
            """.trimIndent()
        }

        webView.evaluateJavascript(js) { result ->
            // بعد الحقن، نفحص النتيجة في الصفحة الحالية (قد تكون صفحة نجاح أو صفحة الدخول مجددًا)
            checkResult(webView, router.successIndicator, router.failureIndicator) { check ->
                when (check) {
                    ResultChecker.Result.Success -> cont.resume(TestResult.SUCCESS)
                    ResultChecker.Result.Failure -> cont.resume(TestResult.FAILURE)
                    else -> cont.resume(TestResult.UNKNOWN)
                }
            }
        }
    }

    fun checkResult(
        webView: WebView,
        successIndicator: String,
        failureIndicator: String,
        callback: (ResultChecker.Result) -> Unit
    ) {
        val js = """
            (function() {
                var html = document.documentElement.innerHTML;
                if (html.indexOf('$successIndicator') !== -1) {
                    return 'success';
                } else if (html.indexOf('$failureIndicator') !== -1) {
                    return 'failure';
                } else {
                    return 'unknown';
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js) { result ->
            val cleanResult = result.trim('"').trim()
            when {
                cleanResult == "success" -> callback(ResultChecker.Result.Success)
                cleanResult == "failure" -> callback(ResultChecker.Result.Failure)
                else -> callback(ResultChecker.Result.Unknown)
            }
        }
    }

    fun performLogout(webView: WebView, logoutSelector: String?) {
        val js = if (!logoutSelector.isNullOrEmpty()) {
            "document.querySelector('${logoutSelector}').click();"
        } else {
            """
            (function() {
                var links = document.querySelectorAll('a');
                for (var i = 0; i < links.length; i++) {
                    if (links[i].textContent.toLowerCase().indexOf('logout') !== -1) {
                        links[i].click();
                        return 'logged out';
                    }
                }
                return 'no logout found';
            })();
            """.trimIndent()
        }
        webView.evaluateJavascript(js, null)
    }

    enum class TestResult { SUCCESS, FAILURE, UNKNOWN }
}