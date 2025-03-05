package com.daon.fido.sdk.sample.kt.authenticators

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.client.sdk.util.SDKPreferences
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.fido.sdk.sample.kt.model.ADOS_VOICE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.OOTP_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.PATTERN_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.SILENT_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.VOICE_AUTH_AAID
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class to hold the state of the Authenticators screen.
data class AuthenticatorState(
    val authArray: Array<Authenticator>,
    val authListAvailable: Boolean,
    val authSelected: Boolean,
    val selectedAuth: Authenticator?,
    val authToDeregister: Authenticator?,
    val selectedIndex: Int,
    val registrationResult: RegistrationResult?,
    val deregistrationResult: DeregistrationResult?,
    val inProgress: Boolean
)

// Data classes to hold the result of registration.
data class RegistrationResult(
    val success: Boolean, val message: String
)

// Data classes to hold the result of deregistration.
data class DeregistrationResult(
    val success: Boolean, val message: String
)

/*
 * ViewModel for the Authenticators screen.
 * It handles FIDO registration and deregistration of authenticators .
 */
@HiltViewModel
class AuthenticatorsViewModel @Inject constructor(
    application: Application, private val fido: IXUAF, private val prefs: SharedPreferences
) : BaseViewModel(application) {

    //Mutable and publiuc state flow to hold the state of the Authenticators screen.
    private val _authState = MutableStateFlow(
        AuthenticatorState(
            authArray = emptyArray<Authenticator>(),
            authListAvailable = false,
            authSelected = false,
            selectedAuth = null,
            authToDeregister = null,
            selectedIndex = -1,
            registrationResult = null,
            deregistrationResult = null,
            inProgress = false
        )
    )
    val authState: StateFlow<AuthenticatorState> = _authState

    //Mutable state list to hold the list of authenticators discovered.
    private val _discoverList = mutableStateListOf<Authenticator>()

    //Public list to hold the list of authenticators discovered.
    val discoverList: List<Authenticator> = _discoverList

    private var sessionId: String = ""


    fun onStart() {
        //Add a listener to choose authenticator.
        fido.addChooseAuthenticatorListener {
            filterAuthArray(it)
        }
        //Discover available authenticators.
        discover()

    }

    //Filter the authenticators to remove the ones that are not supported.
    private fun filterAuthArray(authArray: Array<Authenticator>) {
        val resultArray = mutableStateListOf<Authenticator>()
        for (auth in authArray) {
            if (auth.aaid != VOICE_AUTH_AAID && auth.aaid != ADOS_VOICE_AUTH_AAID && auth.aaid != OOTP_AUTH_AAID && auth.aaid != PATTERN_AUTH_AAID) {
                resultArray.add(auth)
            }
        }
        Log.d("DAON", "filterAuthArray resultArray size : ${resultArray.size}")

        //Update the state with the filtered authenticators.
        if (resultArray.size == 0) {
            _authState.update { currentAuthState ->
                currentAuthState.copy(
                    registrationResult = RegistrationResult(
                        success = false, message = "No more authenticators available to register"
                    )
                )
            }
            cancelCurrentOperation()
        } else {
            _authState.update { currentAuthState ->
                currentAuthState.copy(
                    authArray = resultArray.toTypedArray(), authListAvailable = true
                )
            }
        }
    }

    fun onStop() {
        // No specific cleanup required.
    }

    // Discover available authenticators.
    private fun discover() {
        val username = prefs.getString("currentUser", null).toString()
        val appId = SDKPreferences.instance().getString(context, IXUAF.KEY_APP_ID)
        val result = fido.discover()
        result.onSuccess { discover ->
            val resultArray = mutableStateListOf<Authenticator>()
            discover.availableAuthenticators.forEach { auth ->
                val res = fido.isRegistered(auth.aaid, username, appId)
                if (res.isSuccess && res.getOrNull() == true) {
                    resultArray.add(auth)
                }
            }
            _discoverList.clear()
            _discoverList.addAll(resultArray)

        }.onFailure {
            //Handle failure here
        }
    }

    // Register the selected authenticator.
    fun register() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(inProgress = true)
        }
        viewModelScope.launch(Dispatchers.Default) {
            val bundle = Bundle()
            val username = prefs.getString("currentUser", null)
            if (username != null) {
                bundle.putString(IXUAF.USERNAME, username)
            }
            bundle.putString("sessionId", sessionId)

            when (val response = fido.register(bundle)) {
                is Success -> {
                    // Handle successful registration.
                    prefs.edit {
                        this.remove("selectedAaid")
                    }
                    //discover and update the list
                    Log.d("DAON", "fido register success")
                    _authState.update { currentAuthState ->
                        currentAuthState.copy(
                            inProgress = false,
                            registrationResult = RegistrationResult(
                                success = true, message = "Registration success"
                            )
                        )
                    }
                    discover()

                }

                is Failure -> {
                    // Handle registration failure.
                    prefs.edit {
                        this.remove("selectedAaid")
                    }
                    _authState.update { currentAuthState ->
                        currentAuthState.copy(
                            inProgress = false,
                            registrationResult = RegistrationResult(
                                success = false, message = "Reason for registration failure is: ${
                                    response.params.getString(
                                        IXUAF.ERROR_MESSAGE
                                    )
                                }"
                            )
                        )
                    }
                }
            }

        }
    }

    // Cancel the current operation.
    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

    // Deregister the selected authenticator.
    fun deregister(auth: Authenticator) {
        _authState.update { currentAuthState ->
            currentAuthState.copy(inProgress = true)
        }
        viewModelScope.launch(Dispatchers.Default) {
            val username = prefs.getString("currentUser", null).toString()
            val params = Bundle()
            params.putString("sessionId", sessionId)
            when (val response = fido.deregister(auth.aaid, username, params)) {
                is Success -> {
                    // Handle successful deregistration.
                    Log.d("DAON", "fido deregister success")
                    updateAuthToDeregister(null, -1)
                    _authState.update { currentAuthState ->
                        currentAuthState.copy(
                            inProgress = false,
                            deregistrationResult = DeregistrationResult(
                                success = true, message = "Deregistration success"
                            )
                        )
                    }
                    discover()
                }

                is Failure -> {
                    // Handle deregistration failure.
                    updateAuthToDeregister(null, -1)
                    Log.d("DAON", "fido deregister failure")
                    _authState.update { currentAuthState ->
                        currentAuthState.copy(
                            inProgress = false,
                            deregistrationResult = DeregistrationResult(
                                success = false, message = "Reason for deregistration failure is: ${
                                    response.params.getString(
                                        IXUAF.ERROR_MESSAGE
                                    )
                                }"
                            )
                        )
                    }
                }
            }
        }
    }

    // Update authState with the selected authenticator.
    fun updateSelectedAuth(auth: Authenticator) {
        _authState.update { currentAuthState ->
            currentAuthState.copy(
                selectedAuth = auth, authSelected = true, authListAvailable = false
            )
        }
        prefs.edit {
            this.putString("selectedAaid", auth.aaid)
        }
    }

    // Reset the list of authenticators available.
    fun resetAuthListAvailable() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(authListAvailable = false)
        }
    }

    // Reset the registration result.
    fun resetRegistrationResult() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(registrationResult = null)
        }
    }

    // Reset the deregistration result.
    fun resetDeregistrationResult() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(deregistrationResult = null)
        }
    }

    // Update authState with the authenticator to deregister.
    fun updateAuthToDeregister(auth: Authenticator?, index: Int) {
        _authState.update { currentAuthState ->
            currentAuthState.copy(authToDeregister = auth, selectedIndex = index)
        }
    }

    // Reset the selected authenticator.
    fun deselectAuth() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(authSelected = false)
        }
    }

    // Perform silent registration.
    fun registerSilent() {
        val silentController =
            fido.getController(getApplication(), SILENT_AUTH_AAID) as CaptureControllerProtocol
        silentController.startCapture()
        silentController.completeCapture()
    }

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId
    }
}