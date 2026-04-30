package com.wdmaster.app.domain.model

data class TestConfig(
    val routerId: Long = 0,
    val prefix: String = "",
    val length: Int = 8,
    val count: Int = 50,
    val delayMs: Long = 500L,
    val retry: Int = 3,
    val charset: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
    val skipTested: Boolean = true,
    val stopOnSuccess: Boolean = false
)