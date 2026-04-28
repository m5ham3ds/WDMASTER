package com.wdmaster.app.util

import java.security.MessageDigest

object MD5Utils {
    fun hash(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}