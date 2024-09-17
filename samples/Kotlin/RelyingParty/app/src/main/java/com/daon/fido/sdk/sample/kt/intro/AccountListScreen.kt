package com.daon.fido.sdk.sample.kt.intro


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daon.fido.client.sdk.model.AccountInfo

/**
 *
 * Displays a list of authenticators for the user to select for authentication. It handles
 * user interactions for selecting an authenticator and navigating back.
 */
@Composable
fun AccountListScreen(onNavigateUp: () -> Unit, viewModel: IntroViewModel) {
    // Collect the UiState from the ViewModel which includes the list of accounts
    val state = viewModel.uiState.collectAsState()
    val accountList: List<AccountInfo> = state.value.accountArray.toList()

    // Screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                title = { Text("Choose user account")}
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = it
        ) {
            items(accountList) { accountInfo ->
                AccountCard(account = accountInfo, onNavigateUp, viewModel)

            }
        }
    }

    // Handle back button press
    BackHandler(true) {
        viewModel.resetAccountListAvailable()
        viewModel.cancelCurrentOperation()
        onNavigateUp()
    }
}

@Composable
fun AccountCard(account: AccountInfo, onNavigateUp: () -> Unit, viewModel: IntroViewModel) {
    // Card layout for each account item
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                // Update the selected account and navigate back
                viewModel.updateSelectedAccount(account)
                onNavigateUp()
            },
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(8.dp)) {
                Text(
                   text = account.userName,
                   style = MaterialTheme.typography.h6,
                   color = MaterialTheme.colors.onSurface
                )
            }

        }
    }
}
