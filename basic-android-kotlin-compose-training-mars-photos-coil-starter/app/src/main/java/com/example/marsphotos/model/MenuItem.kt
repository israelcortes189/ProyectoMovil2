package com.example.marsphotos.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.marsphotos.R
import com.example.marsphotos.ui.navigation.Rutas

data class MenuItem(
    val icon: Painter,
    val title: String,
    val ruta: String
)

@Composable
fun getMenuItems(): List<MenuItem> {
    return listOf(
        MenuItem(painterResource(R.drawable.home), "Home", Rutas.Home),
        MenuItem(painterResource(R.drawable.carga_academica), "Carga Académica", Rutas.CargaAcademica),
        MenuItem(painterResource(R.drawable.kerdex), "Kardex", Rutas.Kardex),
        MenuItem(painterResource(R.drawable.calificacion_parcial), "Calificación Parcial", Rutas.CalificacionesParciales),
        MenuItem(painterResource(R.drawable.calificacion_final), "Calificación Final", Rutas.ClificacionFinal)
    )
}






