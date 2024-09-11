package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class CreateTransactionAuthRequest(
    val transactionContentType: String? = null,
    val transactionContent: String? = null,
    val stepUpAuth: Boolean = true,
    val otpEnabled: Boolean = false
)
