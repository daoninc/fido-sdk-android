package com.daon.fido.sdk.sample.kt.util

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.daon.fido.client.sdk.Group
import com.daon.fido.client.sdk.ui.AuthenticatorSet
import com.daon.fido.client.sdk.ui.PagedUIAuthenticator
import com.daon.sdk.authenticator.Authenticator

/**
 * Composable function to display a circular indeterminate progress bar.
 * @param isDisplayed Boolean value to determine if the progress bar should be displayed.
 */
@Composable
fun CircularIndeterminateProgressBar(isDisplayed: Boolean) {
    if (isDisplayed) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(50.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colors.primary)

        }
    }
}

// Get the ImageBitmap from the base64 encoded string
fun getBitmap(icon: String): ImageBitmap {
    val options = BitmapFactory.Options()
    options.inMutable = true
    val commaIndex = icon.indexOf(',')
    val imageBase64 = icon.substring(commaIndex + 1)
    val imgBytes = Base64.decode(imageBase64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size, options).asImageBitmap()
}


fun getGroupTitle(group: Group): String {
    val authSet: AuthenticatorSet = group.getAuthenticatorSet()
    return (0 until authSet.numberOfFactors)
        .joinToString(", ") { index -> authSet.getAuthenticatorInfo(index).metadata.title }
}

fun getGroupDescription(group: Group): String {
    val authSet: AuthenticatorSet = group.getAuthenticatorSet()
    return (0 until authSet.numberOfFactors)
        .joinToString(", ") { index -> authSet.getAuthenticatorInfo(index).metadata.description }
}
