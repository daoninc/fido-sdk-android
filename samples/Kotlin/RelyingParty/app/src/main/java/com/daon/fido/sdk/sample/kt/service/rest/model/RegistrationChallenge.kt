package com.daon.fido.sdk.sample.kt.service.rest.model

data class RegistrationChallenge (val id: String,
                                  val fidoRegistrationRequest: String,
                                  var fidoRegistrationResponse: String,
                                  val fidoResponseCode: Long,
                                  val fidoResponseMsg: String,
                                  val registrationId: String,
                                  val userId: String)


