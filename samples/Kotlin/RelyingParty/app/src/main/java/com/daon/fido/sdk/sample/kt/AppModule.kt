package com.daon.fido.sdk.sample.kt

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.service.rpsa.RPSAService
import com.daon.fido.sdk.sample.kt.service.rest.RestService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule class which provides application level dependencies using Dagger Hilt.
 * Includes methods to provide instances of `IXUAF` and `SharedPreferences`.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    // Provides an instance of `IXUAF` using the application context.
    @Provides
    @Singleton
    fun provideFido(@ApplicationContext appContext: Context): IXUAF {
        val rpsaParams = Bundle().apply {
            putString("server_url", Config.getProperty(Config.SERVER_URL, appContext))
        }
        val rpsaServer = RPSAService(appContext, rpsaParams)
        return IXUAF(appContext, rpsaServer)

        //Use REST service

        /*val restParams = Bundle()
        restParams.putString("appId", "mobileteamfido")
        restParams.putString("regPolicy", "reg")
        restParams.putString("authPolicy", "auth")
        //Basic Auth
        restParams.putString("username", "admin2")
        restParams.putString("password", "admin2")
        restParams.putString("server_url", "https://us-dev-env4.identityx-cloud.com:443/fido")
        restParams.putString("rest_path", "IdentityXServices/rest/v1")
        val restServer =  RestService(appContext, restParams)
        return IXUAF(appContext, restServer)*/


    }

    // Provides an instance of `SharedPreferences` using the application context.
    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext appContext: Context): SharedPreferences =
        appContext.getSharedPreferences("pref_sampleapp_kotlin", Context.MODE_PRIVATE)
}