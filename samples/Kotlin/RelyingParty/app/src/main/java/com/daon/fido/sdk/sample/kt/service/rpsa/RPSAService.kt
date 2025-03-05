package com.daon.fido.sdk.sample.kt.service.rpsa

import android.content.Context
import android.os.Bundle
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.IXUAFService
import com.daon.fido.client.sdk.Response
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.core.ErrorFactory
import com.daon.fido.client.sdk.core.SingleShotAuthenticationRequest
import com.daon.fido.client.sdk.model.Operation
import com.daon.fido.client.sdk.model.UafProtocolMessageBase
import com.daon.fido.client.sdk.uaf.UafMessageUtils
import com.daon.fido.sdk.sample.kt.service.rpsa.model.AuthenticatorInfo
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateAccount
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateAccountResponse
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateAuthRequestResponse
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateAuthenticator
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateAuthenticatorResponse
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateRegistrationRequestResponse
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateSession
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateSessionResponse
import com.daon.fido.sdk.sample.kt.service.rpsa.model.CreateTransactionAuthRequest
import com.daon.fido.sdk.sample.kt.service.rpsa.model.Error
import com.daon.fido.sdk.sample.kt.service.rpsa.model.ListAuthenticationResponse
import com.daon.fido.sdk.sample.kt.service.rpsa.model.SubmitFailedAttemptRequest
import com.daon.fido.sdk.sample.kt.service.rpsa.model.SubmitFailedAttemptResponse
import com.daon.fido.sdk.sample.kt.service.rpsa.model.ValidateTransactionAuth
import com.daon.fido.sdk.sample.kt.service.rpsa.model.ValidateTransactionAuthResponse
import com.daon.sdk.authenticator.VerificationAttemptParameters
import com.daon.sdk.crypto.log.LogUtils

import com.google.gson.Gson
import java.net.HttpURLConnection
import java.util.Locale
import java.util.UUID

/**
 * @suppress
 */
class RPSAService (private val context: Context, private val params: Bundle): IXUAFService {
    //Resources
    private val serverResourceAccount = "accounts"
    private val serverResourceRegRequests = "regRequests"
    private val serverResourceAuthenticators = "authenticators"
    private val serverResourceAuthRequests = "authRequests"
    private val serverResourceTransactionAuthRequests = "transactionAuthRequests"
    private val serverResourceTransactionAuthValidation = "transactionAuthValidation"
    private val serverResourceSessions = "sessions"
    private val serverResourceListAuthenticators = "listAuthenticators"
    private val serverResourceSubmitFailedAttempts = "failedTransactionData"

    private val TAG = RPSAService::class.simpleName ?: LogUtils.TAG

    private var http = HTTP(params)
    private var cachedRegistrationRequestId: String? = null
    private var cachedRegistrationRequest: String? = null

