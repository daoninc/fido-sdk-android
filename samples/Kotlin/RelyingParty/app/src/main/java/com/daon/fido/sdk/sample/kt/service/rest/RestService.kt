package com.daon.fido.sdk.sample.kt.service.rest

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
import com.daon.fido.sdk.sample.kt.service.rest.model.AuthDetails
import com.daon.fido.sdk.sample.kt.service.rest.model.AuthenticationRequest
import com.daon.fido.sdk.sample.kt.service.rest.model.DeregistrationRequest
import com.daon.fido.sdk.sample.kt.service.rest.model.RegistrationChallenge
import com.daon.fido.sdk.sample.kt.service.rest.model.Error
import com.daon.fido.sdk.sample.kt.service.rest.model.User
import com.daon.sdk.authenticator.VerificationAttemptParameters
import com.daon.sdk.crypto.log.LogUtils
import com.google.gson.Gson
import org.json.JSONObject
import java.net.HttpURLConnection

/**
 * @suppress
 */
class RestService (private val context: Context, private val params: Bundle): IXUAFService {
    private val USERS = "users"
    private val AUTHENTICATORS = "authenticators"
    private val AUTHENTICATIONREQUESTS = "authenticationRequests"
    private val REGISTRATIONCHALLENGES = "registrationChallenges"
    private val AUTHKEYID = "authKeyId"
    private val ERRORCODE = "errorCode"
    private val SCORE = "score"
    private val ID = "id"
    private val FAILEDCLIENTATTEMPT = "failedClientAttempt"

    private var http: HTTP = HTTP(params)
    private var appId: String = params.getString("appId").toString()
    private var regPolicy: String = params.getString("regPolicy") ?: "reg"
    private var authPolicy: String = params.getString("authPolicy") ?: "auth"

    private val TAG = RestService::class.simpleName ?: LogUtils.TAG

    override suspend fun serviceRequestAccess(params: Bundle): Response {
        // Nothing to do at the moment

        // serviceRequestRegistration
        //
        // The registration/user will be created if it is not found by a registrationId/applicationId
        // combination as long as a user is also submitted as part of the registration.
        //
        // If a userId is used and the user does not exist then a user will be created.
        return Success(Bundle())
    }

    override suspend fun serviceRevokeAccess(params: Bundle): Response {
        // Nothing to do at the moment
        return Success(Bundle())
    }

