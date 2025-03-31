package com.daon.fido.sdk.sample.kt.authenticators

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.Group
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.Policy
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.client.sdk.util.SDKPreferences
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol
import com.daon.sdk.authenticator.exception.ControllerInitializationException
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class to hold the state of the Authenticators screen.
data class AuthenticatorState(
    val policyAvailable: Boolean = false,
    val policy: Policy? = null,
    val groupSelected: Boolean = false,
    val group: Group? = null,
    val authToDeregister: Authenticator? = null,
    val selectedIndex: Int = -1,
    val registrationResult: RegistrationResult? = null,
    val deregistrationResult: DeregistrationResult? = null,
    val inProgress: Boolean = false
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

    //Mutable and public state flow to hold the state of the Authenticators screen.
    private val _authState = MutableStateFlow(
        AuthenticatorState()
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
            _authState.update { currentAuthState ->
                currentAuthState.copy(
                    policy = it, policyAvailable = true
                )
            }
        }
        //Discover available authenticators.
        discover()

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

    // Update the user selected authenticator group
    fun updateSelectedGroup(group: Group) {
        _authState.update { currentAuthState ->
            currentAuthState.copy(
                group = group, groupSelected = true, policyAvailable = false
            )
        }
        prefs.edit {
            this.putString("selectedAaid", group.getAuthenticator().aaid)
        }
    }

    // Reset the list of authenticators available.
    fun resetAuthListAvailable() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(policyAvailable = false)
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
            currentAuthState.copy(groupSelected = false)
        }
    }

    fun resetInProgress() {
        _authState.update { currentAuthState ->
            currentAuthState.copy(inProgress = false)
        }
    }

    // Perform silent registration.
    fun registerSilent() {
         try {
            _authState.value.group?.let { group ->
                val silentController = fido.getController(getApplication(), group)
                silentController?.startCapture()
                silentController?.completeCapture()
            }
        } catch (e: ControllerInitializationException) {

        }
    }

    fun getPasscodeController(): PasscodeControllerProtocol? {
        return try {
            _authState.value.group?.let { selectedAuth ->
                fido.getController(getApplication(), selectedAuth) as? PasscodeControllerProtocol
            }
        } catch (e: ControllerInitializationException) {
            null
        }
    }

    fun getFaceController(): FaceControllerProtocol? {
        return try {
            _authState.value.group?.let { group ->
                fido.getController(getApplication(), group) as? FaceControllerProtocol
            }
        } catch (e: ControllerInitializationException) {
            null
        }
    }

    fun getFingerprintController(): FingerprintCaptureControllerProtocol? {
        return try {
            _authState.value.group?.let { group ->
                fido.getController(getApplication(), group) as? FingerprintCaptureControllerProtocol
            }
        } catch (e: ControllerInitializationException) {
            null
        }
    }

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId
    }
}