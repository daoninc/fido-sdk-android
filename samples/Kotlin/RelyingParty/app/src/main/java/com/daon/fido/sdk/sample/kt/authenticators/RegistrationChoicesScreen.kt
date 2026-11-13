package com.daon.fido.sdk.sample.kt.authenticators


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.daon.fido.client.sdk.Group
import com.daon.fido.sdk.sample.kt.util.getBitmap
import com.daon.fido.sdk.sample.kt.util.getGroupDescription
import com.daon.fido.sdk.sample.kt.util.getGroupTitle
import com.daon.sdk.authenticator.Authenticator


/**
 *
 * Displays a list of authenticators for the user to select and register. It handles
 * user interactions for selecting an authenticator and navigating back.
 */
@Composable
fun RegistrationChoicesScreen(onNavigateUp: () -> Unit, viewModel: AuthenticatorsViewModel) {
    // Collect the AuthenticatorState from the ViewModel which includes the list of authenticators
    val state = viewModel.authState.collectAsState()
    val policy = state.value.policy
    val groups = policy?.getGroups()
    var selectionInProgress by remember { mutableStateOf(false) }

    val onGroupSelected: (Group) -> Unit = { group ->
        if (!selectionInProgress) {
            selectionInProgress = true
            viewModel.updateSelectedGroup(group)
            onNavigateUp()
        }
    }

    // Screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                title = { Text("Select the authenticator to register") }
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
    ) {
        if (groups != null) {
            GroupList(groups, it, selectionInProgress, onGroupSelected)
        }
    }

    // Handle back button press
    BackHandler(true) {
        viewModel.resetAuthListAvailable()
        viewModel.cancelCurrentOperation()
        onNavigateUp()
    }
}

@Composable
fun GroupList(
    groups: Array<Group>,
    padding: PaddingValues,
    selectionInProgress: Boolean,
    onGroupSelected: (Group) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = padding) {
        items(groups) { group ->
            // Exclude creating an AuthCard for groups containing any of the following factors: VOICE, ADOS_VOICE, OTP, and PATTERN
            if (isAuthenticatorGroupSupported(group)) {
                AuthCard(group, selectionInProgress, onGroupSelected)
            }

        }
    }
}

@Composable
fun AuthCard(
    group: Group,
    selectionInProgress: Boolean,
    onGroupSelected: (Group) -> Unit
) {
    // Card layout for each authenticator item
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(enabled = !selectionInProgress) {
                // Update the selected authenticator and navigate back
                onGroupSelected(group)
            },
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the authenticator icon if available
            Image(
                bitmap = getBitmap(group.getAuthenticator().icon),
                contentDescription = " ",
                modifier = Modifier
                    .size(60.dp)
                    .padding(6.dp),
                contentScale = ContentScale.Fit)
            // Display the authenticator title and description
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = getGroupTitle(group),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
                Text (
                    text = getGroupDescription(group),
                    style = MaterialTheme.typography.body2
                )
            }

        }
    }
}

private fun isAuthenticatorGroupSupported(group: Group): Boolean {
    val unsupportedFactors = setOf(Authenticator.Factor.OTP, Authenticator.Factor.VOICE, Authenticator.Factor.PATTERN)
    return unsupportedFactors.none { group.getAuthenticatorSet().containsFactor(it) }
}




