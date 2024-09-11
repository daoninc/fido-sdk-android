package com.daon.fido.sdk.sample.kt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject

/**
 * Base class for all ViewModels in the app.
 * Provides access to the application context and resources.
 */
open class BaseViewModel @Inject constructor(application: Application): AndroidViewModel(application){
    // Property to get the application context
    protected val context
        get() = getApplication<Application>()

    // Function to get a string resource by id
    fun getResourceString(id : Int) = context.resources.getString(id)

}