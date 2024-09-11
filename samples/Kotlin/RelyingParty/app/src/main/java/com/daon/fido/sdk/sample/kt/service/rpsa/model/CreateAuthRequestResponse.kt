package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class CreateAuthRequestResponse(
    val fidoAuthenticationRequest: String,
    val authenticationRequestId: String
)
