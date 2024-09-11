package com.daon.fido.sdk.sample.kt

import android.content.Context
import android.content.SharedPreferences
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.util.FidoHolder
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
    fun provideFido(@ApplicationContext appContext: Context): IXUAF =
        FidoHolder.getInstance(appContext).fido

    // Provides an instance of `SharedPreferences` using the application context.
    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext appContext: Context): SharedPreferences =
        appContext.getSharedPreferences("pref_sampleapp_kotlin", Context.MODE_PRIVATE)
}