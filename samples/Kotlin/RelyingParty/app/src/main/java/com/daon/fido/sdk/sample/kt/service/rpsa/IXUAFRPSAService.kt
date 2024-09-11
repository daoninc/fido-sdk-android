package com.daon.fido.sdk.sample.kt.service.rpsa

import android.content.Context
import android.os.Bundle
import com.daon.fido.client.sdk.IXUAFService
import com.daon.fido.client.sdk.Response

/**
 * Implementation of the IXUAFService interface for the RPSA server.
 */
class IXUAFRPSAService(context: Context, params: Bundle) : IXUAFService {
    private var rpsaService: RPSAService = RPSAService.getInstance(context, params)
    override suspend fun serviceRequestAccess(params: Bundle) =
        rpsaService.serviceCreateAccount(params)

    override suspend fun serviceRevokeAccess(params: Bundle): Response {
        return rpsaService.serviceDeleteSession()
    }

    override suspend fun serviceDeleteUser(params: Bundle): Response {
        return rpsaService.serviceDeleteAccount()
    }
    override suspend fun serviceRequestRegistration(params: Bundle) =
        rpsaService.serviceRequestRegistration()

    override suspend fun serviceRegister(params: Bundle) = rpsaService.serviceRegister(params)

    override suspend fun serviceRequestAuthentication(params: Bundle) =
        rpsaService.serviceRequestAuthentication(params)

    override suspend fun serviceAuthenticate(params: Bundle) =
        rpsaService.serviceAuthenticate(params)

    override suspend fun serviceRequestDeregistration(params: Bundle) =
        rpsaService.serviceRequestDeregistration(params)

    override suspend fun serviceUpdate(params: Bundle) = rpsaService.serviceUpdate(params)

    override suspend fun serviceUpdateAttempt(info: Bundle) = rpsaService.serviceUpdateAttempt(info)

}