package com.example.marsphotos.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.marsphotos.componentes.MenuLateral
import com.example.marsphotos.componentes.TopBar
import com.example.marsphotos.ui.screens.SNViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@Composable
fun CalificacionesUnidad(
    navController: NavHostController,
    viewModel: SNViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val calificaciones by viewModel.calificacionesState.collectAsState()

    // Cargar al entrar (matricula = null -> ViewModel usará getSavedMatricula())
    viewModel.loadCalificacionesPorUnidad()

    MenuLateral(navController = navController, drawerState = drawerState, viewModel) {
        Scaffold(
            topBar = { TopBar(navController, drawerState) },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (calificaciones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text("No hay calificaciones registradas.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        items(items = calificaciones) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = item.materia, style = MaterialTheme.typography.titleMedium)
                                            Text(text = "Grupo: ${item.grupo} • Observaciones: ${item.observaciones}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(text = "Unidades: ${item.unidadesActivas}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        UnitCell("C1", item.c1)
                                        UnitCell("C2", item.c2)
                                        UnitCell("C3", item.c3)
                                        UnitCell("C4", item.c4)
                                        UnitCell("C5", item.c5)
                                        UnitCell("C6", item.c6)
                                        UnitCell("C7", item.c7)
                                        UnitCell("C8", item.c8)
                                        UnitCell("C9", item.c9)
                                        UnitCell("C10", item.c10)
                                        UnitCell("C11", item.c11)
                                        UnitCell("C12", item.c12)
                                        UnitCell("C13", item.c13)
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
private fun UnitCell(label: String, value: String?) {
    Column(
        modifier = Modifier
            .width(48.dp)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
