package com.daon.fido.sdk.sample.kt.face
import android.Manifest
import android.content.pm.ActivityInfo
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import com.daon.fido.sdk.sample.kt.intro.IntroViewModel
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.LockScreenOrientation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * UI for the face capture process.
 * @param onNavigateUp: Callback function to handle navigation when face capture is complete.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceScreen(
    onNavigateUp: () -> Unit,
    introViewModel: IntroViewModel? = null,
    onNavigateToAccounts: (() -> Unit, ViewModel) -> Unit
) {

    val viewModel = hiltViewModel<FaceViewModel>()
    val uiState by viewModel.faceUIState.collectAsState()
    val captureState by viewModel.faceCaptureState.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    
    var isAccountChooserVisible by rememberSaveable { mutableStateOf(false) }

    introViewModel?.let {
        LaunchedEffect(key1 = it.uiState) {
            it.uiState.collect { uiState ->
                if (uiState.accountListAvailable) {
                    isAccountChooserVisible = true
                    onNavigateToAccounts(onNavigateUp, it)
                }
                if (uiState.accountSelected) {
                    it.submitSelectedAccount()
                }
            }
        }
    }

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    // Handle lifecycle events to request camera permission
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (permissionState.status.isGranted) {
                    Log.d("DAON", "CAMERA permission granted, do nothing")
                } else {
                    permissionState.launchPermissionRequest()
                }
            }

            if (event == Lifecycle.Event.ON_RESUME) {
                if (!isAccountChooserVisible) {
                    viewModel.startCapture(lifecycleOwner, previewView)
                } else {
                    isAccountChooserVisible = false
                }

            }
            if (event == Lifecycle.Event.ON_STOP) {
                if (!isAccountChooserVisible) {
                    viewModel.resetFaceUIState()
                    viewModel.stopCapture()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })


    // Handle capture state changes
    LaunchedEffect(key1 = captureState) {
        if (captureState.captureComplete) {
            viewModel.resetFaceCaptureState()
            onNavigateUp()
        }
        if (captureState.captureMessage != null) {
            Log.d("DAON", "captureMessage: ${captureState.captureMessage}")
            Toast.makeText(context, captureState.captureMessage, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = uiState.recaptureEnabled) {
        if (uiState.recaptureEnabled) {
            viewModel.onRecapture(lifecycleOwner, previewView)
        }
    }

    // Handle back button press
    BackHandler {
        //Cancel the ongoing fido operation and capture controller
        viewModel.cancelCurrentOperation()
        onNavigateUp()
    }

    // Lock the screen orientation to portrait
    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)


    // Layout for the face capture screen
    ConstraintLayout(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        val (previewLayout, buttonsLayout, infoTextView) = createRefs()

        // Preview layout for camera or captured image
        Box(
            modifier = Modifier
                .constrainAs(previewLayout) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(buttonsLayout.top)
                }
        ) {
            if (uiState.previewImageVisible) {
                uiState.previewImage?.let {
                    val imageBitmap = remember(it) { it.asImageBitmap() }
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Preview Image",
                        modifier = Modifier.padding(25.dp)
                    )
                }
            } else {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Buttons layout for retake and done buttons
        Row(
            modifier = Modifier
                .constrainAs(buttonsLayout) {
                    top.linkTo(previewLayout.bottom)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (uiState.retakePhotoEnabled) {
                Button(
                    onClick = {viewModel.onRecapture()},
                    colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor),
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                ) {
                    Text(text = "Retake", color = Color.White)
                }
            }

            if (uiState.doneButtonEnabled) {
                Button(
                    onClick = {
                        if (uiState.isEnrollment) {
                            viewModel.register()
                        } else {
                            viewModel.authenticate()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor),
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f)
                ) {
                    if (uiState.isEnrollment) {
                        Text(text = "Register", color = Color.White)
                    } else {
                        Text(text = "Authenticate", color = Color.White)
                    }
                }

            }
        }

        // Info text view for displaying messages
        if (uiState.infoTextVisible) {
            Text(
                text = uiState.infoText,
                modifier = Modifier
                    .constrainAs(infoTextView) {
                        top.linkTo(parent.top, margin = 24.dp)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .background(Color.Transparent)
                    .padding(16.dp),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6
            )
        }

        if (uiState.inProgress) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

    }

}