    override suspend fun serviceRequestAccess(params: Bundle): Response {
        LogUtils.logVerbose(context, TAG, "RPSAService serviceCreateAccount")
        val username =
            params.getString(IXUAF.USERNAME) ?: UUID.randomUUID().toString().substring(0, 15)
        val firstname = params.getString(IXUAF.FIRSTNAME) ?: "first name"
        val lastname = params.getString(IXUAF.LASTNAME) ?: "last name"
        val password = params.getString(IXUAF.PASSWORD) ?: "password"
        val registration = params.getBoolean(IXUAF.REGISTRATION_REQUESTED, true)
        val account = CreateAccount(
            firstname, lastname, username, password, registration, Locale.getDefault().toString()
        )
        val sessionId = params.getString("sessionId")
        val payload = Gson().toJson(account)
        when (val httpResponse = http.post(serverResourceAccount, payload, sessionId)) {
            is HTTP.Success -> {
                return if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val createAccountResponse =
                        Gson().fromJson(httpResponse.payload, CreateAccountResponse::class.java)
                    cachedRegistrationRequest = createAccountResponse.fidoRegistrationRequest
                    cachedRegistrationRequestId = createAccountResponse.registrationRequestId
                    val result = Bundle()
                    result.putString("sessionId", createAccountResponse.sessionId)
                    result.putString("cachedRegRequest", createAccountResponse.fidoRegistrationRequest)
                    result.putString("cachedRegRequestId", createAccountResponse.registrationRequestId)
                    Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(context, TAG, "regRequests error ${httpResponse.payload}")
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }

    }

    private fun createFailureBundle(erroCode: Int, errorMessage: String?): Bundle {
        val bundle = Bundle().apply {
            putInt(IXUAF.ERROR_CODE, erroCode)
            putString(IXUAF.ERROR_MESSAGE, errorMessage)
        }
        return bundle
    }

    override suspend fun serviceRevokeAccess(params: Bundle): Response {
        val sessionId = params.getString("sessionId")
        if (sessionId != null) {
            val httpResponse = http.deleteResource(serverResourceSessions, sessionId!!, false, sessionId)
            return when (httpResponse) {
                is HTTP.Success -> {
                    Success(Bundle())
                }

                is HTTP.Error -> {
                    Failure(createFailureBundle(httpResponse.code, httpResponse.message))
                }
            }
        } else {
            return Failure(
                createFailureBundle(
                    ErrorFactory.UNEXPECTED_ERROR_CODE, "SessionId is null"
                )
            )
        }
    }

    override suspend fun serviceDeleteUser(params: Bundle): Response {
        val sessionId = params.getString("sessionId")
        if (sessionId != null) {
            val httpResponse = http.deleteResource(serverResourceAccount, sessionId!!, true, sessionId)
            return when (httpResponse) {
                is HTTP.Success -> {
                    Success(Bundle())
                }

                is HTTP.Error -> {
                    Failure(createFailureBundle(httpResponse.code, httpResponse.message))
                }
            }
        } else {
            return Failure(
                createFailureBundle(
                    ErrorFactory.UNEXPECTED_ERROR_CODE, "SessionId is null"
                )
            )
        }
    }

    override suspend fun serviceRequestRegistration(inParams: Bundle): Response {
        if (cachedRegistrationRequest != null) {
            val result = Bundle()
            result.putString(IXUAF.REG_REQUEST, cachedRegistrationRequest)
            result.putString(IXUAF.REQUEST_ID, cachedRegistrationRequestId)
            cachedRegistrationRequest = null
            cachedRegistrationRequestId = null
            return Success(result)
        } else {
            val sessionId = inParams.getString("sessionId")
            when (val httpResponse = http.get(serverResourceRegRequests, sessionId)) {
                is HTTP.Success -> {
                    if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                        LogUtils.logVerbose(context, TAG, "serviceRequestRegistration success OK")
                        val regResponse = Gson().fromJson(
                            httpResponse.payload, CreateRegistrationRequestResponse::class.java
                        )
                        val result = Bundle()
                        result.putString(IXUAF.REG_REQUEST, regResponse.fidoRegistrationRequest)
                        result.putString(IXUAF.REQUEST_ID, regResponse.registrationRequestId)
                        return Success(result)
                    } else {
                        val error: Error = try {
                            LogUtils.logError(
                                context, TAG, "regRequests error ${httpResponse.payload}"
                            )
                            Gson().fromJson(httpResponse.payload, Error::class.java)
                        } catch (e: Exception) {
                            Error(code = -4, message = "Server communication error")
                        }
                        return Failure(createFailureBundle(error.code, error.message))
                    }
                }

                is HTTP.Error -> {
                    LogUtils.logError(context, TAG, "serviceRequestRegistration HTTP.Error")
                    return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
                }
            }
        }
    }

    override suspend fun serviceRegister(params: Bundle): Response {
        LogUtils.logVerbose(context, TAG, "serviceRegister")
        val fidoRegistrationResponse: String =
            params.getString(IXUAF.REG_RESPONSE) ?: params.getString("serverData")!!
        val registrationChallengeId = params.getString(IXUAF.REQUEST_ID)!!
        val createAuthenticator =
            CreateAuthenticator(fidoRegistrationResponse, registrationChallengeId)
        val sessionId = params.getString("sessionId")
        val payload = Gson().toJson(createAuthenticator)
        when (val httpResponse = http.post(serverResourceAuthenticators, payload, sessionId)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    LogUtils.logVerbose(context, TAG, "serviceRegister Success OK")
                    val createAuthenticatorResponse = Gson().fromJson(
                        httpResponse.payload, CreateAuthenticatorResponse::class.java
                    )
                    val result = Bundle()
                    result.putString(
                        IXUAF.REG_CONFIRMATION,
                        createAuthenticatorResponse.fidoRegistrationConfirmation
                    )
                    result.putShort(
                        IXUAF.RESPONSE_CODE, createAuthenticatorResponse.fidoResponseCode.toShort()
                    )
                    LogUtils.logVerbose(
                        context,
                        TAG,
                        "serviceRegister responseCode : ${createAuthenticatorResponse.fidoResponseCode.toShort()}"
                    )
                    result.putString(
                        IXUAF.RESPONSE_MESSAGE, createAuthenticatorResponse.fidoResponseMsg
                    )
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logVerbose(
                            context, TAG, "regRequests error ${httpResponse.payload}"
                        )
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                LogUtils.logVerbose(context, TAG, "serviceRegister HTTP.Error")
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }

    }

