package com.daon.fido.sdk.sample.kt.intro

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.Failure
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.Success
import com.daon.fido.client.sdk.core.ErrorFactory
import com.daon.fido.client.sdk.model.AccountInfo
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.fido.sdk.sample.kt.model.SILENT_AUTH_AAID
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol
import com.daon.sdk.crypto.log.LogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject


// Data class representing the UI state of the Intro screen
data class FidoUiState(
    val inProgress: Boolean = false,
    val initializationResult: FidoInitializationResult? = null,
    val accountCreationResult: AccountCreationResult? = null,
    val authArrayAvailable: Boolean = false,
    val authArray: Array<Authenticator>,
    val authSelected: Boolean,
    val selectedAuth: Authenticator?,
    val loginResult: LoginResult? = null,
    val username: String? = null,
    val accountArray: Array<AccountInfo>,
    val accountListAvailable: Boolean,
    val accountSelected: Boolean,
    val selectedAccount: AccountInfo?
)

// Data class representing the result of an account creation operation
data class AccountCreationResult(
    val success: Boolean,
    val message: String
)

// Data class representing the result of a login operation
data class LoginResult(
    val success: Boolean,
    val message: String,
    val code: Int
)

// Data class representing the result of a FIDO initialization operation
data class FidoInitializationResult(
    val success: Boolean,
    val message: String
)


/*
 * IntroViewModel class manages the UI state and business logic for the
 * Intro screen.It handles FIDO initialization, account creation, authentication,
 * and log management.
 */
@HiltViewModel
class IntroViewModel @Inject constructor(application: Application, private val fido: IXUAF, private val prefs: SharedPreferences) : BaseViewModel(application) {

    // Mutable state flow for the UI state
    private val _uiState = MutableStateFlow(
        FidoUiState(
            inProgress = false,
            authArrayAvailable = false,
            authArray = emptyArray<Authenticator>(),
            authSelected = false,
            selectedAuth = null,
            username = null,
            accountArray = emptyArray<AccountInfo>(),
            accountListAvailable = false,
            accountSelected = false,
            selectedAccount = null
            )
    )

    // State flow for the UI state
    val uiState: StateFlow<FidoUiState> = _uiState

