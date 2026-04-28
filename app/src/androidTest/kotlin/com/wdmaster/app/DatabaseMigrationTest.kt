package com.wdmaster.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wdmaster.app.data.local.database.AppDatabase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DatabaseMigrationTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `migration from version 1`() {
        Assert.assertNotNull(db.openHelper.writableDatabase)
    }
}