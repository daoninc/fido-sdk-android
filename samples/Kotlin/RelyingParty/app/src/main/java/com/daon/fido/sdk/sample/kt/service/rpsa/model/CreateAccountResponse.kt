package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class CreateAccountResponse(
    val sessionId: String,
    val fidoRegistrationRequest: String,
    val registrationRequestId: String
)
