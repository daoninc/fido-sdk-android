package com.daon.fido.sdk.sample.kt.passcode

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * UI for the passcode verification and re-enrollment process.
 * @param onNavigateUp: Callback function to handle navigation when passcode
 * verification and re-enrollment is complete.
 */
@Composable
fun VerifyAndReenrolPasscodeScreen(
    onNavigateUp: () -> Unit ) {

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val verifyAndReenrolPasscodeViewModel = hiltViewModel<PasscodeViewModel>()

    var currentPasscode by remember { mutableStateOf(TextFieldValue("")) }
    var newPasscode by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPasscode by remember { mutableStateOf(TextFieldValue("")) }

    // Starting the ViewModel when the composable enters the composition
    DisposableEffect(key1 = verifyAndReenrolPasscodeViewModel ) {
        verifyAndReenrolPasscodeViewModel.onStart()
        onDispose { verifyAndReenrolPasscodeViewModel.onStop() }
    }

    // Collecting the capture complete state
    val captureComplete = verifyAndReenrolPasscodeViewModel.captureComplete.collectAsState()
    if (captureComplete.value) {
        verifyAndReenrolPasscodeViewModel.resetCaptureComplete()
        onNavigateUp()
    }

    // Collecting the capture info state and displaying a toast message if it is not empty.
    val captureInfo = verifyAndReenrolPasscodeViewModel.captureInfo.collectAsState()
    if (captureInfo.value.isNotEmpty()) {
        Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
        verifyAndReenrolPasscodeViewModel.resetCaptureInfo()
    }

    // Collecting the enableRetry state
    val enableRetry = verifyAndReenrolPasscodeViewModel.enableRetry.collectAsState()
    if (enableRetry.value) {
        // Reset the text fields and stay on the same screen
        currentPasscode = TextFieldValue("")
        newPasscode = TextFieldValue("")
        confirmPasscode = TextFieldValue("")
        verifyAndReenrolPasscodeViewModel.resetEnableRetry()
    }

    // Collecting the capture complete with error state
    val captureCompleteWithError = verifyAndReenrolPasscodeViewModel.captureCompleteWithError.collectAsState()
    if (captureCompleteWithError.value) {
        // Show a toast message and navigate up
        Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
        verifyAndReenrolPasscodeViewModel.resetCaptureCompleteWithError()
        onNavigateUp()

    }

    // Handle back button press
    BackHandler(true) {
        //Cancel the ongoing fido operation and capture controller
        verifyAndReenrolPasscodeViewModel.cancelCurrentOperation()
        onNavigateUp()
    }

    // Passcode verification and re-enrollment UI
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text (
                "Passcode" ,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(height = 50.dp))
        Text (
            "Enter current passcode"
        )

        // Current passcode text field
        OutlinedTextField(
            label = { Text("Current passcode")},
            value = currentPasscode,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onValueChange = {
                currentPasscode = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.NumberPassword
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })

        )
        Text (
            "Enter new passcode"
        )

        // New passcode text field
        OutlinedTextField(
            label = { Text("New passcode")},
            value = newPasscode,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onValueChange = {
                newPasscode = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.NumberPassword
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })

        )
        Text (
            "Confirm new passcode"
        )

        // Confirm passcode text field
        OutlinedTextField(
            label = { Text("Confirm passcode")},
            value = confirmPasscode,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onValueChange = {
                confirmPasscode = it
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.NumberPassword
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.clearFocus()
                if (newPasscode.text != confirmPasscode.text) {
                    newPasscode = TextFieldValue("")
                    confirmPasscode = TextFieldValue("")
                   Toast.makeText(context, "Passcodes do not match", Toast.LENGTH_SHORT).show()
                } else if (currentPasscode.text.isEmpty()) {
                    currentPasscode = TextFieldValue("")
                    Toast.makeText(context, "Passcode cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    verifyAndReenrolPasscodeViewModel.verifyAndReenrol(currentPasscode.text, newPasscode.text)
                }
            })
        )
    }
}
