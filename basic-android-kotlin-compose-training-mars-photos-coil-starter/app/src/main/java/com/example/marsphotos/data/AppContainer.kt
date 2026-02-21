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
package com.example.marsphotos.data

import AddCookiesInterceptor
import ReceivedCookiesInterceptor
import android.content.Context
import com.example.marsphotos.network.MarsApiService
import com.example.marsphotos.network.SICENETWService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory


/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val marsPhotosRepository: MarsPhotosRepository
    val snRepository: SNRepository
}

/**
 * Implementación para el contenedor de dependencias a nivel aplicación.
 * Variables inicializadas de forma lazy y compartidas en toda la app.
 */
class DefaultAppContainer(applicationContext: Context) : AppContainer {

    private val baseUrl = "https://android-kotlin-fun-mars-server.appspot.com/"
    private val baseUrlSN = "https://sicenet.surguanajuato.tecnm.mx"

    // Cliente OkHttp con interceptores de cookies
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor(applicationContext))     // Añade cookies guardadas
        .addInterceptor(ReceivedCookiesInterceptor(applicationContext)) // Recibe y guarda cookies nuevas
        .build()

    // Retrofit para el servicio de ejemplo (Mars)
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .build()

    // Retrofit para SICENET con soporte SOAP/XML
    private val retrofitSN: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrlSN)
        .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
        .client(client) // Usa el cliente con interceptores
        .build()

    // Servicio Retrofit para Mars
    private val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }

    // Servicio Retrofit para SICENET
    private val retrofitServiceSN: SICENETWService by lazy {
        retrofitSN.create(SICENETWService::class.java)
    }

    // Repositorio para Mars
    override val marsPhotosRepository: NetworkMarsPhotosRepository by lazy {
        NetworkMarsPhotosRepository(retrofitService)
    }

    // Repositorio para SICENET (con context para revisar cookies)
    override val snRepository: NetworSNRepository by lazy {
        NetworSNRepository(retrofitServiceSN, applicationContext)
    }
}
