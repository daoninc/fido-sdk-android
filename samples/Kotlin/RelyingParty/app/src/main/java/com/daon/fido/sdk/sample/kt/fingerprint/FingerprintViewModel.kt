package com.daon.fido.sdk.sample.kt.fingerprint

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.fido.sdk.sample.kt.R
import com.daon.fido.sdk.sample.kt.model.FINGERPRINT_AUTH_AAID
import com.daon.sdk.authenticator.controller.CaptureCompleteResult
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol
import com.daon.sdk.authenticator.exception.ControllerInitializationException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FingerprintViewModel handles the state and logic for the fingerprint authentication.
 * @param application The application context.
 * @param fido The IXUAF instance.
 */
@HiltViewModel
class FingerprintViewModel @Inject constructor(
    application: Application,
    private val fido: IXUAF
): BaseViewModel(application){

    // Fingerprint capture controller
    private lateinit var fingerController: FingerprintCaptureControllerProtocol

    // State flow to indicate capture completion
    private val _captureComplete = MutableStateFlow( false)
    val captureComplete  = _captureComplete.asStateFlow()

    // State flow to display capture information
    private val _captureInfo = MutableStateFlow("")
    val captureInfo = _captureInfo.asStateFlow()

    /**
     * Starts the fingerprint capture process.
     * @param context The context to use for starting the capture.
     */
    fun onStart(context: Context) {
        try {
            // Initialize the fingerprint capture controller
            fingerController = fido.getController(
                getApplication(),
                FINGERPRINT_AUTH_AAID
            ) as FingerprintCaptureControllerProtocol
            // Start the fingerprint capture
            fingerController.startCapture(context.findActivity()) { result ->
                if (result != null) {
                    when (result.type) {
                        CaptureCompleteResult.Type.TERMINATE_SUCCESS -> {
                            Log.d("DAON", "finger capture complete")
                            _captureComplete.value = true
                        }

                        CaptureCompleteResult.Type.TERMINATE_FAILURE -> {
                            Log.d("DAON", "finger capture TERMINATE_FAILURE")
                            fingerController.cancelCapture()
                            _captureComplete.value = true
                        }

                        CaptureCompleteResult.Type.CLIENT_ERROR -> {
                            Log.d("DAON", "finger capture CLIENT_ERROR")
                            _captureComplete.value = true
                        }

                        CaptureCompleteResult.Type.CLIENT_VALIDATION_ERROR -> {
                            Log.d("DAON", "finger capture CLIENT_VALIDATION_ERROR")
                        }
                    }
                }
            }

            // Add a listener for user lock warnings
            fido.addUserLockWarningListener {
                _captureInfo.value = getResourceString(R.string.user_lock_warning)
            }
        } catch (e: ControllerInitializationException) {
            Log.e("DAON", "Error starting fingerprint capture: ${e.message}")
            _captureInfo.value = e.message ?: ""
            _captureComplete.value = true
            cancelCurrentOperation()
        }

    }

    /**
     * Stops the fingerprint capture process.
     */
    fun onStop() {
        if (::fingerController.isInitialized)
            fingerController.stopCapture()
    }

    /**
     * Finds the activity from the context.
     * @return The activity if found, otherwise null.
     */
    private fun Context.findActivity(): AppCompatActivity? = when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    /**
     * Resets the capture complete state.
     */
    fun resetCaptureComplete() {
        _captureComplete.value = false
    }

    /**
     * Resets the capture info state.
     */
    fun resetCaptureInfo() {
        _captureInfo.value = ""
    }

    /**
     * Cancels the current operation.
     */
    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }
}