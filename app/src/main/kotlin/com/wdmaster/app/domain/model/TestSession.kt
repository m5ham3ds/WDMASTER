package com.wdmaster.app.domain.model

data class TestSession(
    val id: Long = 0,
    val routerId: Long,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val totalCards: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val isRunning: Boolean = false
)