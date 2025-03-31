package com.daon.fido.sdk.sample.kt.fingerprint

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.daon.fido.sdk.sample.kt.R
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol

/**
 * FingerprintScreen handles the UI for the fingerprint capture process.
 */
@Composable
fun FingerprintScreen(
    onNavigateUp: () -> Unit,
    fingerprintController: FingerprintCaptureControllerProtocol
) {
    val fingerprintViewModel = hiltViewModel<FingerprintViewModel>()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    //Handle fingerprint capture start and stop
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fingerprintViewModel.onStart(context, fingerprintController)
            } else if (event == Lifecycle.Event.ON_STOP) {
                fingerprintViewModel.onStop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val activity = context as Activity
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    })

    //Collect the captureInfo state and display a toast message if it is not empty
    val captureInfo = fingerprintViewModel.captureInfo.collectAsState()
    if (captureInfo.value.isNotEmpty()) {
        Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
        fingerprintViewModel.resetCaptureInfo()
    }

    //Handle fingerprint capture completion
    val captureComplete = fingerprintViewModel.captureComplete.collectAsState()
    if (captureComplete.value) {
        fingerprintViewModel.resetCaptureComplete()
        onNavigateUp()
    }

    // Handle back button press
    BackHandler {
        //Cancel the ongoing fido operation and capture controller
        fingerprintViewModel.cancelCurrentOperation()
        onNavigateUp()
    }

    // fingerprint capture UI
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(id = R.drawable.bg_fingerprint),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "Please touch sensor when ready",
            color = ButtonColor,
            style = MaterialTheme.typography.h1,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(10.dp)
        )

    }

}