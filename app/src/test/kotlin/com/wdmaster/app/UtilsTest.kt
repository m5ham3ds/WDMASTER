package com.wdmaster.app

import com.wdmaster.app.util.ValidationUtils
import org.junit.Assert.*
import org.junit.Test

class UtilsTest {

    @Test
    fun `valid IP addresses`() {
        assertTrue(ValidationUtils.isValidIp("192.168.1.1"))
        assertTrue(ValidationUtils.isValidIp("10.0.0.1"))
    }

    @Test
    fun `invalid IP addresses`() {
        assertFalse(ValidationUtils.isValidIp("256.1.1.1"))
        assertFalse(ValidationUtils.isValidIp("abc.def.ghi.jkl"))
    }

    @Test
    fun `valid ports`() {
        assertTrue(ValidationUtils.isValidPort(80))
        assertTrue(ValidationUtils.isValidPort(8080))
    }

    @Test
    fun `invalid ports`() {
        assertFalse(ValidationUtils.isValidPort(0))
        assertFalse(ValidationUtils.isValidPort(70000))
    }
}