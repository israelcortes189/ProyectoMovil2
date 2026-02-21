package com.example.marsphotos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.marsphotos.ui.LoginScreen
import com.example.marsphotos.ui.HomeScreen
import com.example.marsphotos.ui.screens.SNViewModel

//Ruta actual del nav controller para marcar en la barra lateral
@Composable
fun currentRoute(navController: NavController): String? =
    navController.currentBackStackEntryAsState().value?.destination?.route

@Composable
fun AppNavHost(
    viewModel: SNViewModel
) {
    val navController = rememberNavController()
    val startDestination = if (viewModel.hasSession()) {
        "home"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(navController, viewModel = viewModel)
        }
    }
}



