package com.daon.fido.sdk.sample.kt.service.rpsa.model

import java.util.Date

data class AuthenticatorInfo(
    val id: String,
    val created: Date,
    val lastUsed: Date,
    val name: String,
    val description: String,
    val vendorName: String,
    val icon: String,
    val status: String,
    val fidoDeregistrationRequest: String,
    val aaid: String,
    val deviceCorrelationId: String,
    val presentOnDevice: Boolean
)
