package com.example.marsphotos.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * @param context Contexto de la aplicación, necesario para acceder al servicio de conectividad.
 * @return true si el dispositivo tiene acceso a internet, false en caso contrario.
 */
fun isOnline(context: Context): Boolean {
    // Obtener el servicio de conectividad del sistema (ConnectivityManager).
    // Si no se puede obtener, no hay conexión.
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

    // A partir de Android Marshmallow (API 23) se usa la nueva API de NetworkCapabilities.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Obtener la red activa actual. Si no hay, no hay conexión.
        val nw = cm.activeNetwork ?: return false
        // Obtener las capacidades de la red activa. Si no hay, no hay conexión.
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        // Verificar si la red activa tiene la capacidad de acceder a internet.
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        // Para versiones anteriores a Android M se usa la API de NetworkInfo deprecada.
        @Suppress("DEPRECATION")
        val ni = cm.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        // Retorna true si la red está conectada.
        return ni.isConnected
    }
}

