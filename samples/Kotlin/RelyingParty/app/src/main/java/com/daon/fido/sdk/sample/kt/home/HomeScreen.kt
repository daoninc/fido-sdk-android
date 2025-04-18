package com.daon.fido.sdk.sample.kt.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.daon.fido.client.sdk.Group
import com.daon.fido.sdk.sample.kt.R
import com.daon.fido.sdk.sample.kt.model.*
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.CircularIndeterminateProgressBar

/*
 * `HomeScreen` is the landing screen after user login.
 *  User can initiate step-up authentication, navigate to registration screen and perform a reset
 *  from this screen.
 */
@Composable
fun HomeScreen(
    user: String,
    sessionId: String,
    onNavigateToRegistration: (sessionId: String) -> Unit,
    backToIntro: () -> Unit,
    onNavigateToChooseAuth: (() -> Unit, ViewModel) -> Unit,
    onNavigateToPasscode: () -> Unit,
    onNavigateToFace: () -> Unit,
    onNavigateToFingerprint: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToTransactionConfirmation: (() -> Unit, ViewModel) -> Unit
) {
    // ViewModel for the Home screen
    val viewModel = hiltViewModel<HomeViewModel>()
    val context = LocalContext.current

    // Collect the inProgress state from the ViewModel
    val inProgress = viewModel.inProgress.collectAsState()

    // Observe the transaction state from the ViewModel
    LaunchedEffect(key1 = viewModel.transactionState) {
        viewModel.transactionState.collect { transactionState ->
            // Handle the authentication choices
            if (transactionState.policyAvailable) {
                // For multi-factor authentication, if an authenticator group is already selected, navigate to the next authenticator.
                // For non-multi-factor operations, isUpdate is always false.
                val isUpdate = transactionState.policy?.isUpdate()
                if (isUpdate == true) {
                    val groups: Array<Group>? = transactionState.policy.getGroups()
                    groups?.let {groups ->
                        if (groups.isNotEmpty()) {
                            viewModel.updateSelectedGroup(groups[0])
                        }
                    }
                } else {
                    onNavigateToChooseAuth(onNavigateUp, viewModel)
                }
            }

            // Handle the authentication selection
            if (transactionState.groupSelected) {
                viewModel.deselectAuth()
                when (transactionState.group?.getAuthenticator()?.aaid) {
                    SRP_PASSCODE_AUTH_AAID, PASSCODE_AUTH_AAID -> {
                        val passcodeController  = viewModel.getPasscodeController()
                        if (passcodeController != null) {
                            onNavigateToPasscode()
                        } else {
                            viewModel.cancelCurrentOperation()
                            Toast.makeText(context, "Passcode controller is not available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    FINGERPRINT_AUTH_AAID -> {
                        val fingerprintController = viewModel.getFingerprintController()
                        if (fingerprintController != null) {
                            onNavigateToFingerprint()
                        } else {
                            viewModel.cancelCurrentOperation()
                            Toast.makeText(context, "Fingerprint controller is not available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    FACE_AUTH_AAID, ADOS_FACE_AUTH_AAID -> {
                        val faceController = viewModel.getFaceController()
                        if (faceController != null) {
                            onNavigateToFace()
                        } else {
                            viewModel.cancelCurrentOperation()
                            Toast.makeText(context, "Face controller is not available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    SILENT_AUTH_AAID -> {
                        viewModel.authenticateSilent()
                    }
                }
            }

            // Handle the authentication completion
            if (transactionState.authenticationCompleted) {
                Toast.makeText(context, transactionState.message, Toast.LENGTH_SHORT).show()
                viewModel.resetAuthenticationCompleted()
            }

            // Handle reset completion
            if (transactionState.resetComplete) {
                viewModel.resetResetComplete()
                backToIntro()
            }

            // Handle transaction confirmation
            if (transactionState.confirmTransaction) {
                onNavigateToTransactionConfirmation(onNavigateUp, viewModel)
            }

            // Submit the transaction confirmation result
            if (transactionState.transactionConfirmationResult != 99) {
                viewModel.submitDisplayTransactionResult(transactionState.transactionConfirmationResult)
            }

            // Handle the confirmation OTP
            if (transactionState.confirmationOTPReceived) {
                Toast.makeText(
                    context,
                    "Your One-Time Password - ${transactionState.confirmationOTP}",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetConfirmationOTP()
            }

        }
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        viewModel.setSessionId(sessionId)
        onDispose {
            viewModel.onStop()
        }
    }

    // Home screen layout
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            text = "Welcome", fontSize = 20.sp, fontWeight = FontWeight.W600 , color = ButtonColor
        )
        Text(
            text = "$user", fontSize = 20.sp, color = ButtonColor
        )

        Spacer(modifier = Modifier.height(height = 50.dp))
        CircularIndeterminateProgressBar(
            isDisplayed = inProgress.value
        )
        Button(
            onClick = {
                viewModel.authenticate(false)
            }, shape = CutCornerShape(10), colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor, contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.action_step_up_auth))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Button(
            onClick = {
                viewModel.authenticate(true)
            }, shape = CutCornerShape(10), colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor, contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.action_single_shot))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Button(
            onClick = {
                onNavigateToRegistration(sessionId)
            }, shape = CutCornerShape(10), colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor, contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.action_manage_authenticators))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Button(
            onClick = {
                viewModel.resetFido()
            }, shape = CutCornerShape(10), colors = ButtonDefaults.buttonColors(
                backgroundColor = ButtonColor, contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.action_delete))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
    }

    // Handle the back press
    BackHandler(enabled = true, onBack = {
        viewModel.doLogout()
        backToIntro()
    })
}