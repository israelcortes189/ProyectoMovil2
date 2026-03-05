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
package com.example.marsphotos

import android.app.Application
import android.util.Log
import androidx.work.WorkManager
import com.example.marsphotos.data.AppContainer
import com.example.marsphotos.data.DefaultAppContainer
import com.example.marsphotos.data.datbase.AppDatabase
import androidx.work.Configuration



class MarsPhotosApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Inicializa tu contenedor de dependencias
        container = DefaultAppContainer(applicationContext)

        // Ya no registramos AppWorkerFactory ni inicializamos WorkManager manualmente.
        Log.d("APP", "Application onCreate: container inicializado, WorkManager usa configuración por defecto")
    }
}




