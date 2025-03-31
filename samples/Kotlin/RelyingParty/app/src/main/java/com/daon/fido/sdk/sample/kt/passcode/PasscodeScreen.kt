package com.daon.fido.sdk.sample.kt.passcode

import androidx.compose.runtime.Composable
import com.daon.sdk.authenticator.controller.ControllerConfiguration
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol

/**
 * UI for the passcode capture process.
 * @param onNavigateUp: Callback function to handle navigation when passcode capture is complete.
 * @param passcodeController: The passcode controller to use for the passcode capture process.
 */
@Composable
fun PasscodeScreen(
    onNavigateUp: () -> Unit,
    passcodeController: PasscodeControllerProtocol
) {

    when {
        passcodeController.isEnrol -> {
            // Display the registration screen if in enrollment mode
            RegisterPasscodeScreen (
                onNavigateUp,
                passcodeController = passcodeController)
        }
        passcodeController.authenticationMode == ControllerConfiguration.AuthenticationMode.VERIFY_AND_REENROL -> {
            // Display the verify and re-enroll screen if in verify and re-enroll mode
            VerifyAndReenrolPasscodeScreen (
                onNavigateUp,
                passcodeController = passcodeController
            )
        }
        else -> {
            // Display the authentication screen if in authentication mode
            AuthenticatePasscodeScreen (
                onNavigateUp,
                passcodeController = passcodeController
            )
        }
    }

}