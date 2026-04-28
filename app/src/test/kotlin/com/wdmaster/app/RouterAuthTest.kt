package com.wdmaster.app

import com.wdmaster.app.data.local.entity.RouterProfileEntity
import org.junit.Assert.*
import org.junit.Test

class RouterAuthTest {

    @Test
    fun `router entity creation`() {
        val router = RouterProfileEntity(
            name = "TestRouter",
            ip = "192.168.1.1",
            port = 80,
            username = "admin",
            password = "secret"
        )
        assertEquals("TestRouter", router.name)
        assertEquals(80, router.port)
    }

    @Test
    fun `default auth type`() {
        val router = RouterProfileEntity(
            name = "R", ip = "10.0.0.1", port = 443,
            username = "user", password = "pass"
        )
        assertEquals("FORM", router.authType)
    }
}