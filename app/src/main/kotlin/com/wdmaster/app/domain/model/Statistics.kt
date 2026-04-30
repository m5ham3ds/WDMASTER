package com.wdmaster.app.domain.model

data class Statistics(
    val tested: Int = 0,
    val success: Int = 0,
    val failure: Int = 0,
    val speed: Double = 0.0,  // cards per second
    val eta: Long = 0,        // milliseconds
    val successRate: Float = 0f
)