package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class ValidateTransactionAuthResponse(
    val fidoAuthenticationResponse: String,
    val fidoResponseCode: Long,
    val fidoResponseMsg: String
)
