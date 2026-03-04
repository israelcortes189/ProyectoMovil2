package com.example.marsphotos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.marsphotos.componentes.MenuLateral
import com.example.marsphotos.componentes.TopBar
import com.example.marsphotos.ui.screens.SNViewModel

@Composable
fun CalificacionFinal(
    navController: NavHostController,
    viewModel: SNViewModel,
    matricula: String? = null,      // null = usar la guardada en ViewModel
    modEducativo: Int = 9,          // ajusta según tu API si hace falta
    online: Boolean = true
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val califFinal by viewModel.califFinalState.collectAsState()

    // Cargar al entrar (matricula = null -> ViewModel usará getSavedMatricula())
    LaunchedEffect(matricula, modEducativo, online) {
        viewModel.loadCalificacionFinal(matricula = matricula, modEducativo = modEducativo, online = online)
    }

    MenuLateral(navController = navController, drawerState = drawerState, viewModel) {
        Scaffold(
            topBar = { TopBar(navController, drawerState) },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (califFinal.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text("No hay calificaciones finales registradas.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        items(califFinal) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Encabezado: materia + grupo + calificación
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.materia.ifBlank { "—" },
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Grupo: ${item.grupo.ifBlank { "—" }}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = item.calif.toString(),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = item.acreditacion.ifBlank { "—" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Observaciones
                                    if (!item.observaciones.isNullOrBlank()) {
                                        Text(
                                            text = "Observaciones: ${item.observaciones}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
