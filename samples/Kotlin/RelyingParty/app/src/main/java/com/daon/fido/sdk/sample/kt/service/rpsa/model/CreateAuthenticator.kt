package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class CreateAuthenticator(
    val fidoReqistrationResponse: String,
    val registrationChallengeId: String?
)
