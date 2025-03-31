package com.daon.fido.sdk.sample.kt.intro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.daon.fido.sdk.sample.kt.authenticators.AuthenticatorsScreen
import com.daon.fido.sdk.sample.kt.authenticators.AuthenticatorsViewModel
import com.daon.fido.sdk.sample.kt.authenticators.RegistrationChoicesScreen
import com.daon.fido.sdk.sample.kt.face.FaceScreen
import com.daon.fido.sdk.sample.kt.fingerprint.FingerprintScreen
import com.daon.fido.sdk.sample.kt.home.HomeScreen
import com.daon.fido.sdk.sample.kt.home.HomeViewModel
import com.daon.fido.sdk.sample.kt.passcode.PasscodeScreen
import com.daon.fido.sdk.sample.kt.transaction.TransactionChoicesScreen
import com.daon.fido.sdk.sample.kt.transaction.TransactionConfirmationScreen
import com.daon.fido.sdk.sample.kt.ui.theme.IdentityxandroidsdkfidoTheme
import com.daon.fido.sdk.sample.kt.util.FidoAppState
import com.daon.fido.sdk.sample.kt.util.rememberFidoAppState
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol
import dagger.hilt.android.AndroidEntryPoint

/*
 * This file defines the `IntroActivity` class, which is the entry point of the application.
 * It sets up the initial UI using Jetpack Compose and handles navigation between different screens.
 */
