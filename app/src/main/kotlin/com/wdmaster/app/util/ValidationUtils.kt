package com.wdmaster.app.util

object ValidationUtils {

    fun isValidIp(ip: String): Boolean {
        val regex = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
        if (!regex.matches(ip)) return false
        return ip.split(".").all { it.toIntOrNull() in 0..255 }
    }

    fun isValidPort(port: Int): Boolean = port in 1..65535

    fun isValidUrl(url: String): Boolean {
        return try {
            java.net.URL(url).toURI()
            true
        } catch (e: Exception) {
            false
        }
    }
}