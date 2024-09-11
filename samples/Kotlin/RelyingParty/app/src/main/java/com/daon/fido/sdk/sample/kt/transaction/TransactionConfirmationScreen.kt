    package com.daon.fido.sdk.sample.kt.transaction

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material.Button
    import androidx.compose.material.MaterialTheme
    import androidx.compose.material.Scaffold
    import androidx.compose.material.Text
    import androidx.compose.material.TopAppBar
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.collectAsState
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.asImageBitmap
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.daon.fido.client.sdk.IXUAF
    import com.daon.fido.client.sdk.transaction.TransactionContent
    import com.daon.fido.sdk.sample.kt.home.HomeViewModel

    /**
     * Displays the transaction confirmation screen.
     * It handles user interactions for confirming or cancelling the transaction.
     */
    @Composable
    fun TransactionConfirmationScreen(onNavigateUp: () -> Unit, viewModel: HomeViewModel) {

        val state = viewModel.transactionState.collectAsState()
        val transactionContent: TransactionContent? = state.value.transactionContent

        Scaffold (
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    title = { Text("Transaction Confirmation")}
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TransactionContent(transactionContent = transactionContent)
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ){
                    Button(onClick = {
                    confirmTransaction(onNavigateUp, viewModel)
                    }) {
                        Text(
                            text = "Confirm",
                            style = TextStyle(fontSize = 15.sp)
                        )
                    }
                    Button(onClick = {
                    cancelTransaction(onNavigateUp, viewModel)
                    }) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(fontSize = 15.sp)
                        )
                    }

                }
            }
        }
    }

    @Composable
    fun TransactionContent(transactionContent: TransactionContent?) {
        if (transactionContent != null && transactionContent.isValid) {
            when (transactionContent.type) {
                TransactionContent.Type.Text -> {
                    Text(
                        text = transactionContent.text,
                        style = TextStyle(fontSize = 15.sp))
                }
                TransactionContent.Type.PngImage -> {
                    Image(
                        bitmap = transactionContent.image.asImageBitmap(),
                        contentDescription = "Transaction Confirmation Image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TransactionContent.Type.Unknown -> {
                    null
                }
            }
        }
    }

    fun confirmTransaction (onNavigateUp: () -> Unit, viewModel: HomeViewModel) {
        viewModel.resetTransactionData()
        viewModel.updateTransactionConfirmationResult(IXUAF.DISPLAY_TRANSACTION_RESULT_OK)
        onNavigateUp()

    }

    fun cancelTransaction (onNavigateUp: () -> Unit, viewModel: HomeViewModel) {
        viewModel.resetTransactionData()
        viewModel.updateTransactionConfirmationResult(IXUAF.DISPLAY_TRANSACTION_RESULT_CANCELLED)
        onNavigateUp()
    }
