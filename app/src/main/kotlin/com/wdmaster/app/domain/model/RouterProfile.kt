package com.wdmaster.app.domain.model

data class RouterProfile(
    val id: Long = 0,
    val name: String,
    val ip: String,
    val port: Int = 80,
    val protocol: String = "http",
    val username: String = "admin",
    val password: String = "",
    val loginPath: String = "/login",
    val usernameSelector: String = "input[name=username]",
    val passwordSelector: String = "input[name=password]",
    val submitSelector: String = "button[type=submit]",
    val successIndicator: String = "status=ok",
    val failureIndicator: String = "error=",
    val customJs: String? = null,
    val authType: RouterAuthType = RouterAuthType.FORM,
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)