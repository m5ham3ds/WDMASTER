package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.local.entity.CardEntity
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.local.entity.TestSessionEntity
import com.wdmaster.app.data.repository.CardRepository
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.domain.model.TestConfig
import com.wdmaster.app.domain.model.TestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestBatchUseCase(
    private val cardRepository: CardRepository,
    private val routerRepository: RouterRepository,
    private val sessionRepository: SessionRepository,
    private val testCardUseCase: TestCardUseCase
) {
    suspend operator fun invoke(config: TestConfig): Flow<TestState> = flow {
        val router = routerRepository.getRouterById(config.routerId)
            ?: throw IllegalStateException("Router not found with id=${config.routerId}")

        // إنشاء جلسة جديدة
        val sessionId = sessionRepository.insertSession(
            TestSessionEntity(
                routerId = router.id,
                startedAt = System.currentTimeMillis(),
                totalCards = config.count,
                isRunning = true
            )
        )

        // توليد البطاقات
        val cards = cardRepository.allCards
        // في الإصدار الحقيقي نستخدم Flow، هنا نقوم بمحاكاة
        val generatedCards = GenerateCardsUseCase(cardRepository)(
            prefix = config.prefix,
            length = config.length,
            count = config.count,
            charset = config.charset
        )

        var successCount = 0
        for (card in generatedCards) {
            val outcome = testCardUseCase(card.code, router, sessionId, config.delayMs)
            emit(outcome.state)

            if (outcome.state is TestState.Success) {
                successCount++
                if (config.stopOnSuccess) break
            }
        }

        // إنهاء الجلسة
        sessionRepository.finishSession(sessionId)
        emit(TestState.Success("Batch complete: $successCount/${config.count} succeeded"))
    }
}