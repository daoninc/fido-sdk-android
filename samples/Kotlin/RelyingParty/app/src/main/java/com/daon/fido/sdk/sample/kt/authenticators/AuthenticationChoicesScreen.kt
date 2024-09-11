package com.daon.fido.sdk.sample.kt.authenticators


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.daon.fido.client.sdk.model.Authenticator
import com.daon.fido.sdk.sample.kt.intro.IntroViewModel

/**
 *
 * Displays a list of authenticators for the user to select for authentication. It handles
 * user interactions for selecting an authenticator and navigating back.
 */
@Composable
fun AuthenticationChoicesScreen(onNavigateUp: () -> Unit, viewModel: IntroViewModel) {
    // Collect the AuthenticatorState from the ViewModel which includes the list of authenticators
    val state = viewModel.uiState.collectAsState()
    val authList: List<Authenticator> = state.value.authArray.toList()

    // Screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                title = { Text("Select the authenticator to login")}
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = it
        ) {
            items(authList) { auth ->
                AuthCard(authenticator = auth, onNavigateUp, viewModel)

            }
        }
    }

    // Handle back button press
    BackHandler(true) {
        viewModel.resetAuthArrayAvailable()
        viewModel.cancelCurrentOperation()
        onNavigateUp()
    }
}

@Composable
fun AuthCard(authenticator: Authenticator, onNavigateUp: () -> Unit, viewModel: IntroViewModel) {
    // Card layout for each authenticator item
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                // Update the selected authenticator and navigate back
                viewModel.updateSelectedAuth(authenticator)
                onNavigateUp()
            },
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the authenticator icon if available
            getBitmap(authenticator.icon)?.let { Image(
                bitmap = it,
                contentDescription = " ",
            modifier = Modifier
                .size(60.dp)
                .padding(6.dp),
            contentScale = ContentScale.Fit) }
            // Display the authenticator title and description
            Column(Modifier.padding(8.dp)) {
                Text(
                   text = authenticator.title,
                   style = MaterialTheme.typography.h6,
                   color = MaterialTheme.colors.onSurface
                )
                Text (
                    text = authenticator.description ,
                    style = MaterialTheme.typography.body2
                )
            }

        }
    }
}