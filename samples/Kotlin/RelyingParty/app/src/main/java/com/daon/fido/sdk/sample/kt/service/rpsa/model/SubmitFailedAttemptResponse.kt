package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class SubmitFailedAttemptResponse(
    val fidoAuthenticationResponse: String,
    val fidoAuthenticationRequest: String,
    val authenticationRequestId: String
)
