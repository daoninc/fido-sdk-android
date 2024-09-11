package com.daon.fido.sdk.sample.kt.service.rest.model

data class AuthenticationRequest (var id: String?,
                                  var description: String,
                                  var created: String,
                                  var processed: String,
                                  var expiration: String,
                                  var transactionDescription: String,
                                  var status: String,
                                  var fidoAuthenticationRequest: String,
                                  var fidoAuthenticationResponse: String,
                                  var fidoResponseCode: Long,
                                  var fidoResponseMsg: String,
                                  var userId: String,
                                  var transactionContentType: String,
                                  var textTransactionContent: String,
                                  var imageTransactionContent: String)

