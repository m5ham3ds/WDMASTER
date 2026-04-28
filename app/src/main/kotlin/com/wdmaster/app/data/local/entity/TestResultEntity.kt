package com.wdmaster.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_results",
    foreignKeys = [
        ForeignKey(
            entity = TestSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"])]
)
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "card_code")
    val cardCode: String,

    @ColumnInfo(name = "router_id")
    val routerId: Long,

    @ColumnInfo(name = "router_name")
    val routerName: String = "",

    @ColumnInfo(name = "state")
    val state: String,   // اسم الحالة من TestState

    @ColumnInfo(name = "message")
    val message: String = "",

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0,

    @ColumnInfo(name = "tested_at")
    val testedAt: Long = System.currentTimeMillis()
)