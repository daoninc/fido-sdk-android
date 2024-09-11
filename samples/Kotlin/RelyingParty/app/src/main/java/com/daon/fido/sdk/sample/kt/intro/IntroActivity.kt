package com.daon.fido.sdk.sample.kt.intro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.daon.fido.sdk.sample.kt.authenticators.AuthenticationChoicesScreen
import com.daon.fido.sdk.sample.kt.authenticators.AuthenticatorsScreen
import com.daon.fido.sdk.sample.kt.authenticators.AuthenticatorsViewModel
import com.daon.fido.sdk.sample.kt.util.FidoAppState
import com.daon.fido.sdk.sample.kt.transaction.RegistrationChoicesScreen
import com.daon.fido.sdk.sample.kt.transaction.TransactionChoicesScreen
import com.daon.fido.sdk.sample.kt.transaction.TransactionConfirmationScreen
import com.daon.fido.sdk.sample.kt.face.FaceScreen
import com.daon.fido.sdk.sample.kt.fingerprint.FingerprintScreen
import com.daon.fido.sdk.sample.kt.home.HomeScreen
import com.daon.fido.sdk.sample.kt.home.HomeViewModel
import com.daon.fido.sdk.sample.kt.passcode.PasscodeScreen
import com.daon.fido.sdk.sample.kt.util.rememberFidoAppState
import com.daon.fido.sdk.sample.kt.ui.theme.IdentityxandroidsdkfidoTheme
import dagger.hilt.android.AndroidEntryPoint

/*
 * This file defines the `IntroActivity` class, which is the entry point of the application.
 * It sets up the initial UI using Jetpack Compose and handles navigation between different screens.
 */
@AndroidEntryPoint
class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FidoApp()
        }
    }

    @Composable
    fun FidoApp() {
        IdentityxandroidsdkfidoTheme {
            val appState: FidoAppState = rememberFidoAppState()
            val navController = appState.navController

            //Scaffold layout for the app
            Scaffold(
                scaffoldState = appState.scaffoldState
            ) { innerPadding ->

                //Navigation host for the app
                NavHost(
                    navController = navController,
                    startDestination = Screen.Intro.route,
                    modifier = Modifier.padding(innerPadding)) {

                        // Composable for the Intro screen
                        composable(route = Screen.Intro.route) {
                            IntroScreen(
                                onNavigateToHome = {user: String -> navController.navigate(Screen.Home.createRoute(user))},
                                onNavigateToChooseAuth = {
                                        navigateUp: () -> Unit, viewModel: ViewModel ->
                                    navController.navigate(
                                        Screen.AuthenticationAuths.createRoute(
                                            navigateUp, viewModel
                                        ),
                                    )
                                },
                                onNavigateToPasscode = {navController.navigate(Screen.Passcode.route)},
                                onNavigateToFace = {navController.navigate(Screen.Face.route)},
                                onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route)},
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }

                        // Composable for the Home screen
                        composable(Screen.Home.route) {
                            it.arguments?.getString("user")?.let { it1 ->
                                HomeScreen(
                                    it1,
                                    onNavigateToRegistration = { navController.navigate(Screen.Registration.route) },
                                    backToIntro = { navController.navigate(Screen.Intro.route) {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                    } },
                                    onNavigateToChooseAuth = { navigateUp: () -> Unit, viewModel: ViewModel ->
                                        navController.navigate(
                                            Screen.TransactionAuths.createRoute(
                                                navigateUp, viewModel
                                            ),
                                        )
                                    },
                                    onNavigateToPasscode = {navController.navigate(Screen.Passcode.route)},
                                    onNavigateToFace = {navController.navigate(Screen.Face.route)},
                                    onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route)},
                                    onNavigateUp = { navController.popBackStack() },
                                    onNavigateToTransactionConfirmation = { navigateUp: () -> Unit, viewModel: ViewModel ->
                                        navController.navigate(
                                            Screen.TransactionConfirmation.createRoute(
                                                navigateUp, viewModel
                                            ),
                                        )
                                    },

                                )
                            }
                        }

                    // Composable for the Passcode screen
                    composable(Screen.Passcode.route) {
                        PasscodeScreen(onNavigateUp = { navController.popBackStack()})
                    }

                    // Composable for the Face screen
                    composable(Screen.Face.route) {
                        FaceScreen(onNavigateUp = { navController.popBackStack()})
                    }

                    // Composable for the Fingerprint screen
                    composable(Screen.Fingerprint.route) {
                        FingerprintScreen (onNavigateUp = { navController.popBackStack()})
                    }

                    //Navigation graph for the registration flow
                    registerGraph(navController = navController)

                    // Composable for the AuthenticationAuths screen
                    composable(Screen.AuthenticationAuths.route) {
                        val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Intro.route) }
                        val parentViewModel = hiltViewModel<IntroViewModel>(parentEntry)
                        AuthenticationChoicesScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
                    }

                    // Composable for the TransactionAuths screen
                    composable(Screen.TransactionAuths.route) {
                        val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Home.route) }
                        val parentViewModel = hiltViewModel<HomeViewModel>(parentEntry)
                        TransactionChoicesScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
                    }

                    // Composable for the TransactionConfirmation screen
                    composable(Screen.TransactionConfirmation.route) {
                        val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Home.route) }
                        val parentViewModel = hiltViewModel<HomeViewModel>(parentEntry)
                        TransactionConfirmationScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
                    }
                }

            }
        }
    }

    // Navigation graph for the registration flow
    private fun NavGraphBuilder.registerGraph(navController: NavController) {
        navigation(startDestination = Screen.Authenticators.route, route = Screen.Registration.route) {
            // Composable for the Authenticators screen
            composable(Screen.Authenticators.route) {
                AuthenticatorsScreen(
                    onNavigateToChooseAuth = { navigateUp: () -> Unit, viewModel: ViewModel ->
                        navController.navigate(
                            Screen.RegistrationAuths.createRoute(
                                navigateUp, viewModel
                            ),
                        )
                    },
                    onNavigateToPasscode = { navController.navigate(Screen.Passcode.route) },
                    onNavigateToFace = { navController.navigate(Screen.Face.route)},
                    onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route)},
                    onNavigateUp = { navController.popBackStack() },
                )
            }

            // Composable for the RegistrationAuths screen
            composable(Screen.RegistrationAuths.route) {
                val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Authenticators.route) }
                val parentViewModel = hiltViewModel<AuthenticatorsViewModel>(parentEntry)
                RegistrationChoicesScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
            }
        }
    }
}

// Sealed class representing different screens in the app
sealed class Screen(val route: String) {
    data object Intro: Screen("intro")
    data object Home: Screen("home/{user}") {
        fun createRoute(user: String) = "home/$user"
    }
    data object Passcode: Screen("passcode")
    data object Registration: Screen("registration")
    data object Authenticators: Screen("authenticators")
    data object RegistrationAuths: Screen("registrationAuths/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "registrationAuths/$navigateUp/$viewModel"
    }
    data object AuthenticationAuths: Screen("authenticationAuths/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "authenticationAuths/$navigateUp/$viewModel"
    }
    data object TransactionAuths: Screen("transactionAuths/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "transactionAuths/$navigateUp/$viewModel"
    }
    data object TransactionConfirmation: Screen("transactionConfirmation/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "transactionConfirmation/$navigateUp/$viewModel"
    }
    data object Face: Screen("face")
    data object Fingerprint: Screen("fingerprint")
}



