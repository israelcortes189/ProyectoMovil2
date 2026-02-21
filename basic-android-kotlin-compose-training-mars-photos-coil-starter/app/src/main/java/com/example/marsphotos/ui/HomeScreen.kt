/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.ui


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.marsphotos.componentes.MenuLateral
import com.example.marsphotos.componentes.TopBar
import com.example.marsphotos.model.getMenuItems
import com.example.marsphotos.ui.screens.SNViewModel
import java.time.LocalDateTime

@Composable
fun HomeScreen(navController: NavHostController, viewModel: SNViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    MenuLateral(navController = navController, drawerState = drawerState) {
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
                Contenido(viewModel = viewModel)
            }
        }
    }
}


@Composable
fun Contenido(viewModel: SNViewModel) {
    val profile = viewModel.profileState
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (profile != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Alumno: ${profile.nombre}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text("Matrícula: ${profile.matricula}")
                    Text("Carrera: ${profile.carrera}")
                    Text("Semestre: ${profile.semestre}")
                    Text("Creditos acumulados: ${profile.creditos}")
                }
            }
        } else {
            Text("No se pudo cargar el perfil", color = Color.Red)
        }
    }
}
