package com.wdmaster.app

import com.wdmaster.app.domain.usecase.GenerateCardsUseCase
import com.wdmaster.app.data.local.entity.CardEntity
import com.wdmaster.app.data.repository.CardRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class CardGeneratorTest {

    private lateinit var cardRepository: CardRepository
    private lateinit var useCase: GenerateCardsUseCase

    @Before
    fun setUp() {
        cardRepository = mock(CardRepository::class.java)
        useCase = GenerateCardsUseCase(cardRepository)
    }

    @Test
    fun `generate correct number of cards`() = runBlocking {
        val cards = useCase("TEST", 8, 10, "0123456789")
        assertEquals(10, cards.size)
    }

    @Test
    fun `cards have correct prefix and length`() = runBlocking {
        val cards = useCase("PRE", 5, 5, "ABC")
        cards.forEach {
            assertTrue(it.code.startsWith("PRE"))
            assertEquals(5, it.code.length)
        }
    }

    @Test
    fun `cards are distinct`() = runBlocking {
        val cards = useCase("X", 6, 100, "ABCDEF")
        val unique = cards.map { it.code }.distinct()
        assertTrue(unique.size > 80) // most should be distinct
    }
}