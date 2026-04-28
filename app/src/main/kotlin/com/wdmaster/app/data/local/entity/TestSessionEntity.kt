package com.wdmaster.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_sessions",
    foreignKeys = [
        ForeignKey(
            entity = RouterProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["router_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["router_id"])]
)
data class TestSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "router_id")
    val routerId: Long,

    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "finished_at")
    val finishedAt: Long? = null,

    @ColumnInfo(name = "total_cards")
    val totalCards: Int = 0,

    @ColumnInfo(name = "success_count")
    val successCount: Int = 0,

    @ColumnInfo(name = "failure_count")
    val failureCount: Int = 0,

    @ColumnInfo(name = "is_running")
    val isRunning: Boolean = false
)