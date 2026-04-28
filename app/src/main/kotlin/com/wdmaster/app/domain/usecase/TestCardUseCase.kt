package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.TestResultRepository
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.domain.model.TestState
import kotlinx.coroutines.delay

class TestCardUseCase(
    private val sessionRepository: SessionRepository,
    private val testResultRepository: TestResultRepository,
    private val routerRepository: RouterRepository
) {
    data class TestOutcome(
        val result: TestResultEntity,
        val state: TestState
    )

    suspend operator fun invoke(
        cardCode: String,
        router: RouterProfileEntity,
        sessionId: Long,
        delayMs: Long = 500
    ): TestOutcome {
        val startTime = System.currentTimeMillis()

        // محاكاة خطوات الاختبار (State Machine)
        // في الواقع يتم التنفيذ عبر WebView، لكن الـ Use Case يصف المنطق فقط
        try {
            // تأخير بين البطاقات
            delay(delayMs)

            val duration = System.currentTimeMillis() - startTime
            val success = simulateTest(cardCode, router)

            val state = if (success) TestState.Success("Success: $cardCode")
            else TestState.Failure("Failed: $cardCode")

            val result = TestResultEntity(
                sessionId = sessionId,
                cardCode = cardCode,
                routerId = router.id,
                routerName = router.name,
                state = if (success) "Success" else "Failure",
                message = if (success) "Success: $cardCode" else "Failed: $cardCode",
                durationMs = duration
            )

            testResultRepository.insertResult(result)
            return TestOutcome(result, state)
        } catch (e: Exception) {
            val result = TestResultEntity(
                sessionId = sessionId,
                cardCode = cardCode,
                routerId = router.id,
                routerName = router.name,
                state = "Failure",
                message = "Failed: $cardCode - ${e.message}",
                durationMs = System.currentTimeMillis() - startTime
            )
            testResultRepository.insertResult(result)
            return TestOutcome(result, TestState.Failure("Failed: $cardCode"))
        }
    }

    private fun simulateTest(card: String, router: RouterProfileEntity): Boolean {
        // هذه محاكاة؛ المنطق الحقيقي في TestFragment + StateMachine
        return card.hashCode() % 7 == 0 // ~14% chance success for demo
    }
}