package com.wdmaster.app.presentation.test

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.domain.usecase.*
import com.wdmaster.app.presentation.common.BaseViewModel
import com.wdmaster.app.service.NotificationHelper
import com.wdmaster.app.util.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TestViewModel(
    private val testCardUseCase: TestCardUseCase,
    private val testBatchUseCase: TestBatchUseCase,
    private val manageRoutersUseCase: ManageRoutersUseCase,
    private val patternLearningUseCase: PatternLearningUseCase,
    private val app: Application
) : BaseViewModel() {

    override val logger: Logger = Logger("TestViewModel")

    private val notificationHelper = NotificationHelper(app)

    private val _testState = MutableStateFlow<TestStateMachine.State>(TestStateMachine.State.IDLE)
    val testState: StateFlow<TestStateMachine.State> = _testState.asStateFlow()

    sealed class UiEvent {
        data class ShowRetryDialog(val message: String) : UiEvent()
        object ShowCancelDialog : UiEvent()
        object NavigateBack : UiEvent()
    }
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var testJob: Job? = null
    private val stateMachine = TestStateMachine()
    private var lastRouter: RouterProfileEntity? = null
    private var lastCardList = listOf<String>()
    private var lastDelayMs = 500L
    private var webViewRef: WebView? = null

    private var totalCards = 0
    private var currentProgress = 0
    var isPaused = false
        private set

    // يُعاد إنشاؤه في كل دورة تحميل
    private var _pageLoaded: CompletableDeferred<Unit>? = null
    val pageLoaded: CompletableDeferred<Unit>
        get() {
            if (_pageLoaded == null) _pageLoaded = CompletableDeferred()
            return _pageLoaded!!
        }

    fun attachWebView(webView: WebView) {
        webViewRef = webView
        webView.webViewClient = RouterWebViewClient(this)
        webView.webChromeClient = RouterChromeClient(this)
    }

    fun startTest(routerId: Long, cardList: List<String>, delayMs: Long, webView: WebView?) {
        if (webView == null) return
        attachWebView(webView)
        testJob?.cancel()
        testJob = viewModelScope.launch(Dispatchers.Main) {
            val router = manageRoutersUseCase.getRouterById(routerId)
            if (router == null) {
                _testState.value = TestStateMachine.State.FAILURE
                return@launch
            }
            lastRouter = router
            lastCardList = cardList
            lastDelayMs = delayMs
            totalCards = cardList.size
            currentProgress = 0
            isPaused = false

            updateNotification()

            val injectionManager = InjectionManager()

            for (card in cardList) {
                if (!isActive) {
                    notificationHelper.cancelTestNotification()
                    break
                }

                while (isPaused && isActive) delay(200)
                if (!isActive) break

                delay(delayMs)
                stateMachine.transition(TestStateMachine.State.LOADING_PAGE)
                _testState.value = stateMachine.currentState

                // 1. تحميل صفحة الدخول وانتظارها
                _pageLoaded = CompletableDeferred()
                loadLoginPage(webView, router)
                try {
                    withTimeout(10000) { _pageLoaded?.await() }
                } catch (_: Exception) {
                    stateMachine.transition(TestStateMachine.State.FAILURE)
                    _testState.value = stateMachine.currentState
                    currentProgress++
                    updateNotification()
                    continue
                }

                // 2. حقن البطاقة
                stateMachine.transition(TestStateMachine.State.INJECTING_CARD)
                _testState.value = stateMachine.currentState
                val injectJs = buildInjectJs(router, card)
                webView.evaluateJavascript(injectJs, null)

                // 3. انتظار التنقل إلى صفحة النتيجة
                _pageLoaded = CompletableDeferred()
                try {
                    withTimeout(10000) { _pageLoaded?.await() }
                } catch (_: Exception) {
                    // timeout – نستمر ونفحص الصفحة الحالية
                }

                // 4. فحص النتيجة
                val resultChecker = ResultChecker()
                val result = resultChecker.check(
                    webView,
                    router.successIndicator,
                    router.failureIndicator
                )
                when (result) {
                    ResultChecker.Result.Success -> {
                        stateMachine.transition(TestStateMachine.State.SUCCESS)
                        patternLearningUseCase.learnFromSuccess(card, routerId, "")
                    }
                    ResultChecker.Result.Failure -> {
                        stateMachine.transition(TestStateMachine.State.FAILURE)
                    }
                    ResultChecker.Result.Unknown -> {
                        stateMachine.transition(TestStateMachine.State.FAILURE)
                    }
                }
                _testState.value = stateMachine.currentState

                // 5. تسجيل الخروج (باستخدام null لتفعيل السلوك الافتراضي)
                injectionManager.performLogout(webView, null)
                delay(500)

                currentProgress++
                updateNotification()
            }

            stateMachine.transition(TestStateMachine.State.IDLE)
            _testState.value = stateMachine.currentState
            notificationHelper.cancelTestNotification()
        }
    }

    private fun buildInjectJs(router: RouterProfileEntity, card: String): String {
        val customJs = router.customJs ?: ""
        if (customJs.isNotEmpty()) {
            return customJs.replace("CARD_PLACEHOLDER", card)
        }
        return """
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

    private fun updateNotification() {
        notificationHelper.showTestProgressNotification(
            "اختبار البطاقات", currentProgress, totalCards, isPaused
        )
    }

    fun pauseTest() {
        isPaused = true
        updateNotification()
    }

    fun resumeTest() {
        isPaused = false
        updateNotification()
    }

    fun cancelTest() {
        testJob?.cancel()
        stateMachine.transition(TestStateMachine.State.IDLE)
        _testState.value = stateMachine.currentState
        notificationHelper.cancelTestNotification()
    }

    private fun loadLoginPage(webView: WebView, router: RouterProfileEntity) {
        val url = "http://${router.ip}:${router.port}${router.loginPath}"
        webView.loadUrl(url)
    }

    fun retryTest() {
        lastRouter?.let { router -> startTest(router.id, lastCardList, lastDelayMs, webViewRef) }
    }

    fun requestCancelTest() {
        viewModelScope.launch { _uiEvent.emit(UiEvent.ShowCancelDialog) }
    }

    fun isTestRunning(): Boolean {
        return stateMachine.currentState != TestStateMachine.State.IDLE &&
               stateMachine.currentState != TestStateMachine.State.SUCCESS &&
               stateMachine.currentState != TestStateMachine.State.FAILURE
    }

    fun onPageStarted() {}
    fun onPageFinished() {
        // إكمال pageLoaded عند تحميل أي صفحة جديدة
        _pageLoaded?.let {
            if (!it.isCompleted) it.complete(Unit)
        }
        if (stateMachine.currentState == TestStateMachine.State.LOADING_PAGE) {
            stateMachine.transition(TestStateMachine.State.WAITING_DOM)
            _testState.value = stateMachine.currentState
        }
    }

    fun onReceivedError(errorCode: Int, description: String, failingUrl: String?) {
        viewModelScope.launch {
            stateMachine.transition(TestStateMachine.State.FAILURE)
            _testState.value = stateMachine.currentState
            _uiEvent.emit(UiEvent.ShowRetryDialog("Error $errorCode: $description"))
        }
    }

    fun onJsResult(result: String) { }
    fun onJsError(error: String) { }
}
