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

    // يُستخدم لإكمال انتظار تحميل الصفحة
    var pageLoaded: CompletableDeferred<Unit> = CompletableDeferred()

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
                pageLoaded = CompletableDeferred()
                loadLoginPage(webView, router)
                try {
                    withTimeout(10000) { pageLoaded.await() }
                } catch (_: Exception) {
                    stateMachine.transition(TestStateMachine.State.FAILURE)
                    _testState.value = stateMachine.currentState
                    currentProgress++
                    updateNotification()
                    continue
                }

                // 2. حقن البطاقة (كتابة كود JavaScript فقط)
                stateMachine.transition(TestStateMachine.State.INJECTING_CARD)
                _testState.value = stateMachine.currentState
                val injectJs = buildInjectJs(router, card)
                webView.evaluateJavascript(injectJs, null)

                // 3. انتظار التنقل إلى صفحة النتيجة (أو بقاء نفس الصفحة)
                pageLoaded = CompletableDeferred()
                try {
                    withTimeout(10000) { pageLoaded.await() }
                } catch (_: Exception) {
                    // timeout – نعتبر الصفحة الحالية هي النتيجة
                }

                // 4. فحص النتيجة في الصفحة الحالية
                val result = checkResultInPage(webView, router)
                when (result) {
                    InjectionManager.TestResult.SUCCESS -> {
                        stateMachine.transition(TestStateMachine.State.SUCCESS)
                        patternLearningUseCase.learnFromSuccess(card, routerId, "")
                    }
                    InjectionManager.TestResult.FAILURE -> {
                        stateMachine.transition(TestStateMachine.State.FAILURE)
                    }
                    InjectionManager.TestResult.UNKNOWN -> {
                        stateMachine.transition(TestStateMachine.State.FAILURE)
                    }
                }
                _testState.value = stateMachine.currentState

                // 5. تسجيل الخروج
                injectionManager.performLogout(webView, router.logoutSelector)
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
        // كود افتراضي للحقن
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

    private fun checkResultInPage(webView: WebView, router: RouterProfileEntity): InjectionManager.TestResult {
        // استخدم evaluateJavascript بشكل متزامن داخل coroutine
        var result = InjectionManager.TestResult.UNKNOWN
        val checker = ResultChecker()
        runBlocking {
            checker.check(
                webView,
                router.successIndicator,
                router.failureIndicator
            ) { checkResult ->
                result = when (checkResult) {
                    ResultChecker.Result.Success -> InjectionManager.TestResult.SUCCESS
                    ResultChecker.Result.Failure -> InjectionManager.TestResult.FAILURE
                    else -> InjectionManager.TestResult.UNKNOWN
                }
            }
        }
        return result
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
        if (::pageLoaded.isInitialized && !pageLoaded.isCompleted) {
            pageLoaded.complete(Unit)
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
