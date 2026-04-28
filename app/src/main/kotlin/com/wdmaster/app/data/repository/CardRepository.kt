package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.database.CardDao
import com.wdmaster.app.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

class CardRepository(private val cardDao: CardDao) {

    val allCards: Flow<List<CardEntity>> = cardDao.getAllCards()

    fun searchCards(query: String): Flow<List<CardEntity>> = cardDao.searchCards(query)

    suspend fun insertCard(card: CardEntity) = cardDao.insertCard(card)

    suspend fun insertCards(cards: List<CardEntity>) = cardDao.insertCards(cards)

    suspend fun updateCard(card: CardEntity) = cardDao.updateCard(card)

    suspend fun deleteCard(card: CardEntity) = cardDao.deleteCard(card)

    suspend fun deleteAll() = cardDao.deleteAllCards()
}