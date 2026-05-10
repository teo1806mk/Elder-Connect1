package com.example.elder_connect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.elder_connect.ui.screens.auth.CreateElderScreen
import com.example.elder_connect.ui.screens.auth.LoginScreen
import com.example.elder_connect.ui.screens.auth.RegisterScreen
import com.example.elder_connect.ui.viewmodels.AuthViewModel

/**
 * Auth navigation graph.
 * Login -> Register -> back to Login (μετά την εγγραφή κάνει αυτόματο login)
 */
@Composable
fun AuthNavGraph(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = ElderRoute.Login.route
    ) {
        composable(ElderRoute.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = {
                    navController.navigate(ElderRoute.Register.route)
                }
            )
        }

        composable(ElderRoute.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegistered = onAuthSuccess,
                onBack = { navController.popBackStack() }
            )
        }
    }
}