package com.wdmaster.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wdmaster.app.data.local.database.AppDatabase
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DatabaseTest {

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
    fun `insert and read router`() = runBlocking {
        val router = RouterProfileEntity(
            name = "DBTest", ip = "192.168.0.1", port = 80,
            username = "admin", password = "pass"
        )
        db.routerProfileDao().insertRouter(router)
        val all = db.routerProfileDao().getAllRouters()
        assertEquals(1, all.first().size)
    }
}