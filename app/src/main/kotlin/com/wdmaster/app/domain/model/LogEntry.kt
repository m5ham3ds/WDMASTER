package com.wdmaster.app.domain.model

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val message: String
)

enum class LogLevel {
    DEBUG, INFO, SUCCESS, WARNING, ERROR
}