    override suspend fun serviceRequestRegistration(params: Bundle): Response {
        LogUtils.logVerbose(context, TAG, "RestService serviceRequestRegistration")
        when (val httpResponse =
            http.post(REGISTRATIONCHALLENGES, createRequestRegistrationPayload(params))) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val registrationChallenge = Gson().fromJson(
                        httpResponse.payload, RegistrationChallenge::class.java
                    )

                    val result = Bundle()
                    result.putString(
                        IXUAF.REG_REQUEST, registrationChallenge.fidoRegistrationRequest
                    )
                    result.putString(IXUAF.REQUEST_ID, registrationChallenge.id)
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(null, TAG, "regRequests error ${httpResponse.payload}")
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

    private fun createFailureBundle(errorCode: Int, errorMessage: String?): Bundle {
        return Bundle().apply {
            putInt(IXUAF.ERROR_CODE, errorCode)
            putString(IXUAF.ERROR_MESSAGE, errorMessage)
        }
    }

    private fun createRequestRegistrationPayload(params: Bundle): String {

        val username = params.getString("username")

        val user = JSONObject()
        user.put("userId", username)

        val policy = JSONObject()
        policy.put("policyId", regPolicy)
        val application = JSONObject()
        application.put("applicationId", appId)
        policy.put("application", application)

        val reg = JSONObject()
        reg.put("registrationId", username)
        reg.put("application", application)
        reg.put("user", user)

        val challenge = JSONObject()
        challenge.put("policy", policy)
        challenge.put("registration", reg)

        return challenge.toString()

        //Example challnege : {"policy":{"policyId":"reg","application":{"applicationId":"fido"}},"registration":{"registrationId":"ft@ft.com fido registration","application":{"applicationId":"fido"},"user":{"userId":"ft@ft.com"}}}
    }

    override suspend fun serviceRegister(params: Bundle): Response {
        LogUtils.logVerbose(null, TAG, "RestService serviceRegister")
        val regRequestId = params.getString(IXUAF.REQUEST_ID)
        when (val httpResponse = http.post(
            "$REGISTRATIONCHALLENGES/$regRequestId", createRegistrationPayload(params)
        )) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val registrationChallenege = Gson().fromJson(
                        httpResponse.payload, RegistrationChallenge::class.java
                    )

                    val result = Bundle()
                    result.putString(
                        IXUAF.REG_CONFIRMATION, registrationChallenege.fidoRegistrationResponse
                    )
                    result.putShort(
                        IXUAF.RESPONSE_CODE, registrationChallenege.fidoResponseCode.toShort()
                    )
                    LogUtils.logVerbose(
                        null,
                        TAG,
                        "serviceRegister responseCode : ${registrationChallenege.fidoResponseCode.toShort()}"
                    )
                    result.putString(IXUAF.RESPONSE_MESSAGE, registrationChallenege.fidoResponseMsg)
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(null, TAG, "regRequests error ${httpResponse.payload}")
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

    private fun createRegistrationPayload(params: Bundle): String {
        val fidoRegistrationResponse: String =
            params.getString(IXUAF.REG_RESPONSE) ?: params.getString(IXUAF.SERVER_DATA)!!
        val regRequestId = params.getString(IXUAF.REQUEST_ID)
        val request = JSONObject()
        request.put("id", regRequestId)
        request.put("status", "PENDING")
        request.put("fidoRegistrationResponse", fidoRegistrationResponse)
        return request.toString()
    }

    override suspend fun serviceRequestAuthentication(params: Bundle): Response {
        LogUtils.logVerbose(null, TAG, "RestService serviceRequestAuthentication")
        val singleshot = params.getBoolean(IXUAF.SINGLE_SHOT)
        if (singleshot) {
            try {
                val appId = params.getString(IXUAF.APP_ID)
                val username = params.getString(IXUAF.USERNAME)
                val ssar =
                    SingleShotAuthenticationRequest.createUserAuthWithAllRegisteredAuthenticators(
                        context, appId, username
                    )
                ssar.addExtension("com.daon.face.ados.mode", "verify")
                ssar.addExtension("com.daon.face.retriesRemaining", "5")
                ssar.addExtension("com.daon.face.liveness.passive.type", "server")
                ssar.addExtension("com.daon.face.liveness.active.type", "none")
                ssar.addExtension("com.daon.passcode.type", "ALPHANUMERIC")
                //Add the decChain extension value here
                //ssar.addExtension("com.daon.sdk.ados.decChain", decChain)
                val result = Bundle()
                result.putString(
                    IXUAF.AUTH_REQUEST, ssar.toString()
                )
                return Success(result)
            } catch (e: Exception) {
                LogUtils.logError(context, TAG, "serviceRequestAuthentication error ${e.message}")
                return Failure(createFailureBundle(-4, e.message))
            }
        } else {
            when (val httpResponse =
                http.post(AUTHENTICATIONREQUESTS, createAuthenticationRequestPayload(params))) {
                is HTTP.Success -> {
                    if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                        val authenticationChallenge = Gson().fromJson(
                            httpResponse.payload, AuthenticationRequest::class.java
                        )

                        val result = Bundle()
                        result.putString(
                            IXUAF.AUTH_REQUEST, authenticationChallenge.fidoAuthenticationRequest
                        )
                        result.putString(IXUAF.REQUEST_ID, authenticationChallenge.id)
                        return Success(result)
                    } else {
                        val error: Error = try {
                            LogUtils.logError(null, TAG, "regRequests error ${httpResponse.payload}")
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
    }

    private fun createAuthenticationRequestPayload(params: Bundle): String {
        val username = params.getString(IXUAF.USERNAME)
        val description = params.getString("description")
        val confirmationOTP = params.getBoolean(IXUAF.CONFIRMATION_OTP)
        val user = JSONObject()
        user.put("userId", username)

        val policy = JSONObject()
        policy.put("policyId", authPolicy)
        val application = JSONObject()
        application.put("applicationId", appId)
        policy.put("application", application)

        val request = JSONObject()
        request.put("policy", policy)
        if (params.containsKey(IXUAF.TRANSACTION_CONTENT_TYPE)) {
            request.put(
                "secureTransactionContentType",
                params.getString(IXUAF.TRANSACTION_CONTENT_TYPE)
            )
            if (params.getString(IXUAF.TRANSACTION_CONTENT_TYPE)
                    .equals("text/plain", ignoreCase = true)
            ) {
                request.put(
                    "secureTextTransactionContent",
                    params.getString(IXUAF.TRANSACTION_CONTENT_DATA)
                )
            } else {
                request.put(
                    "secureImageTransactionContent",
                    params.getString(IXUAF.TRANSACTION_CONTENT_DATA)
                )
            }
        }
        request.put("user", user)
        request.put("type", "FI")
        if (confirmationOTP) {
            request.put("oneTimePasswordEnabled", true)
        }
        request.put("description", description ?: "NA")
        return request.toString()
    }

    override suspend fun serviceAuthenticate(params: Bundle): Response {
        LogUtils.logVerbose(null, TAG, "RestService serviceAuthenticate")
        val authRequestId = params.getString(IXUAF.REQUEST_ID)
        val httpUrl: String = if (authRequestId != null) {
            "$AUTHENTICATIONREQUESTS/$authRequestId"
        } else {
            AUTHENTICATIONREQUESTS
        }
        when (val httpResponse = http.post(httpUrl, createAuthenticationPayload(params))) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val authResponse = Gson().fromJson(
                        httpResponse.payload, AuthenticationRequest::class.java
                    )

                    val result = Bundle()
                    result.putString(
                        IXUAF.AUTH_CONFIRMATION, authResponse.fidoAuthenticationResponse
                    )
                    result.putShort(
                        IXUAF.RESPONSE_CODE, authResponse.fidoResponseCode.toShort()
                    )
                    LogUtils.logVerbose(null, TAG,
                        "serviceAuthenticate responseCode : ${authResponse.fidoResponseCode.toShort()}"
                    )
                    result.putString(IXUAF.RESPONSE_MESSAGE, authResponse.fidoResponseMsg)
                    result.putString(IXUAF.EMAIL, authResponse.userId)
                    // create session
                    //result.putString("lastLoggedIn", authResponse.lastLoggedIn.toString())
                    //result.putString("loggedInWith", authResponse.loggedInWith.toString())
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(null, TAG, "regRequests error ${httpResponse.payload}")
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

    private fun createAuthenticationPayload(params: Bundle): String {
        val fidoAuthResponse: String =
            params.getString(IXUAF.AUTH_RESPONSE) ?: params.getString(IXUAF.SERVER_DATA)!!
        val authRequestId = params.getString(IXUAF.REQUEST_ID)
        var fidoAuthRequest: String? = params.getString(IXUAF.AUTH_REQUEST)

        val request = JSONObject()
        request.put("fidoAuthenticationResponse", fidoAuthResponse)
        if (authRequestId == null) {
            //authRequestId is null for SingleShot request
            //making the SingleShot request here
            request.put("fidoAuthenticationRequest", fidoAuthRequest)
            val policy = JSONObject()
            policy.put("policyId", authPolicy)
            val application = JSONObject()
            application.put("applicationId", appId)
            policy.put("application", application)
            request.put("policy", policy)
            request.put("description", "Single shot")
            request.put("type", "FI")
        }

        return request.toString()
    }

    override suspend fun serviceUpdate(params: Bundle): Response {
        LogUtils.logVerbose(null, TAG, "RestService serviceUpdate")
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

    override suspend fun serviceRequestDeregistration(params: Bundle): Response {
        LogUtils.logVerbose(null, TAG, "RestService serviceRequestDeregistration")
        val username = params.getString(IXUAF.USERNAME)
        val aaid = params.getString(IXUAF.AAID)
        if (username != null && aaid != null) {
            val authId = getActiveAuthenticatorId(username, aaid)
            if (authId != null) {
                return archiveAuthenticator(authId)
            }
        }
        val error = Error(code = -4, message = "Server communication error")
        return Failure(createFailureBundle(error.code, error.message))
    }

    private fun archiveAuthenticator(authId: String): Response {
        val httpUrl = "$AUTHENTICATORS/$authId/archived"
        return when (val httpResponse = http.post(httpUrl, "{}")) {
            is HTTP.Success -> {
                val deregRequest =
                    Gson().fromJson(httpResponse.payload, DeregistrationRequest::class.java)
                val params = Bundle()
                params.putString(IXUAF.DEREG_REQUEST, deregRequest.fidoDeregistrationRequest)
                Success(params)
            }

            is HTTP.Error -> {
                Failure(createFailureBundle(httpResponse.code, httpResponse.message))
            }

        }
    }

    private fun getActiveAuthenticatorId(username: String, aaid: String): String? {
        when (val authResponse = getAuthenticators(username)) {
            is Success -> {
                val authDetailsJson = JSONObject(authResponse.params.getString("authDetails"))
                if (authDetailsJson.has("items")) {
                    val authDetails = authDetailsJson.getJSONArray("items")
                    val authDetailsArray =
                        Gson().fromJson(authDetails.toString(), Array<AuthDetails>::class.java)
                    for (auth in authDetailsArray) {
                        if (auth.authenticatorAttestationId == aaid && auth.status == "ACTIVE") {
                            return auth.id
                        }
                    }
                }
                return null
            }

            is Failure -> {
                return null
            }
        }
    }

    private fun parseUserResponse(userResponseString: String?): User? {
        val usersJson = JSONObject(userResponseString)
        if (usersJson.has("items")) {
            val userDetails = usersJson.getJSONArray("items")
            val usersArray = Gson().fromJson(userDetails.toString(), Array<User>::class.java)
            for (user in usersArray) {
                if (user.status == "ACTIVE") {
                    return user

                }
            }
        }
        return null
    }

    private fun getAuthenticators(username: String): Response {
        when (val userResponse = getUser(username)) {
            is Success -> {
                val userString = userResponse.params.getString("user")
                val user = parseUserResponse(userString)
                if (user != null) {
                    val httpUrl = "$USERS/${user.id}/authenticators?limit=1000"
                    return when (val authResponse = http.get(httpUrl)) {
                        is HTTP.Success -> {
                            val response = Bundle()
                            response.putString("authDetails", authResponse.payload)
                            Success(response)
                        }

                        is HTTP.Error -> {
                            Failure(createFailureBundle(authResponse.code, authResponse.message))
                        }
                    }
                } else {
                    val error = Error(code = -4, message = "Server communication error")
                    return Failure(createFailureBundle(error.code, error.message))
                }
            }

            is Failure -> {
                return Failure(createFailureBundle(userResponse.params.getInt(IXUAF.ERROR_CODE), userResponse.params.getString(IXUAF.ERROR_MESSAGE)))
            }
        }
    }

    private fun getUser(username: String): Response {
        val httpUrl = "$USERS?userId=$username"
        when (val httpResponse = http.get(httpUrl)) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val result = Bundle()
                    result.putString("user", httpResponse.payload)
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(null, TAG, "regRequests error ${httpResponse.payload}")
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

    override suspend fun serviceUpdateAttempt(params: Bundle): Response {
        LogUtils.logVerbose(null, TAG, "RestService serviceUpdateAttempt")
        val paramId = params.getString(VerificationAttemptParameters.PARAM_USER_AUTH_KEY_ID)
        val paramErrorCode = params.getInt(VerificationAttemptParameters.PARAM_ERROR_CODE)
        val paramScore = params.getDouble(VerificationAttemptParameters.PARAM_SCORE)
        val authRequestId = params.getString(IXUAF.REQUEST_ID)

        val attempts = JSONObject()
        attempts.put(AUTHKEYID, paramId)
        attempts.put(ERRORCODE, paramErrorCode)
        attempts.put(SCORE, paramScore)

        val request = JSONObject()
        request.put(ID, authRequestId)
        request.put(FAILEDCLIENTATTEMPT, attempts)

        val httpUrl = "$AUTHENTICATIONREQUESTS/$authRequestId/appendFailedAttempt"

        when (val httpResponse = http.post(httpUrl, request.toString())) {
            is HTTP.Success -> {
                if (httpResponse.httpStatusCode == HttpURLConnection.HTTP_CREATED || httpResponse.httpStatusCode == HttpURLConnection.HTTP_OK) {
                    val attemptResponse =
                        Gson().fromJson(httpResponse.payload, AuthenticationRequest::class.java)
                    val result = Bundle()
                    result.putString(
                        IXUAF.RESPONSE, attemptResponse.fidoAuthenticationResponse
                    )
                    return Success(result)
                } else {
                    val error: Error = try {
                        LogUtils.logError(null, TAG, "serviceUpdateAttempt error ${httpResponse.payload}")
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

    override suspend fun serviceDeleteUser(params: Bundle): Response {
        LogUtils.logVerbose(null, TAG, "RestService serviceDeleteUser")
        val username = params.getString(IXUAF.USERNAME)
        if (username != null) return archiveUser(username)
        return Failure(createFailureBundle(ErrorFactory.UNEXPECTED_ERROR_CODE, "username is null"))
    }

    private fun archiveUser(username: String): Response {
        when (val userResponse = getUser(username)) {
            is Success -> {
                val userString = userResponse.params.getString("user")
                val user = Gson().fromJson(
                    userString, User::class.java
                )
                val httpUrl = "$USERS/${user.id}/archived"
                return when (val response = http.post(httpUrl, " ")) {
                    is HTTP.Success -> {
                        Success(Bundle())
                    }

                    is HTTP.Error -> {
                        Failure(createFailureBundle(response.code, response.message))
                    }
                }
            }

            is Failure -> {
                return Failure(createFailureBundle(userResponse.params.getInt(IXUAF.ERROR_CODE), userResponse.params.getString(IXUAF.ERROR_MESSAGE)))
            }
        }

    }


}