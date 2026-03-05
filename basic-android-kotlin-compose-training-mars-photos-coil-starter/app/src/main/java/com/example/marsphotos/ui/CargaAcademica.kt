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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow


@Composable
fun CargaAcademica(
    navController: NavHostController,
    viewModel: SNViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val carga by viewModel.cargaState.collectAsState()

    // Cargar al entrar (matricula = null -> ViewModel usará getSavedMatricula())
    viewModel.loadCargaAcademica()

    MenuLateral(navController = navController, drawerState = drawerState, viewModel) {
        Scaffold(
            topBar = { TopBar(navController, drawerState) },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (carga.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text("No hay registros de carga académica.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        items(carga) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Encabezado: materia + grupo + créditos
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = item.materia ?: "—", style = MaterialTheme.typography.titleMedium)
                                            Text(text = "Grupo: ${item.grupo ?: "—"} • Clave: ${item.claveOficial ?: "—"}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(text = "${item.creditos ?: item.creditos ?: "—"} cr", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Docente y observaciones
                                    Text(text = "Docente: ${item.docente ?: "—"}", style = MaterialTheme.typography.bodySmall)
                                    if (!item.observaciones.isNullOrBlank()) {
                                        Text(text = "Observaciones: ${item.observaciones}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Horario: fila compacta con días
                                    Column {
                                        Text(text = "Horario", style = MaterialTheme.typography.titleSmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            DayCell(day = "Lun", value = item.lunes)
                                            DayCell(day = "Mar", value = item.martes)
                                            DayCell(day = "Mié", value = item.miercoles)
                                            DayCell(day = "Jue", value = item.jueves)
                                            DayCell(day = "Vie", value = item.viernes)
                                            DayCell(day = "Sáb", value = item.sabado)
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
}

@Composable
private fun DayCell(day: String, value: String?) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = day, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

