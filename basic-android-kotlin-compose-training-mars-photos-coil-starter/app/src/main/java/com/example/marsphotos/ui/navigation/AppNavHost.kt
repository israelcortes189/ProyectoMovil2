package com.example.marsphotos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.marsphotos.ui.CargaAcademica
import com.example.marsphotos.ui.LoginScreen
import com.example.marsphotos.ui.HomeScreen
import com.example.marsphotos.ui.screens.SNViewModel

//Ruta actual del nav controller para marcar en la barra lateral
@Composable
fun currentRoute(navController: NavController): String? =
    navController.currentBackStackEntryAsState().value?.destination?.route

//Tomar el nombre de la direccion actual
fun getNombreRuta(route: String?): String {
    return when (route) {
        Rutas.Home -> "Datos Academicos"
        Rutas.CargaAcademica -> "Carga academica"
        Rutas.ClificacionFinal -> "Calificaciones finales"
        Rutas.Kardex -> "Kardex"
        Rutas.CalificacionesParciales -> "Calificaciones Parciales"
        else -> ""
    }
}

@Composable
fun AppNavHost(
    viewModel: SNViewModel
) {
    val navController = rememberNavController()
    //Verifica si el viewModel tiene una sesion activa
    val startDestination = if (viewModel.hasSession()) {
        Rutas.Home
    } else {
        Rutas.Login
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rutas.Login) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    //despues de logearse el nav lo manda al home
                    navController.navigate(Rutas.Home) {
                        //elimina la pantalla de login del historial de navegación para que
                        //para que no pueda regresar al login
                        popUpTo(Rutas.Login) { inclusive = true }
                    }
                }
            )
        }


        composable(Rutas.Home) {
            HomeScreen(navController, viewModel = viewModel)
        }
        composable(Rutas.CargaAcademica) {
            CargaAcademica(navController, viewModel = viewModel)
        }
        composable(Rutas.Kardex) {
            // TODO: Pantalla de kardex
        }
        composable(Rutas.CalificacionesParciales) {
            // TODO: Pantalla de calificaciones parciales
        }
        composable(Rutas.ClificacionFinal) {
            // TODO: Pantalla de calificación final
        }
    }
}




