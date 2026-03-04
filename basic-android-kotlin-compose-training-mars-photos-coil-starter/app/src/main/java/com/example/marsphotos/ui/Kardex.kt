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
fun Kardex(
    navController: NavHostController,
    viewModel: SNViewModel,
    matricula: String? = null,
    lineamiento: Int = 3,
    online: Boolean = true
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Observa el StateFlow del ViewModel
    val cardex by viewModel.cardexState.collectAsState()

    // Cargar al entrar: pasamos matricula = null para que el ViewModel recupere la guardada
    LaunchedEffect(matricula, lineamiento, online) {
        viewModel.loadCardex(matricula = matricula, lineamiento = lineamiento, online = online)
    }

    MenuLateral(navController = navController, drawerState = drawerState, viewModel) {
        Scaffold(
            topBar = { TopBar(navController, drawerState) },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (cardex.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text("No hay registros. Pulsa 'Cargar Cardex' para obtenerlos.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                        ) {
                            items(cardex) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = item.materia ?: "—", style = MaterialTheme.typography.titleMedium)
                                            Text(text = item.calificacion?.toString() ?: "—", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Clave: ${item.claveMateria ?: "—"}  •  Oficial: ${item.claveOficial ?: "—"}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Créditos: ${item.creditos ?: "—"}  •  Acreditación: ${item.acreditacion ?: "—"}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Semestre: ${item.semestre ?: "—"}  •  ${item.periodo ?: ""} ${item.anio ?: ""}", style = MaterialTheme.typography.bodySmall)
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

