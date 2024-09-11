package com.daon.fido.sdk.sample.kt.service.rest.model

data class AuthDetails(val id: String,
                       val authenticatorId : String,
                       val authenticatorAttestationId : String,
                       val created : String,
                       val updated : String,
                       val archived : String,
                       val status : String,
                       val deviceCorrelationId :String,
                       val appCorrelationId : String)
