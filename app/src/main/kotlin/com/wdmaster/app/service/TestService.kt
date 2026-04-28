package com.wdmaster.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.wdmaster.app.data.repository.*
import com.wdmaster.app.domain.usecase.TestCardUseCase
import com.wdmaster.app.domain.model.TestState
import com.wdmaster.app.util.Logger
import com.wdmaster.app.util.Constants

class TestService : Service(), KoinComponent {

    private val cardRepository: CardRepository by inject()
    private val routerRepository: RouterRepository by inject()
    private val testResultRepository: TestResultRepository by inject()
    private val sessionRepository: SessionRepository by inject()
    private val testCardUseCase: TestCardUseCase by inject()
    private val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }

    private val logger = Logger("TestService")
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var testJob: Job? = null
    private var isPaused = false
    private var currentProgress = 0
    private var totalCards = 0
    private var routerId = 0L
    private var sessionId = 0L
    private var delayMs = 500L
    private var cardList = listOf<String>()

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
        }
        return START_STICKY
    }

    private fun startTesting() {
        if (totalCards == 0) return
        val notification = notificationHelper.buildForegroundNotification("Testing...", currentProgress, totalCards)
        startForeground(Constants.NOTIFICATION_TEST_ID, notification)

        testJob = serviceScope.launch {
            val sessionEntity = com.wdmaster.app.data.local.entity.TestSessionEntity(
                routerId = routerId, startedAt = System.currentTimeMillis(), totalCards = totalCards, isRunning = true
            )
            sessionId = sessionRepository.insertSession(sessionEntity)

            val router = routerRepository.getRouterById(routerId)
            if (router == null) { stopSelf(); return@launch }

            for ((index, card) in cardList.withIndex()) {
                while (isPaused && isActive) delay(100)
                if (!isActive) break
                try {
                    val outcome = testCardUseCase(card, router, sessionId, delayMs)
                    currentProgress = index + 1
                    updateNotification()
                    if (outcome.state is TestState.Success) {
                        notificationHelper.showResultNotification("Card Success!", "Card $card succeeded", true)
                    }
                } catch (e: Exception) { logger.e("Test failed for card $card", e) }
            }

            finishSessionSafe()
            notificationHelper.showResultNotification("Test Finished", "Completed $currentProgress/$totalCards cards", true)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private suspend fun finishSessionSafe() = withContext(Dispatchers.IO) {
        try { sessionRepository.finishSession(sessionId) }
        catch (_: Exception) {}
    }

    private fun pauseTesting() { isPaused = true; updateNotification() }
    private fun resumeTesting() { isPaused = false; updateNotification() }
    private fun stopTesting() {
        testJob?.cancel()
        serviceScope.launch { finishSessionSafe() }
        notificationHelper.cancelTestNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val notification = notificationHelper.buildForegroundNotification(
            if (isPaused) "Testing Paused" else "Testing...", currentProgress, totalCards, isPaused
        )
        startForeground(Constants.NOTIFICATION_TEST_ID, notification)
    }

    override fun onDestroy() { serviceScope.cancel(); super.onDestroy() }

    companion object {
        const val ACTION_START = "com.wdmaster.app.action.START_TEST"
        const val ACTION_PAUSE = "com.wdmaster.app.action.PAUSE_TEST"
        const val ACTION_RESUME = "com.wdmaster.app.action.RESUME_TEST"
        const val ACTION_CANCEL = "com.wdmaster.app.action.CANCEL_TEST"
        const val EXTRA_ROUTER_ID = "extra_router_id"
        const val EXTRA_DELAY_MS = "extra_delay_ms"
        const val EXTRA_CARD_LIST = "extra_card_list"
    }
}
