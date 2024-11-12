package com.daon.fido.sdk.sample.kt.passcode

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * UI for the passcode capture process.
 * @param onNavigateUp: Callback function to handle navigation when passcode capture is complete.
 */
@Composable
fun PasscodeScreen(
    onNavigateUp: () -> Unit
) {
    val passcodeViewModel = hiltViewModel<PasscodeViewModel>()
    val controllerInstantiated = passcodeViewModel.controllerInstantiated.collectAsState()
    val isEnrol = passcodeViewModel.isEnrol.collectAsState()
    val isVerifyAndEnroll = passcodeViewModel.isVerifyAndEnroll.collectAsState()
    val captureInfo = passcodeViewModel.captureInfo.collectAsState()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                passcodeViewModel.intializeController()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })


    //Handle controller instantiation error
    LaunchedEffect(key1 = passcodeViewModel.onControllerInstantiateError) {
        passcodeViewModel.onControllerInstantiateError.collect { controllerInstantiatedError ->
            if (controllerInstantiatedError) {
                passcodeViewModel.resetControllerInstantiatedError()
                Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
                onNavigateUp()
            }

        }

    }


    // Navigate to the corresponding screen based on the state.
    if (controllerInstantiated.value) {
        if (isEnrol.value) {
            // Display the registration screen if in enrollment mode
            RegisterPasscodeScreen {
                onNavigateUp()
            }
        } else {
            // Display the verify and re-enroll screen if in verify and re-enroll mode
            if (isVerifyAndEnroll.value) {
                VerifyAndReenrolPasscodeScreen {
                    onNavigateUp()
                }
            } else {
                // Display the authentication screen if in authentication mode
                AuthenticatePasscodeScreen {
                    onNavigateUp()
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Initializing controller, please wait ...")
        }
    }

}