package com.daon.fido.sdk.sample.kt.passcode

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * UI for the passcode registration process.
 * @param onNavigateUp: Callback function to handle navigation when passcode registration is complete.
 */
@Composable
fun RegisterPasscodeScreen(
    onNavigateUp: () -> Unit ) {

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val registerPasscodeViewModel = hiltViewModel<PasscodeViewModel>()

    var textValue1 by remember { mutableStateOf(TextFieldValue("")) }
    var textValue2 by remember { mutableStateOf(TextFieldValue("")) }

    // Starting the ViewModel when the composable enters the composition
    DisposableEffect(key1 = registerPasscodeViewModel ) {
        registerPasscodeViewModel.onStart()
        onDispose { registerPasscodeViewModel.onStop() }
    }

    // Collecting the capture complete state
    val captureComplete = registerPasscodeViewModel.captureComplete.collectAsState()
    if (captureComplete.value) {
        registerPasscodeViewModel.resetCaptureComplete()
        onNavigateUp()
    }

    // Collecting the capture info state and displaying a toast message if it is not empty
    val captureInfo = registerPasscodeViewModel.captureInfo.collectAsState()
    if (captureInfo.value.isNotEmpty()) {
        Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
        registerPasscodeViewModel.resetCaptureInfo()
    }

    // Collecting the enableRetry state
    val enableRetry = registerPasscodeViewModel.enableRetry.collectAsState()
    if (enableRetry.value) {
        // Reset the text fields and stay on the same screen
        textValue1 = TextFieldValue("")
        textValue2 = TextFieldValue("")
        registerPasscodeViewModel.resetEnableRetry()
    }

    // Collecting the capture complete with error state
    val captureCompleteWithError = registerPasscodeViewModel.captureCompleteWithError.collectAsState()
    if (captureCompleteWithError.value) {
        // Show a toast message and navigate back
        Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
        registerPasscodeViewModel.resetCaptureCompleteWithError()
        onNavigateUp()

    }

    // Handle back button press
    BackHandler(true) {
        //Cancel the ongoing fido operation and capture controller
        registerPasscodeViewModel.cancelCurrentOperation()
        onNavigateUp()
    }

    // Passcode registration UI
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text (
            text = "Choose a passcode to be used when authenticating",
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            label = { Text("Passcode")},
            value = textValue1,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onValueChange = {
                textValue1 = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.NumberPassword
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })

        )
        OutlinedTextField(
            label = { Text("Confirm")},
            value = textValue2,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onValueChange = {
                textValue2 = it
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.NumberPassword
            ),
            keyboardActions = KeyboardActions(onNext = {
                //focusManager.moveFocus(FocusDirection.Down)
                focusManager.clearFocus()
                if (textValue1.text != textValue2.text) {
                    textValue1 = TextFieldValue("")
                    textValue2 = TextFieldValue("")
                   Toast.makeText(context, "Passcodes do not match", Toast.LENGTH_SHORT).show()
                } else if (textValue1.text.isEmpty()) {
                    textValue1 = TextFieldValue("")
                    textValue2 = TextFieldValue("")
                    Toast.makeText(context, "Passcode cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    registerPasscodeViewModel.register(textValue1.text)
                }
            })
        )
    }
}