    override suspend fun serviceRequestAuthentication(params: Bundle): Response {
        LogUtils.logVerbose(context, TAG, "serviceRegister")
        val sessionId = params.getString("sessionId")
        val transactionId = params.getString(IXUAF.ID)
        val username = params.getString(IXUAF.USERNAME)
        val singleshot = params.getBoolean(IXUAF.SINGLE_SHOT)
        if (transactionId != null) {
            //Push
            return getAuthRequest("$serverResourceAuthRequests/$transactionId")
        } else {
            return if (sessionId != null) {
                if (singleshot) {
                    try {
                        val appId = params.getString(IXUAF.APP_ID)
                        val ssar =
                            SingleShotAuthenticationRequest.createUserAuthWithAllRegisteredAuthenticators(
                                context, appId, username
                            )
                        ssar.addExtension("com.daon.face.ados.mode", "verify")
                        ssar.addExtension("com.daon.face.retriesRemaining", "5")
                        ssar.addExtension("com.daon.face.liveness.passive.type", "server")
                        ssar.addExtension("com.daon.passcode.type", "ALPHANUMERIC")
                        ssar.addExtension("com.daon.face.liveness.active.type", "none")
                        //Add the decChain extension value here
                        //ssar.addExtension("com.daon.sdk.ados.decChain", decChain)
                        val result = Bundle()
                        result.putString(
                            IXUAF.AUTH_REQUEST, ssar.toString()
                        )
                        return Success(result)
                    }catch (e: Exception) {
                        LogUtils.logError(context, TAG, "serviceRequestAuthentication error ${e.message}")
                        return Failure(createFailureBundle(-4, e.message))
                    }
                } else {
                    //step up
                    getTransactionAuthRequest(params)
                }
            } else {
                // Login with username for SRP
                if (username != null) {
                    getAuthRequest("$serverResourceAuthRequests?userId=$username")
                } else {
                    getAuthRequest(serverResourceAuthRequests)
                }
            }
        }
    }

    private fun getTransactionAuthRequest(params: Bundle): Response {
        val createTransactionAuthRequest: CreateTransactionAuthRequest
        val confirmationOTP = params.getBoolean(IXUAF.CONFIRMATION_OTP)
        createTransactionAuthRequest = if (params.containsKey(IXUAF.TRANSACTION_CONTENT_TYPE)) {
            CreateTransactionAuthRequest(
                transactionContentType = params.getString(IXUAF.TRANSACTION_CONTENT_TYPE),
                transactionContent = params.getString(IXUAF.TRANSACTION_CONTENT_DATA),
                otpEnabled = confirmationOTP
            )
        } else {
            CreateTransactionAuthRequest(otpEnabled = confirmationOTP)
        }

        val sessionId = params.getString("sessionId")
        val payload = Gson().toJson(createTransactionAuthRequest)
        when (val httpResponse = http.post(serverResourceTransactionAuthRequests, payload, sessionId)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val authResponse = Gson().fromJson(
                        httpResponse.payload, CreateAuthRequestResponse::class.java
                    )
                    val result = Bundle()
                    result.putString(
                        IXUAF.AUTH_REQUEST, authResponse.fidoAuthenticationRequest
                    )
                    result.putString(IXUAF.REQUEST_ID, authResponse.authenticationRequestId)
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(
                            context, TAG, "getTransactionAuthRequest error ${httpResponse.payload}"
                        )
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                LogUtils.logVerbose(context, TAG, "getTransactionAuthRequest HTTP.Error")
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }
    }

    private fun getAuthRequest(relativeUrl: String): Response {
        //sessionId is null for login
        when (val httpResponse = http.get(relativeUrl, null)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val authResponse = Gson().fromJson(
                        httpResponse.payload, CreateAuthRequestResponse::class.java
                    )
                    val result = Bundle()
                    result.putString(
                        IXUAF.AUTH_REQUEST, authResponse.fidoAuthenticationRequest
                    )
                    result.putString(IXUAF.REQUEST_ID, authResponse.authenticationRequestId)
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(
                            context, TAG, "getAuthRequest error ${httpResponse.payload}"
                        )
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                LogUtils.logError(context, TAG, "getAuthRequest HTTP.Error")
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }
    }

