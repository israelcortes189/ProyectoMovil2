package com.example.cliente

import android.content.ContentValues
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el contenido de la pantalla usando Jetpack Compose
        setContent {
            // Columna que contiene la pantalla principal y el botón de inserción
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Pantalla principal (ocupa el espacio disponible)
                Box(modifier = Modifier.weight(1f)) {
                    SimpleClientScreen()
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para insertar fila de prueba
                InsertTestButton()
            }
        }
    }

    @Composable
    fun InsertTestButton() {
        val context = LocalContext.current
        Button(onClick = {
            //
            val uri = Uri.parse("content://com.example.marsphotos.provider/carga")
            val values = ContentValues().apply {
                put("matricula", "S20120189")
                put("claveOficial", "MAT101")
                put("materia", "Algoritmos")
                put("creditos", 6)
            }
            val result = try {
                context.contentResolver.insert(uri, values)
            } catch (e: SecurityException) {
                null
            } catch (e: Exception) {
                null
            }
            val msg = if (result != null) "Insertado: $result" else "Fallo al insertar (revisa permisos)"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }) {
            Text("Insertar fila de prueba")
        }
    }


    @Composable
    fun SimpleClientScreen() {
        // Scope para lanzar corrutinas
        val scope = rememberCoroutineScope()

        // Mapa que guarda los datos: título -> lista de strings
        var data by remember { mutableStateOf(mapOf<String, List<String>>()) }

        // Variable para guardar errores
        var error by remember { mutableStateOf<String?>(null) }

        // Función para actualizar los datos desde el ContentProvider
        fun refresh() {
            scope.launch {
                try {
                    // Consulta de datos
                    val carga = query("carga")
                    val cardex = query("cardex")

                    // Guardamos ambos resultados en el mapa
                    data = mapOf("Carga Académica" to carga, "Kardex" to cardex)

                    // Limpiamos error si todo sale bien
                    error = null
                } catch (e: Exception) {
                    // Guardamos el error si ocurre alguno
                    error = e.message ?: "Error de conexión"
                }
            }
        }

        // Cargar datos al iniciar la composición
        LaunchedEffect(Unit) {
            refresh()
        }

        Scaffold(
            // Botón flotante para actualizar datos
            floatingActionButton = {
                ExtendedFloatingActionButton(onClick = { refresh() }) {
                    Text("Actualizar")
                }
            }
        ) { padding ->
            // Lista vertical para mostrar la información
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {

                // Si hay error, se muestra arriba
                error?.let {
                    item {
                        Text(" $it", color = MaterialTheme.colorScheme.error)
                    }
                }

                // Recorremos el mapa (título y lista de datos)
                data.forEach { (titulo, filas) ->

                    // Mostramos el título (ej: Carga Académica)
                    item {
                        Text(titulo, style = MaterialTheme.typography.headlineSmall)
                    }

                    if (filas.isEmpty()) {
                        // Si no hay datos
                        item {
                            Text(
                                "Sin datos disponibles",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        // Mostramos cada fila dentro de una tarjeta
                        items(filas) { fila ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    fila,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Espacio entre secciones
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    //Consulta el ContentProvider y devuelve una lista de Strings formateados.
    private suspend fun query(path: String): List<String> = withContext(Dispatchers.IO) {
        // Construye la URI según el path (carga o cardex)
        val uri = Uri.parse("content://com.example.marsphotos.provider/$path")
        val list = mutableListOf<String>()
        // Ejecuta la consulta al ContentProvider
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            // Obtiene los nombres de las columnas
            val columnNames = cursor.columnNames
            // Recorre cada fila del cursor
            while (cursor.moveToNext()) {

                // Une todas las columnas en un solo string
                val rowData = columnNames.joinToString(" | ") { col ->
                    val idx = cursor.getColumnIndex(col)
                    "${col}: ${cursor.getString(idx) ?: ""}"
                }
                // Agrega la fila formateada a la lista
                list.add(rowData)
            }
        }

        // Devuelve la lista de resultados
        list
    }
}