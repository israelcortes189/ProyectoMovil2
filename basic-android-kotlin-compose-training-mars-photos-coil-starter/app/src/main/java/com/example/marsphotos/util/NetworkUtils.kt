package com.example.marsphotos.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        @Suppress("DEPRECATION")
        val ni = cm.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return ni.isConnected
    }
}
