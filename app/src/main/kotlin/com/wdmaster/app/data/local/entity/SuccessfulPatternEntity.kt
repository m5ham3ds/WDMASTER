package com.wdmaster.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "successful_patterns")
data class SuccessfulPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "router_id")
    val routerId: Long,

    @ColumnInfo(name = "charset")
    val charset: String,

    @ColumnInfo(name = "length")
    val length: Int,

    @ColumnInfo(name = "discovered_at")
    val discoveredAt: Long = System.currentTimeMillis()
)