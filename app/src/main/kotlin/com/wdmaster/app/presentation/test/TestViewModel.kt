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

    val pageLoaded = CompletableDeferred<Unit>()

    fun startTest(routerId: Long, cardList: List<String>, delayMs: Long, webView: WebView?) {
        if (webView == null) return
        webViewRef = webView
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

                loadLoginPage(webView, router)

                try {
                    withTimeout(10000) { pageLoaded.await() }
                    pageLoaded.reset()
                } catch (_: Exception) {
                    stateMachine.transition(TestStateMachine.State.FAILURE)
                    _testState.value = stateMachine.currentState
                    currentProgress++
                    updateNotification()
                    continue
                }

                val result = injectionManager.injectAndCheck(webView, router, card, router.customJs ?: "")
                when (result) {
                    InjectionManager.TestResult.SUCCESS -> {
                        stateMachine.transition(TestStateMachine.State.SUCCESS)
                        patternLearningUseCase.learnFromSuccess(card, routerId, "")
                    }
                    InjectionManager.TestResult.FAILURE -> {
                        stateMachine.transition(TestStateMachine.State.FAILURE)
                    }
                }
                _testState.value = stateMachine.currentState

                injectionManager.performLogout(webView, router.logoutSelector ?: "button[type=submit]")
                delay(500)

                currentProgress++
                updateNotification()
            }

            stateMachine.transition(TestStateMachine.State.IDLE)
            _testState.value = stateMachine.currentState
            notificationHelper.cancelTestNotification()
        }
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
        if (!pageLoaded.isCompleted) pageLoaded.complete(Unit)
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