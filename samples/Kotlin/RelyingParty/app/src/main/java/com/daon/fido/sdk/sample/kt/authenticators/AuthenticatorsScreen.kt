package com.daon.fido.sdk.sample.kt.authenticators

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.daon.fido.client.sdk.Group
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.sdk.sample.kt.model.ADOS_FACE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.FACE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.FINGERPRINT_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.PASSCODE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.SILENT_AUTH_AAID
import com.daon.fido.sdk.sample.kt.model.SRP_PASSCODE_AUTH_AAID
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.CircularIndeterminateProgressBar
import com.daon.fido.sdk.sample.kt.util.getBitmap

/*
 * Displays a list of registered authenticators .
 * It also handles user interactions for registering and deregistering authenticators.
 */
@Composable
fun AuthenticatorsScreen(
    sessionId: String,
    onNavigateToChooseAuth: (() -> Unit, ViewModel) -> Unit,
    onNavigateToPasscode: () -> Unit,
    onNavigateToFace: () -> Unit,
    onNavigateToFingerprint: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val viewModel = hiltViewModel<AuthenticatorsViewModel>()
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current
    val state = viewModel.authState.collectAsState()
    val authToDeregister = state.value.authToDeregister
    var inProgress by remember { mutableStateOf(false) }

    // Observe the state changes
    LaunchedEffect(key1 = viewModel.authState) {
        viewModel.authState.collect { authState ->
            if (authState.policyAvailable) {
                // For multi-factor registration, if an authenticator group is already selected, navigate to the next authenticator.
                // For non-multi-factor operations, isUpdate is always false.
                val isUpdate = authState.policy?.isUpdate()
                if (isUpdate == true) {
                    val groups: Array<Group>? = authState.policy.getGroups()
                    groups.let { groups ->
                        if (!groups.isNullOrEmpty()) {
                            viewModel.updateSelectedGroup(groups[0])
                        }
                    }
                } else {
                    onNavigateToChooseAuth(onNavigateUp, viewModel)
                }
            }

            selectedIndex = authState.selectedIndex

            // Navigate to the respective authenticator screen based on the selected authenticator
            if (authState.groupSelected) {
                when (authState.group?.getAuthenticator()?.aaid) {
                    SRP_PASSCODE_AUTH_AAID, PASSCODE_AUTH_AAID -> {
                        val passcodeController = viewModel.getPasscodeController()
                        if (passcodeController != null) {
                            onNavigateToPasscode()
                        } else {
                            //Not able to proceed with registration, so cancelling the current operation
                            viewModel.cancelCurrentOperation()
                            viewModel.resetInProgress()
                            Toast.makeText(
                                context,
                                "Passcode controller is not available.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    FINGERPRINT_AUTH_AAID -> {
                        val fingerprintController = viewModel.getFingerprintController()
                        if (fingerprintController != null) {
                            onNavigateToFingerprint()
                        } else {
                            //Not able to proceed with registration, so cancelling the current operation
                            viewModel.cancelCurrentOperation()
                            viewModel.resetInProgress()
                            Toast.makeText(
                                context,
                                "Fingerprint controller is not available.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    FACE_AUTH_AAID, ADOS_FACE_AUTH_AAID -> {
                        val faceController = viewModel.getFaceController()
                        if (faceController != null) {
                            onNavigateToFace()
                        } else {
                            //Not able to proceed with registration, so cancelling the current operation
                            viewModel.cancelCurrentOperation()
                            viewModel.resetInProgress()
                            Toast.makeText(
                                context,
                                "Face controller is not available.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    SILENT_AUTH_AAID -> {
                        viewModel.registerSilent()
                    }

                }
                viewModel.deselectAuth()

            }

            // Show a toast message for the registration and deregistration results
            authState.registrationResult?.let { registrationResult ->
                Toast.makeText(
                    context, registrationResult.message, Toast.LENGTH_SHORT
                ).show()
                viewModel.resetRegistrationResult()
            }

            authState.deregistrationResult?.let { deregistrationResult ->
                Toast.makeText(
                    context, deregistrationResult.message, Toast.LENGTH_SHORT
                ).show()
                viewModel.resetDeregistrationResult()
            }

            inProgress = authState.inProgress
        }
    }

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        viewModel.setSessionId(sessionId)
        onDispose {
            viewModel.onStop()
        }
    }

    // Screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                title = { Text("Registered authenticators")}
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AuthenticatorList(viewModel, selectedIndex)
                CircularIndeterminateProgressBar(isDisplayed = inProgress)
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.register()
                        }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = ButtonColor, contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Register", style = TextStyle(fontSize = 15.sp)
                        )

                    }

                    Button(
                        onClick = {
                            if (authToDeregister != null) {
                                viewModel.deregister(viewModel.authState.value.authToDeregister!!)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please select one authenticator from the list.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = ButtonColor, contentColor = Color.White
                        ), enabled = authToDeregister != null

                    ) {
                        Text(
                            text = "Deregister", style = TextStyle(fontSize = 15.sp)
                        )
                    }
                }
            }
        }

    }

    // Handle back button press
    BackHandler(true) {
        onNavigateUp()
    }
}

// List of registered authenticators
@Composable
fun AuthenticatorList(viewModel: AuthenticatorsViewModel, selectedIndex: Int) {
    Log.d("DAON", "AuthenticatorList ${viewModel.discoverList.size}")

    // Handle the item click event
    val onItemClick = { index: Int ->
        Log.d("DAON", "onItemClick $index")
        viewModel.updateAuthToDeregister(viewModel.discoverList[index], index)
    }
    LazyColumn(modifier = Modifier.padding(10.dp)) {
        itemsIndexed(viewModel.discoverList) { index, item: Authenticator ->
            AuthenticatorInfoCard(
                authenticator = item,
                index = index,
                selected = selectedIndex == index,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun AuthenticatorInfoCard(
    authenticator: Authenticator,
    index: Int,
    selected: Boolean,
    onItemClick: (Int) -> Unit
) {
    // Card layout for each authenticator item
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(if (selected) Color.LightGray else MaterialTheme.colors.background)
            .clickable {
                onItemClick(index)
            },
        backgroundColor = (if (selected) Color.LightGray else MaterialTheme.colors.background),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the authenticator icon if available
            Image(
                bitmap = getBitmap(authenticator.icon),
                contentDescription = " ",
                modifier = Modifier
                    .size(60.dp)
                    .padding(6.dp),
                contentScale = ContentScale.Fit
            )
            // Display the authenticator title and description
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = authenticator.title,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = authenticator.description, style = MaterialTheme.typography.body2
                )
            }

        }
    }
}

