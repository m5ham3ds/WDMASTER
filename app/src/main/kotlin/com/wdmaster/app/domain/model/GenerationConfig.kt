package com.wdmaster.app.domain.model

data class GenerationConfig(
    val limit: Int = 100,
    val seed: Long = 0
)