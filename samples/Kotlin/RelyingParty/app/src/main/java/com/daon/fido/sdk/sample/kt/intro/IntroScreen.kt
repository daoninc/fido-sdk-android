package com.daon.fido.sdk.sample.kt.intro

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import com.daon.fido.sdk.sample.kt.model.ADOS_FACE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.FACE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.FINGERPRINT_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.PASSCODE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.SILENT_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.SRP_PASSCODE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.CircularIndeterminateProgressBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/*
 * This file defines the `IntroScreen` composable, which is the initial screen of the application.
 * It handles user interactions for logging in and creating an account, and manages
 * navigation to other screens.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IntroScreen(
    onNavigateToHome: (username: String) -> Unit,
    onNavigateToChooseAuth: (() -> Unit, ViewModel) -> Unit,
    onNavigateToAccounts: (() -> Unit, ViewModel) -> Unit,
    onNavigateToPasscode: () -> Unit,
    onNavigateToFace: (ViewModel) -> Unit,
    onNavigateToFingerprint: () -> Unit,
    onNavigateUp: () -> Unit
) {
    // ViewModel for the IntroScreen
    val viewModel = hiltViewModel<IntroViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Basic permissions required for the app to function
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )
    )


    // Initialize FIDO and request permissions when the app is started
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.initFido()
                if (!permissionStates.allPermissionsGranted) {
                    permissionStates.launchMultiplePermissionRequest()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    // Start GPS when all permissions are granted
    LaunchedEffect(key1 = permissionStates.allPermissionsGranted) {
        if (permissionStates.allPermissionsGranted) {
            viewModel.startGps()
        }
    }

    // Observe the UI state
    LaunchedEffect(key1 = viewModel.uiState) {
        viewModel.uiState.collect { uiState ->

            // Handle account creation result
            uiState.accountCreationResult?.let { accountCreationResult ->
                if (accountCreationResult.success) {
                    Log.d("DAON", "accountCreationResult success")
                    Toast.makeText(
                        context, "Account created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateToHome(uiState.username.toString())
                } else {
                    Toast.makeText(
                        context, "Account creation failed ${accountCreationResult.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.resetAccountCreationResult()

            }

            // Handle authentication choices
            if (uiState.authArrayAvailable) {
                onNavigateToChooseAuth(onNavigateUp, viewModel)
            }

            // Handle account selection for non-ADoS authentication
            if (uiState.accountListAvailable) {
                onNavigateToAccounts(onNavigateUp, viewModel)
            }


            if (uiState.accountSelected) {
                viewModel.submitSelectedAccount()
            }

            // Handle authentication selection
            if (uiState.authSelected) {
                viewModel.deselectAuth()
                val aaid = uiState.selectedAuth?.aaid
                when (uiState.selectedAuth?.aaid) {

                    SRP_PASSCODE_AUTH_AAID, PASSCODE_AUTH_AAID -> {
                        if (aaid != null) {
                            onNavigateToPasscode()
                        }
                    }
                    FINGERPRINT_AUTH_AAID -> {
                        onNavigateToFingerprint()
                    }
                    FACE_AUTH_AAID, ADOS_FACE_AUTH_AAID -> {
                        if (aaid != null) {
                            onNavigateToFace(viewModel)
                        }
                    }
                    SILENT_AUTH_AAID -> {
                        viewModel.authenticateSilent()
                    }
                }
            }

            // Handle login result
            uiState.loginResult?.let { loginResult ->
                if (loginResult.success) {
                    Toast.makeText(
                        context, "Authentication success",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateToHome(uiState.username.toString())
                } else {
                    Toast.makeText(
                        context,
                        "Authentication failed ${loginResult.code} :  ${loginResult.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.resetLoginResult()
            }

        }
    }

    // Reset the UI state when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetUiState()
        }
    }

    // Layout for the IntroScreen
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "IdentityX FIDO Sample",
            fontSize = 20.sp,
            color = ButtonColor
        )
        Spacer(modifier = Modifier.height(height = 50.dp))
        LoginButton(viewModel = viewModel)
        CircularIndeterminateProgressBar(isDisplayed = uiState.inProgress)
        CreateAccountButton(viewModel = viewModel)
    }
}



@Composable
fun LoginButton(viewModel: IntroViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // Display the login button if not in progress
    if (!uiState.inProgress) {
        Button(
            onClick = {
                viewModel.resetUiState()
                viewModel.authenticate()
            },
            shape = CutCornerShape(10),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(text = "Log in with Fido")
        }
    }
}

@Composable
fun CreateAccountButton(viewModel: IntroViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // Display the create account button if not in progress
    if (!uiState.inProgress) {
        Button(
            onClick = {
                Log.d("DAON", "CreateAccount onClick")
                viewModel.createAccount()
            },
            shape = CutCornerShape(10),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(text = "Create Account")
        }
    }
}
