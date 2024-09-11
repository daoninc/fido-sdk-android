package com.daon.fido.sdk.sample.kt.service.rest

import android.content.Context
import android.os.Bundle
import com.daon.fido.client.sdk.IXUAFService
import com.daon.fido.client.sdk.Response
import com.daon.fido.client.sdk.Success


/**
 * Implementation of the IXUAFService interface for the REST server.
 */
class IXUAFRestService(context: Context, params: Bundle): IXUAFService {
    private var restService: RestService = RestService.getInstance(context, params)

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

    override suspend fun serviceDeleteUser(params: Bundle): Response =
        restService.serviceDeleteUser(params)

    //username, appId, regPolicy
    override suspend fun serviceRequestRegistration(params: Bundle) =
        restService.serviceRequestRegistration(params)

    //registrationResponse
    override suspend fun serviceRegister(params: Bundle): Response =
        restService.serviceRegister(params)

    override suspend fun serviceRequestAuthentication(params: Bundle): Response =
        restService.serviceRequestAuthentication(params)

    override suspend fun serviceAuthenticate(params: Bundle): Response =
        restService.serviceAuthenticate(params)

    override suspend fun serviceUpdate(params: Bundle): Response =
        restService.serviceUpdate(params)

    override suspend fun serviceRequestDeregistration(params: Bundle): Response =
        restService.serviceRequestDeregistration(params)

    override suspend fun serviceUpdateAttempt(info: Bundle): Response =
        restService.serviceUpdateAttempt(info)
}