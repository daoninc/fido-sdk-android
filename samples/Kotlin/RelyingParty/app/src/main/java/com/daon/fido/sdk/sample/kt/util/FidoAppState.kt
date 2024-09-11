package com.daon.fido.sdk.sample.kt.util

import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

/**
 * Class to hold the state of the app.
 */
class FidoAppState(
    val scaffoldState: ScaffoldState,
    val navController: NavHostController
)

/**
 * Remember the state of the app.
 */
@Composable
fun rememberFidoAppState(
    scaffoldState: ScaffoldState = rememberScaffoldState(
        snackbarHostState = remember {
            SnackbarHostState()
        }
    ),
    navController: NavHostController = rememberNavController(),
    snackbarScope: CoroutineScope = rememberCoroutineScope()
) = remember(scaffoldState, navController, snackbarScope) {
    FidoAppState(
        scaffoldState = scaffoldState,
        navController = navController
    )
}
