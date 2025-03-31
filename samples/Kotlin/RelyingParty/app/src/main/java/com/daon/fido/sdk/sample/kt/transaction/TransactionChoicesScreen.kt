package com.daon.fido.sdk.sample.kt.transaction

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
import com.daon.fido.client.sdk.Group
import com.daon.fido.sdk.sample.kt.home.HomeViewModel
import com.daon.fido.sdk.sample.kt.util.getBitmap
import com.daon.fido.sdk.sample.kt.util.getGroupDescription
import com.daon.fido.sdk.sample.kt.util.getGroupTitle

/**
 *
 * Displays a list of authenticators for the user to select for step-up authentication. It handles
 * user interactions for selecting an authenticator and navigating back.
 */
@Composable
fun TransactionChoicesScreen(onNavigateUp: () -> Unit, viewModel: HomeViewModel) {
    val state = viewModel.transactionState.collectAsState()
    val policy = state.value.policy
    val groups = policy?.getGroups()

    // Screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                title = { Text("Select the authenticator")}
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    ) {
        if (groups != null) {
            GroupList(
                groups,
                onNavigateUp,
                viewModel,
                it
            )
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
fun GroupList(groups: Array<Group>, onNavigateUp: () -> Unit, viewModel: HomeViewModel, padding: PaddingValues) {

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = padding) {
        items(groups) { group ->
            AuthCard(
                group = group,
                onNavigateUp = onNavigateUp,
                viewModel = viewModel
            )
        }
    }
}


@Composable
fun AuthCard(group: Group, onNavigateUp: () -> Unit, viewModel: HomeViewModel) {
    // Card layout for each authenticator item
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                // Update the selected authenticator and navigate back
                viewModel.updateSelectedGroup(group)
                onNavigateUp()
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
                    text = getGroupDescription(group) ,
                    style = MaterialTheme.typography.body2
                )
            }

        }
    }

}


