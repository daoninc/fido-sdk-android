package com.daon.fido.sdk.sample.kt.face
import android.Manifest
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.daon.fido.sdk.sample.kt.intro.IntroViewModel
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * UI for the face capture process.
 * @param onNavigateUp: Callback function to handle navigation when face capture is complete.
 * @param introViewModel: The intro view model to use for navigation to accounts screen for ADoS authenticators.
 * @param onNavigateToAccounts: Callback function to navigate to accounts screen.
 * @param faceController: The face controller to use for the face capture process.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceScreen(
    onNavigateUp: () -> Unit,
    introViewModel: IntroViewModel? = null,
    onNavigateToAccounts: (() -> Unit, ViewModel) -> Unit,
    faceController: FaceControllerProtocol
) {

    val viewModel = hiltViewModel<FaceViewModel>()
    val uiState by viewModel.faceUIState.collectAsState()
    val captureState by viewModel.faceCaptureState.collectAsState()

    val context = LocalContext.current
    val activity = requireNotNull(LocalActivity.current)
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val previewView = remember(context) { PreviewView(context) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    DisposableEffect(activity) {
        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }

    introViewModel?.let {
        LaunchedEffect(key1 = it.uiState) {
            it.uiState.collect { uiState ->
                if (uiState.accountListAvailable) {
                    onNavigateToAccounts(onNavigateUp, it)
                }
            }
        }
    }

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    // Handle lifecycle events to request camera permission
    DisposableEffect(lifecycleOwner, permissionState) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (permissionState.status.isGranted) {
                    Log.d("DAON", "CAMERA permission granted, do nothing")
                } else {
                    permissionState.launchPermissionRequest()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionGranted = permissionState.status.isGranted
    DisposableEffect(lifecycleOwner, previewView, faceController, viewModel, permissionGranted) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && permissionGranted) {
                viewModel.startCapture(faceController, lifecycleOwner, previewView)
            }
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopCapture()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopCapture()
        }
    }

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

    LaunchedEffect(
        uiState.recaptureEnabled, permissionGranted, lifecycleState, lifecycleOwner, faceController, previewView
    ) {
        if (uiState.recaptureEnabled && permissionGranted && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.onRecapture(faceController, lifecycleOwner, previewView)
        }
    }

    // Handle back button press
    BackHandler {
        //Cancel the ongoing fido operation and capture controller
        viewModel.cancelCurrentOperation()
        onNavigateUp()
    }

    // Layout for the face capture screen
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        val (previewLayout, buttonsLayout, infoTextView) = createRefs()

        // Preview layout for camera or captured image
        Box(
            modifier = Modifier
                .constrainAs(previewLayout) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    if (isLandscape) {
                        end.linkTo(buttonsLayout.start)
                        bottom.linkTo(parent.bottom)
                    } else {
                        end.linkTo(parent.end)
                        bottom.linkTo(buttonsLayout.top)
                    }
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        ) {
            if (uiState.previewImageVisible) {
                uiState.previewImage?.let {
                    val imageBitmap = remember(it) { it.asImageBitmap() }
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Preview Image",
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }
            } else {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val ovalHeight = (size.height * 0.8f).coerceAtMost(size.width * 1.2f)
                    val ovalWidth = ovalHeight * 2f / 3f
                    // Add a rectangle that covers the entire canvas
                    val path = Path().apply {
                        addRect(Rect(0f, 0f, size.width, size.height))
                        // Subtract an oval from the center of the rectangle
                        op(
                            this,
                            Path().apply {
                                addOval(
                                    Rect(
                                        left = (size.width - ovalWidth) / 2f,
                                        top = (size.height - ovalHeight) / 2f,
                                        right = (size.width + ovalWidth) / 2f,
                                        bottom = (size.height + ovalHeight) / 2f
                                    )
                                )
                            },
                            PathOperation.Difference
                        )
                    }
                    // Draw the resulting path
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Buttons layout for retake and done buttons
        Column(
            modifier = Modifier
                .constrainAs(buttonsLayout) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    if (isLandscape) {
                        top.linkTo(parent.top)
                        start.linkTo(previewLayout.end)
                        width = Dimension.percent(0.35f)
                        height = Dimension.fillToConstraints
                    } else {
                        start.linkTo(parent.start)
                        width = Dimension.fillToConstraints
                    }
                }
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (uiState.retakePhotoEnabled) {
                    Button(
                        onClick = { viewModel.onRecapture() },
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
        }

        // Info text view for displaying messages
        if (uiState.infoTextVisible) {
            Text(
                text = uiState.infoText,
                modifier = Modifier
                    .constrainAs(infoTextView) {
                        if (isLandscape) {
                            top.linkTo(buttonsLayout.top)
                            bottom.linkTo(buttonsLayout.bottom)
                            start.linkTo(buttonsLayout.start)
                            end.linkTo(buttonsLayout.end)
                        } else {
                            top.linkTo(previewLayout.top, margin = 24.dp)
                            bottom.linkTo(previewLayout.bottom)
                            start.linkTo(previewLayout.start)
                            end.linkTo(previewLayout.end)
                        }
                        width = Dimension.fillToConstraints
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