    override suspend fun serviceAuthenticate(params: Bundle): Response {
        LogUtils.logVerbose(context, TAG, "serviceAuthenticate")
        val sessionId = params.getString("sessionId")
        return if (sessionId == null) {
            LogUtils.logVerbose(context, TAG, "serviceAuthenticate createSession")
            createSession(params)
        } else {
            LogUtils.logVerbose(context, TAG, "serviceAuthenticate verify")
            verify(params)
        }
    }

    private fun createSession(params: Bundle): Response {
        LogUtils.logVerbose(context, TAG, "createSession")
        val fidoAuthResponse: String =
            params.getString(IXUAF.AUTH_RESPONSE) ?: params.getString(IXUAF.SERVER_DATA)!!
        val createSession = CreateSession(
            fidoAuthenticationResponse = fidoAuthResponse, authenticationRequestId = params.getString(
                IXUAF.REQUEST_ID
            )!!
        )
        val payload = Gson().toJson(createSession)
        //sessionId is null for login
        when (val httpResponse = http.post(serverResourceSessions, payload, null)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val authResponse = Gson().fromJson(
                        httpResponse.payload, CreateSessionResponse::class.java
                    )
                    val result = Bundle()
                    result.putString(
                        IXUAF.AUTH_CONFIRMATION, authResponse.fidoAuthenticationResponse
                    )
                    result.putShort(IXUAF.RESPONSE_CODE, authResponse.fidoResponseCode.toShort())
                    result.putString(IXUAF.RESPONSE_MESSAGE, authResponse.fidoResponseMsg)
                    result.putString(IXUAF.LAST_LOGIN, authResponse.lastLoggedIn.toString())
                    result.putString(IXUAF.LOGGEDIN_WITH, authResponse.loggedInWith.toString())
                    result.putString(IXUAF.EMAIL, authResponse.email)
                    result.putString("sessionId", authResponse.sessionId)
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(
                            context, TAG, "createSession error ${httpResponse.payload}"
                        )
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    LogUtils.logError(context, TAG, "createSession Failure")
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                LogUtils.logError(context, TAG, "createSession HTTP.Error")
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }
    }

    private fun verify(params: Bundle): Response {
        var validateTransactionAuth: ValidateTransactionAuth? = null
        val authResponse: String =
            params.getString(IXUAF.AUTH_RESPONSE) ?: params.getString(IXUAF.SERVER_DATA)!!
        var authRequest: String? = params.getString(IXUAF.AUTH_REQUEST)
        val username: String? = params.getString(IXUAF.USERNAME)
        val authRequestId: String? = params.getString(IXUAF.REQUEST_ID)

        if (authRequestId != null) {
            validateTransactionAuth = ValidateTransactionAuth(
                fidoAuthenticationResponse = authResponse, authenticationRequestId = authRequestId
            )
        } else if (authRequest != null) {
            validateTransactionAuth = ValidateTransactionAuth(
                email = username,
                fidoAuthenticationRequest = authRequest,
                fidoAuthenticationResponse = authResponse
            )
        }

        val sessionId = params.getString("sessionId")
        val payload = Gson().toJson(validateTransactionAuth)
        when (val httpResponse = http.post(serverResourceTransactionAuthValidation, payload, sessionId)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val validateTransactionAuthResponse = Gson().fromJson(
                        httpResponse.payload, ValidateTransactionAuthResponse::class.java
                    )

                    val result = Bundle()
                    result.putString(
                        IXUAF.AUTH_CONFIRMATION,
                        validateTransactionAuthResponse.fidoAuthenticationResponse
                    )
                    result.putShort(
                        IXUAF.RESPONSE_CODE,
                        validateTransactionAuthResponse.fidoResponseCode.toShort()
                    )
                    result.putString(
                        IXUAF.RESPONSE_MESSAGE, validateTransactionAuthResponse.fidoResponseMsg
                    )
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(context, TAG, "verify error ${httpResponse.payload}")
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                LogUtils.logVerbose(context, TAG, "verify HTTP.Error")
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }
    }

