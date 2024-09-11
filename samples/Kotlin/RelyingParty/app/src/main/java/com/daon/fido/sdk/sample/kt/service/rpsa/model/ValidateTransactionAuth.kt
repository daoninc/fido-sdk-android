package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class ValidateTransactionAuth(
    val email: String? = null,
    val fidoAuthenticationRequest: String? = null,
    val fidoAuthenticationResponse: String,
    val authenticationRequestId: String? = null
)
