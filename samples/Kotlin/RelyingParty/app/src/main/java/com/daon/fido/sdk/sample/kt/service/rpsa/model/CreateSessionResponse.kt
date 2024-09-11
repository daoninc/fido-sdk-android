package com.daon.fido.sdk.sample.kt.service.rpsa.model

import java.util.*

data class CreateSessionResponse(
    val sessionId: String,
    val lastLoggedIn: Date,
    val loggedInWith: AuthenticationMethod,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fidoAuthenticationResponse: String,
    val fidoResponseCode: Long,
    val fidoResponseMsg: String
)
