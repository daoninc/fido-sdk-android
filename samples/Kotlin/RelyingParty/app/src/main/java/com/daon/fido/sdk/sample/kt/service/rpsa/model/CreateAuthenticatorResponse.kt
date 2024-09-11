package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class CreateAuthenticatorResponse(
    val fidoRegistrationConfirmation: String,
    val fidoResponseCode: Long,
    val fidoResponseMsg: String
)
