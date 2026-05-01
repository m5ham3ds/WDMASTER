package com.wdmaster.app.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.http.SslError
import android.os.Build
import android.os.IBinder
import android.webkit.*
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.data.local.entity.TestSessionEntity
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.data.repository.TestResultRepository
import com.wdmaster.app.presentation.test.InjectionManager
import com.wdmaster.app.presentation.test.ResultChecker
import com.wdmaster.app.util.Constants
import com.wdmaster.app.util.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ServiceState(
    val progress: Int = 0,
    val total: Int = 0,
    val currentCard: String = "",
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val isPaused: Boolean = false,
    val status: String = "IDLE",
    val error: String? = null,
    val screenshot: Bitmap? = null
)

class TestService : Service(), KoinComponent {

    private val routerRepository: RouterRepository by inject()
    private val testResultRepository: TestResultRepository by inject()
    private val sessionRepository: SessionRepository by inject()
    private val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }

    private val logger = Logger("TestService")
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var webView: WebView? = null
    private var testJob: Job? = null
    private var currentProgress = 0
    private var totalCards = 0
    private var routerId = 0L
    private var sessionId = 0L
    private var delayMs = 500L
    private var cardList = listOf<String>()
    private var successCount = 0
    private var failureCount = 0

    private val _serviceState = MutableStateFlow(ServiceState())
    val serviceState: StateFlow<ServiceState> = _serviceState

    private var pageLoaded = CompletableDeferred<Unit>()
    private var pageError = false

    // جسر JavaScript لتلقي النتائج
    private var jsResultReceived = CompletableDeferred<String>()

    private var screenshotJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createWebView()
        startScreenshotLoop()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView() {
        webView = WebView(this).apply {
            layout(0, 0, 1080, 1920)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                allowFileAccess = false
                allowContentAccess = false
                javaScriptCanOpenWindowsAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }
            // إضافة جسر التواصل مع JavaScript
            addJavascriptInterface(object {
                @JavascriptInterface
                fun onResult(result: String) {
                    if (!jsResultReceived.isCompleted) {
                        jsResultReceived.complete(result)
                    }
                }
            }, "AndroidBridge")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (!pageLoaded.isCompleted) pageLoaded.complete(Unit)
                }
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    pageError = true
                    if (!pageLoaded.isCompleted) pageLoaded.complete(Unit)
                }
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    handler?.proceed()
                }
            }
            webChromeClient = WebChromeClient()
        }
    }

    override fun onBind(intent: Intent?): IBinder = ServiceBinder(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                routerId = intent.getLongExtra(EXTRA_ROUTER_ID, 0L)
                delayMs = intent.getLongExtra(EXTRA_DELAY_MS, 500L)
                cardList = intent.getStringArrayListExtra(EXTRA_CARD_LIST) ?: emptyList()
                totalCards = cardList.size
                startTesting()
            }
            ACTION_PAUSE -> pauseTesting()
            ACTION_RESUME -> resumeTesting()
            ACTION_CANCEL -> stopTesting()
            ACTION_RETRY -> retryLoad()
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val state = _serviceState.value
        val notification = notificationHelper.buildForegroundNotification(
            if (state.isPaused) "Testing Paused" else "Testing...",
            state.progress, state.total, state.isPaused
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(Constants.NOTIFICATION_TEST_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(Constants.NOTIFICATION_TEST_ID, notification)
        }
    }

    private fun startTesting() {
        if (totalCards == 0) return
        startForegroundNotification()
        _serviceState.value = ServiceState(total = totalCards, status = "RUNNING")
        successCount = 0
        failureCount = 0

        testJob = serviceScope.launch {
            val router = routerRepository.getRouterById(routerId) ?: run {
                _serviceState.value = _serviceState.value.copy(error = "Router not found", status = "ERROR")
                return@launch
            }
            val sessionEntity = TestSessionEntity(
                routerId = routerId, startedAt = System.currentTimeMillis(), totalCards = totalCards, isRunning = true
            )
            sessionId = sessionRepository.insertSession(sessionEntity)
            val injectionManager = InjectionManager()

            for ((index, card) in cardList.withIndex()) {
                while (_serviceState.value.isPaused && isActive) delay(100)
                if (!isActive) break

                // 1. تحميل صفحة الدخول
                pageError = false
                pageLoaded = CompletableDeferred()
                withContext(Dispatchers.Main) {
                    webView?.loadUrl("http://${router.ip}:${router.port}${router.loginPath}")
                }
                pageLoaded.await()

                if (pageError) {
                    _serviceState.value = _serviceState.value.copy(
                        status = "LOAD_ERROR", error = "فشل تحميل صفحة الدخول", currentCard = card
                    )
                    while (_serviceState.value.status == "LOAD_ERROR" && isActive) delay(200)
                    if (_serviceState.value.status == "CANCELED" || !isActive) break
                    continue
                }

                // 2. حقن البطاقة (باستخدام loadUrl لضمان التوافق)
                val js = if (router.customJs.isNullOrEmpty()) buildPlainJs(router, card)
                         else router.customJs.replace("CARD_PLACEHOLDER", card)
                jsResultReceived = CompletableDeferred()
                withContext(Dispatchers.Main) { webView?.loadUrl("javascript:$js") }

                // 3. انتظار نتيجة الحقن (حتى 60 ثانية)
                val jsResult = try {
                    withTimeout(60_000L) { jsResultReceived.await() }
                } catch (_: TimeoutCancellationException) {
                    "timeout"
                }

                // 4. تحديد النتيجة
                val (state, message) = when {
                    jsResult.startsWith("success") -> "Success" to "Success: $card"
                    jsResult.startsWith("failure") || jsResult == "no_form" || jsResult == "timeout" -> "Failure" to "Failed: $card ($jsResult)"
                    jsResult == "submitted" -> {
                        // تم الإرسال، لكن النتيجة النهائية لم تصل (حالة نادرة)
                        "Unknown" to "Submitted: $card"
                    }
                    else -> "Unknown" to "Unknown: $card ($jsResult)"
                }

                testResultRepository.insertResult(
                    TestResultEntity(
                        sessionId = sessionId, cardCode = card, routerId = routerId,
                        routerName = router.name, state = state, message = message,
                        durationMs = delayMs, testedAt = System.currentTimeMillis()
                    )
                )

                if (state == "Success") successCount++ else failureCount++
                delay(300)

                currentProgress = index + 1
                _serviceState.value = _serviceState.value.copy(
                    progress = currentProgress,
                    currentCard = card,
                    successCount = successCount,
                    failureCount = failureCount,
                    error = null
                )
                updateNotificationAndState()
            }

            finishSessionSafe()
            _serviceState.value = ServiceState(status = "FINISHED")
            notificationHelper.showResultNotification("Test Finished", "Completed $currentProgress/$totalCards cards", true)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun buildPlainJs(router: RouterProfileEntity, card: String) = """
        (function() {
            var u = document.querySelector('${router.usernameSelector}');
            var p = document.querySelector('${router.passwordSelector}');
            var s = document.querySelector('${router.submitSelector}');
            if (u && p && s) { u.value = '$card'; p.value = ''; s.click(); AndroidBridge.onResult('submitted'); }
            else AndroidBridge.onResult('fields_not_found');
        })();
    """.trimIndent()

    private suspend fun finishSessionSafe() {
        try { withContext(Dispatchers.IO) { sessionRepository.finishSession(sessionId) } } catch (_: Exception) {}
    }

    private fun pauseTesting() {
        _serviceState.value = _serviceState.value.copy(isPaused = true, status = "PAUSED")
        updateNotificationAndState()
    }

    private fun resumeTesting() {
        _serviceState.value = _serviceState.value.copy(isPaused = false, status = "RUNNING")
        updateNotificationAndState()
    }

    private fun stopTesting() {
        testJob?.cancel()
        serviceScope.launch { finishSessionSafe() }
        notificationHelper.cancelTestNotification()
        _serviceState.value = ServiceState(status = "STOPPED")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun retryLoad() {
        _serviceState.value = _serviceState.value.copy(status = "RUNNING", error = null)
    }

    private fun updateNotificationAndState() {
        val state = _serviceState.value
        val notification = notificationHelper.buildForegroundNotification(
            if (state.isPaused) "Testing Paused" else "Testing...",
            state.progress, state.total, state.isPaused
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(Constants.NOTIFICATION_TEST_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(Constants.NOTIFICATION_TEST_ID, notification)
        }
    }

    private fun startScreenshotLoop() {
        if (screenshotJob?.isActive == true) return
        screenshotJob = serviceScope.launch {
            while (isActive) {
                val bitmap = captureScreenshot()
                _serviceState.value = _serviceState.value.copy(screenshot = bitmap)
                delay(300)
            }
        }
    }

    private fun captureScreenshot(): Bitmap? {
        val wv = webView ?: return null
        return try {
            val bitmap = Bitmap.createBitmap(wv.width, wv.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            wv.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        screenshotJob?.cancel()
        serviceScope.cancel()
        webView?.destroy()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.wdmaster.app.action.START_TEST"
        const val ACTION_PAUSE = "com.wdmaster.app.action.PAUSE_TEST"
        const val ACTION_RESUME = "com.wdmaster.app.action.RESUME_TEST"
        const val ACTION_CANCEL = "com.wdmaster.app.action.CANCEL_TEST"
        const val ACTION_RETRY = "com.wdmaster.app.action.RETRY_LOAD"
        const val EXTRA_ROUTER_ID = "extra_router_id"
        const val EXTRA_DELAY_MS = "extra_delay_ms"
        const val EXTRA_CARD_LIST = "extra_card_list"
    }
}
