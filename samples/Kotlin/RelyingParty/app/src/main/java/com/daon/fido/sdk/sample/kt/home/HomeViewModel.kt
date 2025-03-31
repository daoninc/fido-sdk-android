package com.daon.fido.sdk.sample.kt.home

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.Group
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.Policy
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.transaction.TransactionContent
import com.daon.fido.client.sdk.transaction.TransactionUtils
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.fido.sdk.sample.kt.R
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol
import com.daon.sdk.authenticator.exception.ControllerInitializationException
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//Data class to hold the state of the transaction
data class TransactionState(
    val policyAvailable: Boolean = false,
    val policy: Policy? = null,
    val groupSelected: Boolean = false,
    val group: Group? = null,
    val authenticationCompleted: Boolean = false,
    val resetComplete: Boolean = false,
    val message: String? = null,
    val confirmTransaction: Boolean = false,
    val transactionContent: TransactionContent? = null,
    val transactionConfirmationResult:Int = 99,
    val confirmationOTPReceived: Boolean = false,
    val confirmationOTP: String? = null
)

/**
 * Manages the UI state and business logic for the Home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(application: Application, private val fido: IXUAF, private val prefs: SharedPreferences): BaseViewModel(application) {

    // Mutable state flow for the transaction state
    private val _transactionState = MutableStateFlow(
        TransactionState()
    )
    // Public state flow for the transaction state
    val transactionState: StateFlow<TransactionState> = _transactionState

    // Mutable and public state flow for the in progress flag
    private var _inProgress = MutableStateFlow(false)
    val inProgress = _inProgress.asStateFlow()

    private var sessionId: String = ""

    // Called when the ViewModel is created
    fun onStart() {
        // Add listener for choosing an authenticator
        fido.addChooseAuthenticatorListener {
            // Show the authList to user
            _transactionState.update { currentTransactionState ->
                currentTransactionState.copy(policyAvailable = true, policy = it)

            }
        }

        // Add listener for displaying transaction
        fido.addDisplayTransactionListener {
            _transactionState.update { currentTransactionState ->
                val txnContent = TransactionUtils.getTransactionContent(context, it)
                // Update the transaction state with the transaction content
                currentTransactionState.copy(confirmTransaction = true, transactionContent = txnContent)
            }
        }

        // Add listener for confirmation OTP
        fido.addConfirmationOTPListener { confirmationOTP ->
            Log.d("DAON", "Confirmation OTP: $confirmationOTP")
            _transactionState.update { currentTransactionState ->
                // Update the transaction state with the received confirmation OTP
                currentTransactionState.copy(
                    confirmationOTPReceived = true,
                    confirmationOTP = confirmationOTP
                )
            }
        }
    }

    fun onStop() {
        // No specific actions needed in onStop()
    }

    // Authenticate the user
    fun authenticate(isSingleShot: Boolean) {
        _inProgress.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val bundle = Bundle()
            val username = prefs.getString("currentUser", null)
            if (username != null) {
                bundle.putString(IXUAF.USERNAME, username)
            }
            if (isSingleShot) {
                bundle.putBoolean(IXUAF.SINGLE_SHOT, true)
                //Creating SSAR in the comm layer needs APP_ID
                bundle.putString(IXUAF.APP_ID, fido.getAppID())
            }
            //sample code to demonstrate an image shown as transaction confirmation content
            //bundle.putString(IXUAF.TRANSACTION_CONTENT_TYPE, "image/png")
            //bundle.putString(IXUAF.TRANSACTION_CONTENT_DATA, getResourceString(R.string.transaction_image_content))

            //And here the transaction content is text
            bundle.putString(IXUAF.TRANSACTION_CONTENT_TYPE, "text/plain")
            bundle.putString(IXUAF.TRANSACTION_CONTENT_DATA, getResourceString(R.string.transaction_text_content))
            bundle.putString("sessionId", sessionId)
            when (val response = fido.authenticate(bundle)) {
                is Success -> {
                    // Handle successful authentication
                    _inProgress.value = false
                    _transactionState.update { currentTransactionState ->
                        currentTransactionState.copy(authenticationCompleted = true, message = getResourceString(
                            R.string.transaction_validation_success
                        ))
                    }
                }

                is Failure -> {
                    // Handle authentication failure
                    _inProgress.value = false
                    _transactionState.update { currentTransactionState ->
                        currentTransactionState.copy(authenticationCompleted = true, message = response.params.getString(IXUAF.ERROR_MESSAGE))
                    }
                }
            }
        }
    }

    // Archive the user and remove all Fido SDK data from the device
    fun resetFido() {
        deleteUser()
    }

    // Remove all Fido SDK data from the device
    private fun reset() {
        val username = prefs.getString("currentUser", null)
        viewModelScope.launch {
            when (fido.reset(fido.getAppID(), username)) {
                is Success -> {
                    Log.d("DAON", "Reset Fido Success :" + fido.isInitialised())
                    _transactionState.update { currentTransactionState ->
                        currentTransactionState.copy(resetComplete = true)
                    }
                }

                is Failure -> {

                }
            }
        }
    }

    //Archive the current user
    private fun deleteUser() {
        val username = prefs.getString("currentUser", null)
        val bundle = Bundle().apply {
            this.putString("sessionId", sessionId)
        }
        viewModelScope.launch {
            when (fido.deleteUser(username, bundle)) {
                is Success -> {
                    Log.d("DAON", "Delete User Success")
                    reset()
                }

                is Failure -> {
                    Log.d("DAON", "Delete User Failure")
                }
            }
        }
    }

    // Logout the user
    fun doLogout() {
        viewModelScope.launch {
            when (fido.revokeServiceAccess(Bundle())) {
                is Success -> {
                    Log.d("DAON", "Logout Success")
                }

                is Failure -> {
                    Log.d("DAON", "Logout Failure")
                }
            }
        }
    }

    // Reset the resetComplete flag
    fun resetResetComplete() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(resetComplete = false)
        }
    }

    // Reset the authenticationCompleted flag
    fun resetAuthenticationCompleted() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(authenticationCompleted = false)
        }
    }

    // Reset the authSelected flag
    fun deselectAuth() {
        _inProgress.value = false
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(groupSelected = false)
        }
    }

    // Update the selected authenticator
    fun updateSelectedGroup(group: Group) {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(group = group, groupSelected = true, policyAvailable = false)
        }
        prefs.edit {
            this.putString("selectedAaid", group.getAuthenticator().aaid)
        }
    }

    // Reset the authenticatorChoiceGroupAvailable flag
    fun resetAuthArrayAvailable() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(policyAvailable = false)
        }
    }

    // Perform silent authentication
    fun authenticateSilent() {
        try {
            _transactionState.value.group?.let { group ->
                val silentController = fido.getController(getApplication(), group)
                silentController?.startCapture()
                silentController?.completeCapture()
            }
        } catch (e: ControllerInitializationException) {

        }
    }

    // Cancel the current operation
    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

    // Update the transaction state with the transaction confirmation result
    fun updateTransactionConfirmationResult(result: Int) {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(transactionConfirmationResult = result)
        }
    }

    // Submit the transaction confirmation result
    fun submitDisplayTransactionResult(result: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            resetTransactionConfirmationResult()
            fido.submitDisplayTransactionResult(result, " ")
        }
    }

    // Reset the transaction data
    fun resetTransactionData() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(confirmTransaction = false, transactionContent = null)
        }
    }

    // Reset the transaction confirmation result
    private fun resetTransactionConfirmationResult() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(transactionConfirmationResult = 99)
        }
    }

    // Reset the confirmation OTP
    fun resetConfirmationOTP() {
        _transactionState.update { currentTransactionState ->
            currentTransactionState.copy(confirmationOTPReceived = false, confirmationOTP = null)
        }
    }

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId
    }

    fun getPasscodeController(): PasscodeControllerProtocol? {
        return try {
            _transactionState.value.group?.let { group ->
                fido.getController(getApplication(), group) as? PasscodeControllerProtocol
            }
        } catch (e: ControllerInitializationException) {
            Log.d("DAON", "getPasscodeController: ${e.message}")
            null
        }
    }

    fun getFaceController(): FaceControllerProtocol? {
        return try {
            _transactionState.value.group?.let { group ->
                fido.getController(getApplication(), group) as? FaceControllerProtocol
            }
        } catch (e: ControllerInitializationException) {
            Log.d("DAON", "getFaceController: ${e.message}")
            null
        }
    }

    fun getFingerprintController(): FingerprintCaptureControllerProtocol? {
        return try {
            _transactionState.value.group?.let { group ->
                fido.getController(getApplication(), group) as? FingerprintCaptureControllerProtocol
            }
        } catch (e: ControllerInitializationException) {
            Log.d("DAON", "getFingerprintController: ${e.message}")
            null
        }
    }

}