package com.daon.fido.sdk.sample.kt.service.rpsa.model

enum class AuthenticationMethod(private val description: String) {
    USERNAME_PASSWORD("Username and password"),
    FIDO_AUTHENTICATION("FIDO Authentication");

    override fun toString(): String {
        return description
    }

}