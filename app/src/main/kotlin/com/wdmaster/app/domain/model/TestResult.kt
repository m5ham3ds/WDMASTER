package com.wdmaster.app.domain.model

data class TestResult(
    val id: Long = 0,
    val sessionId: Long,
    val cardCode: String,
    val routerId: Long,
    val routerName: String = "",
    val state: TestState,
    val message: String = "",
    val durationMs: Long = 0,
    val testedAt: Long = System.currentTimeMillis()
)