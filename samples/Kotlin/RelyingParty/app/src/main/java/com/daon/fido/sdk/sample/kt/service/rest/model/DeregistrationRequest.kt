package com.daon.fido.sdk.sample.kt.service.rest.model

data class DeregistrationRequest(val id: String,
                                 val fidoDeregistrationRequest: String,
                                 val fidoResponseCode: Long,
                                 val fidoResponseMsg: String)
