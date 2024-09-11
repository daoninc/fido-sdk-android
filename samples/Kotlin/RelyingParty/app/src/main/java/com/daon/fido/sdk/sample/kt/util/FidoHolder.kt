package com.daon.fido.sdk.sample.kt.util

import android.content.Context
import android.os.Bundle
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.IXUAFService
import com.daon.fido.sdk.sample.kt.Config
import com.daon.fido.sdk.sample.kt.service.rpsa.IXUAFRPSAService

/**
 *  Singleton class to hold the FIDO SDK instance and the RPSA server instance.
 *  This ensures a single point of access to the IXUAF instance.
 */
class FidoHolder private constructor(private val context: Context) {
    var fido: IXUAF
    private var rpsaServer: IXUAFService

    init {
        // Initialize the IXUAFService and IXUAF instance
        rpsaServer = getServer()
        fido = IXUAF(context, rpsaServer)
    }

    // Get the IXUAFService instance
    private fun getServer(): IXUAFService {
        val rpsaParams = Bundle()
        rpsaParams.putString("server_url", Config.getProperty(Config.SERVER_URL, context))
        return IXUAFRPSAService(context, rpsaParams)
    }

    companion object {
        private var instance: FidoHolder? = null

        // Get the singleton instance of FidoHolder
        fun getInstance(context: Context): FidoHolder {
            if (instance != null) {
                return instance as FidoHolder
            }
            instance = FidoHolder(context)
            return instance as FidoHolder
        }
    }

}