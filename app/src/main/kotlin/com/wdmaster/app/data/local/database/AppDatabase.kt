package com.wdmaster.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wdmaster.app.data.local.entity.CardEntity
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.local.entity.SuccessfulPatternEntity
import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.data.local.entity.TestSessionEntity

@Database(
    entities = [
        CardEntity::class,
        RouterProfileEntity::class,
        TestResultEntity::class,
        SuccessfulPatternEntity::class,
        TestSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao
    abstract fun routerProfileDao(): RouterProfileDao
    abstract fun testResultDao(): TestResultDao
    abstract fun patternDao(): PatternDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "wificardmaster.db"
    }
}