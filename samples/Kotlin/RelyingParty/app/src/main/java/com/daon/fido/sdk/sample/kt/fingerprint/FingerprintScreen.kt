package com.daon.fido.sdk.sample.kt.fingerprint

import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.daon.fido.sdk.sample.kt.R
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.LockScreenOrientation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

/**
 * FingerprintScreen handles the UI for the fingerprint capture process.
 */
@Composable
fun FingerprintScreen(
    onNavigateUp: () -> Unit
) {

    val scope = rememberCoroutineScope()
    val fingerprintViewModel = hiltViewModel<FingerprintViewModel>()

    val context = LocalContext.current

    //Handle fingerprint capture completion
    LaunchedEffect(key1 = fingerprintViewModel.captureComplete) {
        fingerprintViewModel.captureComplete.collect { captureComplete ->
            if (captureComplete) {
                fingerprintViewModel.resetCaptureComplete()
                onNavigateUp()
            }

        }
    }

    //Handle fingerprint capture start and stop
    DisposableEffect(key1 = fingerprintViewModel ) {
        scope.launch {
            delay(100)
            fingerprintViewModel.onStart(context)
        }
        onDispose {
            fingerprintViewModel.onStop()
        }
    }

    //Collect the captureInfo state and display a toast message if it is not empty
    val captureInfo = fingerprintViewModel.captureInfo.collectAsState()
    if (captureInfo.value.isNotEmpty()) {
        Toast.makeText(context, captureInfo.value, Toast.LENGTH_SHORT).show()
        fingerprintViewModel.resetCaptureInfo()
    }

    //Lock the screen orientation to portrait
    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

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