    override suspend fun serviceRequestDeregistration(params: Bundle): Response {
        val sessionId = params.getString("sessionId")
        val aaid = params.getString(IXUAF.AAID)!!
        when (val httpResponse = http.get(serverResourceListAuthenticators, sessionId)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val listAuthenticatorsResponse = Gson().fromJson(
                        httpResponse.payload, ListAuthenticationResponse::class.java
                    )
                    val authenticatorInfo = listAuthenticatorsResponse.authenticatorInfoList
                    return when (val deregRequest =
                        getDeregistrationRequest(aaid, authenticatorInfo, sessionId)) {
                        is Success -> {
                            Success(deregRequest.params)
                        }

                        is Failure -> {
                            Failure(
                                createFailureBundle(
                                    deregRequest.params.getInt(IXUAF.ERROR_CODE),
                                    deregRequest.params.getString(IXUAF.ERROR_MESSAGE)
                                )
                            )
                        }
                    }

                } else {
                    val error: Error = try {
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }

    }

    private fun getDeregistrationRequest(
        aaid: String, authenticatorInfo: Array<AuthenticatorInfo>, sessionId: String?
    ): Response {
        var found = false
        var authInfo: AuthenticatorInfo? = null
        for (auth in authenticatorInfo) {
            if (auth.aaid == aaid && auth.status == "ACTIVE") {
                found = true
                authInfo = auth
                break
            }
        }
        if (found && authInfo != null) {
            val httpResponse = http.deleteResource(serverResourceAuthenticators, authInfo.id, true, sessionId)
            when (httpResponse) {
                is HTTP.Success -> {
                    if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                        val params = Bundle()
                        params.putString(IXUAF.DEREG_REQUEST, httpResponse.payload)
                        return Success(params)
                    } else {
                        val error: Error = try {
                            LogUtils.logError(
                                context,
                                TAG,
                                "getDeregistrationRequest1 error ${httpResponse.payload}"
                            )
                            Gson().fromJson(httpResponse.payload, Error::class.java)
                        } catch (e: Exception) {
                            Error(code = -4, message = "Server communication error")
                        }
                        return Failure(createFailureBundle(error.code, error.message))
                    }
                }

                is HTTP.Error -> {
                    return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
                }
            }

        } else {
            val error = Error(code = -4, message = "Server communication error")
            return Failure(createFailureBundle(error.code, error.message))
        }
    }

    override suspend fun serviceUpdate(params: Bundle): Response {
        val response = params.getString(IXUAF.SERVER_DATA)
        val uafRequests: Array<UafProtocolMessageBase> = UafMessageUtils.validateUafMessage(
            context, response, UafMessageUtils.OpDirection.Response, null
        )
        return if (uafRequests[0].header.op == Operation.Reg) {
            serviceRegister(params)
        } else {
            serviceAuthenticate(params)
        }
    }


    override suspend fun serviceUpdateAttempt(params: Bundle): Response {
        val submitFailedAttemptRequest = SubmitFailedAttemptRequest(
            emailAddress = params.getString(VerificationAttemptParameters.PARAM_USER_ACCOUNT, null),
            attempt = params.getInt(VerificationAttemptParameters.PARAM_ATTEMPT, Integer.MIN_VALUE)
                .toString(),
            attemptsRemaining = params.getInt(
                VerificationAttemptParameters.PARAM_ATTEMPTS_REMAINING, Integer.MIN_VALUE
            ).toString(),
            globalAttempt = params.getInt(
                VerificationAttemptParameters.PARAM_GLOBAL_ATTEMPT, Integer.MIN_VALUE
            ).toString(),
            lockStatus = params.getString(VerificationAttemptParameters.PARAM_LOCK_STATUS, null),
            errorCode = params.getInt(VerificationAttemptParameters.PARAM_ERROR_CODE, 0).toString(),
            score = params.getDouble(VerificationAttemptParameters.PARAM_SCORE, 0.0).toString(),
            userAuthKeyId = params.getString(
                VerificationAttemptParameters.PARAM_USER_AUTH_KEY_ID, null
            ),
            authenticationRequestId = params.getString(IXUAF.REQUEST_ID, null)
        )
        val sessionId = params.getString("sessionId")
        val payload = Gson().toJson(submitFailedAttemptRequest)
        when (val httpResponse = http.post(serverResourceSubmitFailedAttempts, payload, sessionId)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val submitFailedAttemptResponse = Gson().fromJson(
                        httpResponse.payload, SubmitFailedAttemptResponse::class.java
                    )
                    val result = Bundle()
                    result.putString(
                        IXUAF.RESPONSE, submitFailedAttemptResponse.fidoAuthenticationResponse
                    )
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(
                            context, TAG, "serviceUpdateAttempt error ${httpResponse.payload}"
                        )
                        Gson().fromJson(httpResponse.payload, Error::class.java)
                    } catch (e: Exception) {
                        Error(code = -4, message = "Server communication error")
                    }
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is HTTP.Error -> {
                LogUtils.logError(context, TAG, "verify HTTP.Error : ${httpResponse.code}")
                return Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }
        }
    }
}