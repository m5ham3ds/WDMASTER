package com.wdmaster.app.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var webView: WebView? = null
    private var testJob: Job? = null
    private var currentProgress = 0
    private var totalCards = 0
    private var routerId = 0L
    private var sessionId = 0L
    private var delayMs = 500L
    private var cardList = listOf<String>()

    private val _serviceState = MutableStateFlow(ServiceState())
    val serviceState: StateFlow<ServiceState> = _serviceState

    private var pageLoaded = CompletableDeferred<Unit>()
    private var pageError = false

    // أدوات التصوير الدوري
    private val mainHandler = Handler(Looper.getMainLooper())
    private var screenshotJob: Job? = null

    override fun onCreate() {
        super.onCreate()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView() {
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                allowFileAccess = false
                allowContentAccess = false
                javaScriptCanOpenWindowsAutomatically = true
            }
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
        createWebView()
        startScreenshotLoop() // بدء التقاط اللقطات
        startForegroundNotification()
        _serviceState.value = ServiceState(total = totalCards, status = "RUNNING")

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
            var successCount = 0
            var failureCount = 0

            for ((index, card) in cardList.withIndex()) {
                while (_serviceState.value.isPaused && isActive) delay(100)
                if (!isActive) break

                // تحميل صفحة الدخول (انتظار حتى تكتمل أو يحدث خطأ)
                pageError = false
                pageLoaded = CompletableDeferred()
                withContext(Dispatchers.Main) {
                    webView?.loadUrl("http://${router.ip}:${router.port}${router.loginPath}")
                }
                pageLoaded.await()

                if (pageError) {
                    _serviceState.value = _serviceState.value.copy(
                        status = "LOAD_ERROR",
                        error = "فشل تحميل صفحة الدخول",
                        currentCard = card
                    )
                    while (_serviceState.value.status == "LOAD_ERROR" && isActive) delay(200)
                    if (_serviceState.value.status == "CANCELED" || !isActive) break
                    continue
                }

                // حقن البطاقة
                val js = if (router.customJs.isNullOrEmpty()) buildPlainJs(router, card) else router.customJs.replace("CARD_PLACEHOLDER", card)
                withContext(Dispatchers.Main) { webView?.evaluateJavascript(js, null) }

                // انتظار تحميل صفحة النتيجة
                pageError = false
                pageLoaded = CompletableDeferred()
                pageLoaded.await()

                val (state, message) = if (pageError) {
                    "Connection Error" to "Connection Error: $card"
                } else {
                    val result = withContext(Dispatchers.Main) {
                        webView?.let { ResultChecker().check(it, router.successIndicator, router.failureIndicator) }
                    }
                    when (result) {
                        ResultChecker.Result.Success -> "Success" to "Success: $card"
                        ResultChecker.Result.Failure -> "Failure" to "Failed: $card"
                        else -> "Unknown" to "Unknown: $card"
                    }
                }

                testResultRepository.insertResult(
                    TestResultEntity(
                        sessionId = sessionId, cardCode = card, routerId = routerId,
                        routerName = router.name, state = state, message = message,
                        durationMs = delayMs, testedAt = System.currentTimeMillis()
                    )
                )

                if (state == "Success") successCount++ else failureCount++
                withContext(Dispatchers.Main) { injectionManager.performLogout(webView!!, null) }
                delay(500)

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
            stopScreenshotLoop()
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun buildPlainJs(router: RouterProfileEntity, card: String) = """
        (function() {
            var u = document.querySelector('${router.usernameSelector}');
            var p = document.querySelector('${router.passwordSelector}');
            var s = document.querySelector('${router.submitSelector}');
            if (u && p && s) { u.value = '$card'; p.value = ''; s.click(); }
        })();
    """.trimIndent()

    private suspend fun finishSessionSafe() = withContext(Dispatchers.IO) {
        try { sessionRepository.finishSession(sessionId) } catch (_: Exception) {}
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
        stopScreenshotLoop()
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

    // --- نظام اللقطات الدورية ---
    private fun startScreenshotLoop() {
        if (screenshotJob?.isActive == true) return
        screenshotJob = serviceScope.launch {
            while (isActive) {
                val bitmap = withContext(Dispatchers.Main) { captureScreenshot() }
                _serviceState.value = _serviceState.value.copy(screenshot = bitmap)
                delay(500) // التقاط كل نصف ثانية
            }
        }
    }

    private fun stopScreenshotLoop() {
        screenshotJob?.cancel()
        _serviceState.value = _serviceState.value.copy(screenshot = null)
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
        stopScreenshotLoop()
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
