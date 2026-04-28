package com.wdmaster.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    indices = [Index(value = ["code"], unique = true)]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "charset")
    val charset: String = "",

    @ColumnInfo(name = "length")
    val length: Int = code.length,

    @ColumnInfo(name = "generated_at")
    val generatedAt: Long = System.currentTimeMillis()
)