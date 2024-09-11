package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class SubmitFailedAttemptRequest(
    val emailAddress: String?,
    val authenticationRequestId: String?,
    val errorCode: String?,
    val globalAttempt: String?,
    val attempt: String?,
    val attemptsRemaining: String?,
    val userAuthKeyId: String?,
    val lockStatus: String?,
    val score: String?
)
