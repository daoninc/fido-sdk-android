package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class CreateAccount(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val registrationRequested: Boolean,
    val language: String
    )
