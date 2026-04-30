package com.wdmaster.app.presentation.home

import com.wdmaster.app.domain.model.Statistics

object StatisticsHelper {
    fun formatSpeed(cardsPerSecond: Double): String = String.format("%.1f c/s", cardsPerSecond)
    fun formatEta(etaMs: Long): String {
        if (etaMs <= 0) return "--:--"
        val seconds = etaMs / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    fun formatSuccessRate(rate: Float): String = String.format("%.1f%%", rate * 100)
}