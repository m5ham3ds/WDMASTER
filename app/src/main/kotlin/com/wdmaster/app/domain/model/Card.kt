package com.wdmaster.app.domain.model

data class Card(
    val id: Long = 0,
    val code: String,
    val charset: String = "",
    val length: Int = code.length,
    val generatedAt: Long = System.currentTimeMillis()
)