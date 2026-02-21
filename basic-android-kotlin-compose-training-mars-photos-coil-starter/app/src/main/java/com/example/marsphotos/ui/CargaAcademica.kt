package com.example.marsphotos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.marsphotos.componentes.MenuLateral
import com.example.marsphotos.componentes.TopBar
import com.example.marsphotos.ui.screens.SNViewModel

@Composable
fun CargaAcademica(navController: NavHostController, viewModel: SNViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    MenuLateral(navController = navController, drawerState = drawerState, viewModel) {
        //Esqueleto de la UI con barra superior, menú lateral y contenido
        Scaffold(
            topBar = {
                TopBar(navController, drawerState)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                ContenidoCargaAcaemica()
            }
        }
    }
}


@Composable
fun ContenidoCargaAcaemica() {

}