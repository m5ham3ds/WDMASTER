package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.local.entity.CardEntity
import com.wdmaster.app.data.repository.CardRepository
import kotlinx.coroutines.flow.Flow

class GenerateCardsUseCase(private val cardRepository: CardRepository) {

    sealed class Result {
        data class Success(val cards: List<CardEntity>) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(
        prefix: String,
        length: Int,
        count: Int,
        charset: String
    ): List<CardEntity> {
        val cards = mutableListOf<CardEntity>()
        val random = java.security.SecureRandom()
        val chars = charset.toCharArray()

        repeat(count) {
            val sb = StringBuilder(prefix)
            repeat(length) {
                sb.append(chars[random.nextInt(chars.size)])
            }
            val code = sb.toString()
            cards.add(
                CardEntity(
                    code = code,
                    charset = charset,
                    length = code.length
                )
            )
        }
        // نخزّنها في قاعدة البيانات عبر المستودع
        cardRepository.insertCards(cards)
        return cards
    }
}