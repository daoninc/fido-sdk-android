package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class CreateSession(
    val email: String? = null,
    val password: String? = null,
    val fidoAuthenticationResponse: String,
    val authenticationRequestId: String?)
