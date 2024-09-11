package com.daon.fido.sdk.sample.kt.service.rpsa.model

data class Error(
    val code: Int,
    val message: String,
    val fidoMessage: String?,
    val fidoResponseCode: String?,
    val fidoResponseMsg: String?
) {
    constructor(code: Int, message: String): this(code, message, null, null, null)
}