    // Initialize FIDO
    fun initFido() {
        // Enable logging
        fido.setLogging(true, LogUtils.LogLevel.VERBOSE, true)
        // Rotate logs if file size is greater than 5KB
        rotateLogs()
        if (fido.isInitialised()) {
            Log.d("DAON", "initFido already initialized")
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(inProgress = true)
            }
            viewModelScope.launch(Dispatchers.Default)  {
                val parameters = Bundle().apply {
                    putString("com.daon.sdk.ignoreNativeClients", "true")
                    putString("com.daon.sdk.ados.enabled", "true")
                    //Enabling ADoS SRP Passcode
                    putString("com.daon.sdk.passcode.ados.version", "2")
                }

                when (val response = fido.initialize(parameters)) {
                    is Success -> {
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                inProgress = false,
                                initializationResult = FidoInitializationResult(
                                    success = true,
                                    message = "Fido init done"
                                )
                            )
                        }
                        response.params.getIntArray("warnings")?.forEach {
                            Log.d("DAON", "warning - $it")
                        }

                    }

                    is Failure -> {
                        _uiState.update { currentUiState ->
                            currentUiState.copy(
                                inProgress = false,
                                initializationResult = FidoInitializationResult(
                                    success = true,
                                    message = "Fido init failes with error code ${response.params.getInt(IXUAF.ERROR_CODE)}"
                                ))
                        }
                        Log.d("DAON", "initFido failure ${response.params.getInt(IXUAF.ERROR_CODE)} : ${response.params.getString(IXUAF.ERROR_MESSAGE)}")
                    }
                }
            }
        }

        fido.addChooseAuthenticatorListener {
            _uiState.update { currentUiState ->
                currentUiState.copy(authArrayAvailable = true, authArray = it)
            }
        }

        fido.addAccountListListener{
            Log.d("DAON", "addAccountListListener : $it")
            _uiState.update { currentUiState ->
                currentUiState.copy(accountListAvailable = true, accountArray = it)
            }
        }
    }

    // Check if file size is greater than 5KB
    private fun isFileSizeGreaterThan5KB(): Boolean {
        val file = File(context.getExternalFilesDir(null), "daon-ixa.log")
        return if (file.exists()) {
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            fileSizeInKB > 5
        } else {
            false
        }
    }

    // Rotate logs if file size is greater than 5KB
    private fun rotateLogs() {
        if (isFileSizeGreaterThan5KB()) {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS-dd-MM-yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())
            fido.rotate(context, "$currentDate-fido-ixa.log")
            deleteLogs()
        }
    }

    // Delete the oldest logs if more than 5 files are present
    private fun deleteLogs() {
        val directory = context.getExternalFilesDir(null)
        val logFileNamePattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}-\\d{2}-\\d{2}-\\d{4}-fido-ixa\\.log")
        val files = directory?.listFiles()
        val logFiles = files?.filter { logFileNamePattern.matcher(it.name).matches() }?.sortedBy { it.lastModified() }
        if (logFiles != null && logFiles.size > 5) {
            for (i in 0 until logFiles.size - 5) {
                fido.delete(context, logFiles[i].name)
            }
        }

    }

    // Start GPS locator
    fun startGps() {
        fido.startLocator(null)
        gpsTimeoutCountdown.cancel()
        gpsTimeoutCountdown.start()
    }

    // Countdown timer for GPS timeout
    private val gpsTimeoutCountdown: CountDownTimer = object : CountDownTimer(60000, 60000) {
        override fun onTick(millisUntilFinished: Long) {
            // No action required.
        }

        override fun onFinish() {
            fido.stopLocator()
        }
    }

    // Create a new account with generated email
     fun createAccount() {
        val usr = generateEmail()
        Log.d("DAON", "createNewAccount usr - $usr")
        _uiState.update { currentUiState ->
            currentUiState.copy(
                inProgress = true)
        }
        viewModelScope.launch {
            val params = Bundle()
            params.putString(IXUAF.FIRSTNAME, "first name")
            params.putString(IXUAF.LASTNAME, "last name")
            params.putString(IXUAF.PASSWORD, "pp")
            when (val response = fido.requestServiceAccess(usr, params)) {
                is Success -> {
                    val editor = prefs.edit()
                    editor.putString("currentUser", usr)
                    editor.apply()
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            username = usr,
                            inProgress = false,
                            accountCreationResult = AccountCreationResult(
                                success = true,
                                message = "Account created successfully",
                            )
                        )
                    }
                }

                is Failure -> {
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            inProgress = false,
                            accountCreationResult = AccountCreationResult(
                                success = false,
                                message = "Account creation failed with " +
                                        "error code ${response.params.getInt(IXUAF.ERROR_CODE)} and " +
                                        "message ${response.params.getString(IXUAF.ERROR_MESSAGE)}",
                            )
                        )
                    }
                }
            }
        }
    }

    // Generate a random email for account creation
    private fun generateEmail(): String {
        val randomString = UUID.randomUUID().toString().substring(0, 15)
        Log.d("DAON", "generateRandomString :$randomString")
        return "$randomString@daon.com"
    }

    // Authenticate the user
    fun authenticate() {
        _uiState.update { currentUiState ->
            currentUiState.copy(inProgress = true)
        }
        viewModelScope.launch(Dispatchers.Default) {
            val bundle = Bundle()
            when (val response = fido.authenticate(bundle)) {

                is Success -> {
                    val username = response.params.getString(IXUAF.EMAIL)
                    val editor = prefs.edit()
                    editor.putString("currentUser", username)
                    editor.apply()
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            inProgress = false,
                            username = response.params.getString(IXUAF.EMAIL) ?: prefs.getString("currentUser", null),
                            loginResult = LoginResult(
                                success = true,
                                message = "Authentication success",
                                code = 0,
                            )
                        )
                    }
                }

                is Failure -> {
                    Log.d("DAON", "IntroScreen authenticate failure " +
                            "${response.params.getInt(IXUAF.ERROR_CODE)} " +
                            "${response.params.getString(IXUAF.ERROR_MESSAGE)} + bundle -${response.params}")
                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            inProgress = false,
                            loginResult = LoginResult(
                                success = false,
                                message = response.params.getString(IXUAF.ERROR_MESSAGE).toString(),
                                code = response.params.getInt(IXUAF.ERROR_CODE),
                            )
                        )
                    }
                }
            }
        }
    }

    // Reset the UI state
    fun resetUiState() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                inProgress = false,
                accountCreationResult = null,
                loginResult = null)
        }
    }

    // Reset the account creation result
    fun resetAccountCreationResult() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                accountCreationResult = null)
        }
    }

    // Reset the login result
    fun resetLoginResult() {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                loginResult = null)
        }
    }

    // Update the selected authenticator
    fun updateSelectedAuth(auth: Authenticator) {
        _uiState.update { currentAuthState ->
            currentAuthState.copy(selectedAuth = auth, authSelected = true, authArrayAvailable = false)
        }
        prefs.edit {
            this.putString("selectedAaid", auth.aaid)
        }
    }

    // Update the selected authenticator
    fun updateSelectedAccount(account: AccountInfo) {
        _uiState.update { currentAuthState ->
            currentAuthState.copy(selectedAccount = account, accountSelected = true, accountListAvailable = false, accountArray = emptyArray<AccountInfo>())
        }
    }

    fun resetAccountListAvailable() {
        _uiState.update { currentAuthState ->
            currentAuthState.copy(accountListAvailable = false, accountArray = emptyArray<AccountInfo>())
        }
    }

    fun submitSelectedAccount() {
        uiState.value.selectedAccount?.let { fido.submitUserSelectedAccount(it) }
        _uiState.update { currentUiState ->
            currentUiState.copy(accountSelected = false)
        }
    }

    // Reset the auth array available flag
    fun resetAuthArrayAvailable() {
        _uiState.update { currentAuthState ->
            currentAuthState.copy(authArrayAvailable = false)
        }
    }

    // Deselect the selected authenticator
    fun deselectAuth() {
        _uiState.update { currentUiState ->
            currentUiState.copy(authSelected = false)
        }
    }

    // Perform silent authentication
    fun authenticateSilent() {
        val silentController = fido.getController(getApplication(), SILENT_AUTH_AAID) as CaptureControllerProtocol
        silentController.startCapture()
        silentController.completeCapture()
    }

    // Cancel the current operation
    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

}