@AndroidEntryPoint
class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                                onNavigateToHome = {user: String, sessionId: String -> navController.navigate(Screen.Home.createRoute(user, sessionId))},
                                onNavigateToChooseAuth = {
                                        navigateUp: () -> Unit, viewModel: ViewModel ->
                                    navController.navigate(
                                        Screen.AuthenticationAuths.createRoute(
                                            navigateUp, viewModel
                                        ),
                                    )
                                },
                                onNavigateToAccounts = {navigateUp: () -> Unit, viewModel: ViewModel ->
                                    navController.navigate(
                                        Screen.Accounts.createRoute(
                                            navigateUp, viewModel))},
                                onNavigateToPasscode = {navController.navigate(Screen.Passcode.route)},
                                onNavigateToFace = {navController.navigate(Screen.Face.route)},
                                onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route)},
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }

                        // Composable for the Home screen
                        composable(Screen.Home.route) {
                            val user = it.arguments?.getString("user")
                            val sessionId = it.arguments?.getString("sessionId")
                            if (user != null && sessionId != null) {
                                HomeScreen(
                                    user,
                                    sessionId,
                                    onNavigateToRegistration = { sessionId: String -> navController.navigate(Screen.Registration.createRoute(sessionId)) },
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
                        val previousBackStackEntry = navController.previousBackStackEntry
                        val passcodeController = when (previousBackStackEntry?.destination?.route) {
                            Screen.Authenticators.route ->  hiltViewModel<AuthenticatorsViewModel>(previousBackStackEntry).getPasscodeController()
                            Screen.Intro.route -> hiltViewModel<IntroViewModel>(previousBackStackEntry).getPasscodeController()
                            Screen.Home.route -> hiltViewModel<HomeViewModel>(previousBackStackEntry).getPasscodeController()
                            else -> null
                        }
                        passcodeController?.let {
                            PasscodeScreen(
                                onNavigateUp = { navController.popBackStack() },
                                passcodeController = it
                            )

                        }
                    }
                    // Composable for the Face screen
                    composable(Screen.Face.route) {
                        val parentEntry: NavBackStackEntry?
                        var parentViewModel: IntroViewModel? = null
                        var faceController: FaceControllerProtocol? = null
                        val previousBackStackEntry = remember(it) { navController.previousBackStackEntry }

                        if (previousBackStackEntry?.destination?.route == Screen.Authenticators.route) {
                            parentEntry = previousBackStackEntry
                            faceController = hiltViewModel<AuthenticatorsViewModel>(parentEntry).getFaceController()
                        } else if (previousBackStackEntry?.destination?.route == Screen.Intro.route) {
                            parentEntry = previousBackStackEntry
                            parentViewModel = hiltViewModel<IntroViewModel>(parentEntry)
                            faceController = hiltViewModel<IntroViewModel>(parentEntry).getFaceController()
                        } else if (previousBackStackEntry?.destination?.route == Screen.Home.route) {
                            parentEntry = previousBackStackEntry
                            faceController = hiltViewModel<HomeViewModel>(parentEntry).getFaceController()
                        }
                        faceController?.let {controller ->
                                FaceScreen(
                                    onNavigateUp = { navController.popBackStack() },
                                    introViewModel = parentViewModel,
                                    onNavigateToAccounts = { navigateUp: () -> Unit, viewModel: ViewModel ->
                                        navController.navigate(
                                            Screen.Accounts.createRoute(
                                                navigateUp, viewModel))},
                                    faceController = controller
                                )
                        }
                    }

                    // Composable for the Fingerprint screen
                    composable(Screen.Fingerprint.route) {
                        val previousBackStackEntry = remember(it) { navController.previousBackStackEntry }

                        val fingerController = when (previousBackStackEntry?.destination?.route) {
                            Screen.Authenticators.route -> hiltViewModel<AuthenticatorsViewModel>(previousBackStackEntry).getFingerprintController()
                            Screen.Intro.route -> hiltViewModel<IntroViewModel>(previousBackStackEntry).getFingerprintController()
                            Screen.Home.route -> hiltViewModel<HomeViewModel>(previousBackStackEntry).getFingerprintController()
                            else -> null
                        }

                        fingerController?.let {
                            FingerprintScreen (
                                onNavigateUp = { navController.popBackStack()},
                                fingerprintController = it)
                        }

                    }

                    //Navigation graph for the registration flow
                    registerGraph(navController = navController)

                    // Composable for the AuthenticationAuths screen
                    composable(Screen.AuthenticationAuths.route) {
                        val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Intro.route) }
                        val parentViewModel = hiltViewModel<IntroViewModel>(parentEntry)
                        AuthenticationChoicesScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
                    }

                    composable(Screen.Accounts.route) {
                        val parentEntry = remember(it) { navController.getBackStackEntry(Screen.Intro.route) }
                        val parentViewModel = hiltViewModel<IntroViewModel>(parentEntry)
                        AccountListScreen(onNavigateUp = {navController.popBackStack()}, parentViewModel)
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
                val sessionId = it.arguments?.getString("sessionId")
                if (sessionId != null) {
                    AuthenticatorsScreen(
                        sessionId = sessionId,
                        onNavigateToChooseAuth = { navigateUp: () -> Unit, viewModel: ViewModel ->
                            navController.navigate(
                                Screen.RegistrationAuths.createRoute(
                                    navigateUp, viewModel
                                ),
                            )
                        },
                        onNavigateToPasscode = { navController.navigate(Screen.Passcode.route) },
                        onNavigateToFace = { navController.navigate(Screen.Face.route) },
                        onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route) },
                        onNavigateUp = { navController.popBackStack() },
                    )
                }
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
    data object Home: Screen("home/{user}/{sessionId}") {
        fun createRoute(user: String, sessionId: String) = "home/$user/$sessionId"
    }
    data object Passcode: Screen("passcode")
    data object Registration: Screen("registration/{sessionId}") {
        fun createRoute(sessionId: String) = "registration/$sessionId"
    }
    data object Authenticators: Screen("authenticators/{sessionId}") {
        fun createRoute(sessionId: String) = "authenticators/$sessionId"
    }
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
    data object Accounts: Screen("accounts/{navigateUp}/{viewModel}") {
        fun createRoute(
            navigateUp: () -> Unit,
            viewModel: ViewModel
        ) = "accounts/$navigateUp/$viewModel"
    }
    data object Face: Screen("face")
    data object Fingerprint: Screen("fingerprint")
}



