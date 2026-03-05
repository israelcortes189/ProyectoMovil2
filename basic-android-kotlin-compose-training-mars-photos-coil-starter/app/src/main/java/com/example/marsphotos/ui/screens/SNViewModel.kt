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
package com.example.marsphotos.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.Entityes.CalificacionFinalEntity
import com.example.marsphotos.data.Entityes.CalificacionUnidadEntity
import com.example.marsphotos.data.Entityes.CardexEntity
import com.example.marsphotos.data.Entityes.CargaEntity
import com.example.marsphotos.repository.LocalRepository
import com.example.marsphotos.repository.MainRepository

import com.example.marsphotos.repository.NetworSNRepository
import com.example.marsphotos.data.Entityes.ProfileEntity
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.util.isOnline
import com.example.marsphotos.workers.LocalWorker
import com.example.marsphotos.workers.RemoteWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * UI state for the Home screen
 *
 * SNUiState representa el estado general de la UI (éxito, error o cargando).
 */
sealed interface SNUiState {
    data class Success(val accesoLogin: String ) : SNUiState
    object Error : SNUiState
    object Loading : SNUiState
}

/**
 * SNViewModel
 *
 * ViewModel central que orquesta:
 * - Acceso y sesión (login/logout)
 * - Carga de datos (perfil, cardex, carga, calificaciones)
 * - Sincronización mediante WorkManager (syncData)
 *
 * Recibe:
 * - repository: fachada que expone operaciones remotas y locales (MainRepository)
 * - appContext: contexto de aplicación para acceder a SharedPreferences y WorkManager
 */
class SNViewModel(
    private val repository: MainRepository,
    private val appContext: Context
) : ViewModel() {

    var snUiState: SNUiState by mutableStateOf(SNUiState.Loading)
        private set

    var profileState by mutableStateOf<ProfileEntity?>(null)
    var isLoading by mutableStateOf(false)
        private set

    // Estados reactivos para la UI
    private val _cardexState = MutableStateFlow<List<CardexEntity>>(emptyList())
    val cardexState = _cardexState.asStateFlow()

    private val _calificacionesState = MutableStateFlow<List<CalificacionUnidadEntity>>(emptyList())
    val calificacionesState = _calificacionesState.asStateFlow()

    private val _cargaState = MutableStateFlow<List<CargaEntity>>(emptyList())
    val cargaState = _cargaState.asStateFlow()

    private val _califFinalState = MutableStateFlow<List<CalificacionFinalEntity>>(emptyList())
    val califFinalState = _califFinalState.asStateFlow()

    private val _syncState = MutableStateFlow<WorkInfo?>(null)

    fun <T> loadDataGeneric(tipo: String, stateFlow: MutableStateFlow<List<T>>, callRepo: suspend (String, Boolean) -> List<T>?
    ) {
        val mat = getSavedMatricula() ?: run {
            snUiState = SNUiState.Error
            return
        }

        viewModelScope.launch {
            snUiState = SNUiState.Loading
            try {
                val online = isOnline(appContext)
                // Intento online con fallback automático a local si falla la red
                val data = if (online) {
                    try { callRepo(mat, true) } catch (e: Exception) { callRepo(mat, false) }
                } else {
                    callRepo(mat, false)
                }

                if (!data.isNullOrEmpty()) {
                    stateFlow.value = data
                    // Si hubo éxito online, disparamos sync en fondo para actualizar Room
                    if (online) syncData(tipo, mat)
                    snUiState = SNUiState.Success("${tipo}Loaded")
                } else {
                    stateFlow.value = emptyList()
                    snUiState = SNUiState.Error
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL", "Error cargando $tipo", e)
                snUiState = SNUiState.Error
            }
        }
    }

    fun hasSession(): Boolean =
        repository.hasSession()


    fun logout() {
        repository.logout()
        profileState = null
        appContext.getSharedPreferences("session", Context.MODE_PRIVATE).edit().clear().apply()
        snUiState = SNUiState.Loading
    }

    fun loadProfile(matriculaParam: String? = null) {
        val mat = matriculaParam ?: getSavedMatricula() ?: return
        viewModelScope.launch {
            isLoading = true
            val online = isOnline(appContext)
            try {
                val perfil = repository.getProfile(mat, online)
                if (perfil != null) {
                    profileState = perfil
                    snUiState = SNUiState.Success(if (online) "Online" else "Offline")
                    if (online) syncData("perfil", mat)
                } else {
                    snUiState = SNUiState.Error
                }
            } finally { isLoading = false }
        }
    }

    /**
     * init
     *
     * Al crear el ViewModel se ejecuta checkSession() para validar si hay sesión activa
     * y cargar el perfil (online u offline) según disponibilidad de red.
     */
    init {
        checkSession()
    }

    /**
     * checkSession
     *
     * - Verifica SharedPreferences y repository.hasSession() para decidir si hay sesión.
     * - Reconstruye la matrícula en memoria si hace falta.
     * - Si hay internet intenta obtener perfil online; si falla, hace fallback a Room.
     * - Si no hay datos en local, fuerza logout y marca error.
     */
    private fun checkSession() {
        viewModelScope.launch {
            val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
            val hasSessionPref = prefs.getBoolean("hasSession", false)

            // Si ni el repo ni las prefs indican sesión, marcar error
            if (!repository.hasSession() && !hasSessionPref) {
                snUiState = SNUiState.Error
                return@launch
            }

            // Reconstruir matrícula en memoria si repository no la tiene
            if (repository.remoteRepository.currentMatricula == null) {
                val saved = prefs.getString("matricula", null)
                if (!saved.isNullOrBlank()) {
                    repository.remoteRepository.currentMatricula = saved
                }
            }

            val matricula = repository.remoteRepository.currentMatricula ?: return@launch

            // Intento online si hay conexión
            if (isOnline(appContext)) {
                try {
                    val profile = repository.getProfile(matricula, online = true)
                    if (profile != null) {
                        profileState = profile
                        snUiState = SNUiState.Success("Online")
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("SESSION", "Error online, fallback a local", e)
                }
            }

            // Fallback offline: leer desde Room
            val offlineProfile = repository.getProfile(matricula, online = false)
            if (offlineProfile != null) {
                profileState = offlineProfile
                snUiState = SNUiState.Success("Offline")
            } else {
                // No hay datos locales: cerrar sesión y limpiar prefs
                repository.logout()
                prefs.edit().clear().apply()
                snUiState = SNUiState.Error
            }
        }
    }

    /**
     * accesoSN
     *
     * Maneja el login:
     * - Llama repository.acceso(usuario, password)
     * - Si OK guarda matrícula y flag de sesión en prefs
     * - Encola sincronización de perfil si hay internet
     * - Si no hay internet hace fallback a datos locales
     */
    fun accesoSN(usuario: String, password: String) {
        viewModelScope.launch {
            snUiState = SNUiState.Loading
            val result = repository.acceso(usuario, password)
            if (result == "OK") {
                val mat = repository.remoteRepository.currentMatricula
                saveSession(mat)
                loadProfile(mat)
            } else {
                snUiState = SNUiState.Error
            }
        }
    }

    private fun getSavedMatricula(): String? {
        val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
        return repository.remoteRepository.currentMatricula ?: prefs.getString("matricula", null)
    }

    private fun saveSession(mat: String?) {
        appContext.getSharedPreferences("session", Context.MODE_PRIVATE).edit()
            .putString("matricula", mat)
            .putBoolean("hasSession", true)
            .apply()
    }

    fun loadCardex() = loadDataGeneric("cardex", _cardexState) { m, o ->
        repository.getCardex(m, 1, o)
    }

    fun loadCalificacionesPorUnidad() = loadDataGeneric("califUnidades", _calificacionesState) { m, o ->
        repository.getCalificacionesPorUnidad(m, o)
    }

    fun loadCargaAcademica() = loadDataGeneric("carga", _cargaState) { m, o ->
        repository.getCargaAcademica(m, o)
    }

    fun loadCalificacionFinal() = loadDataGeneric("califFinal", _califFinalState) { m, o ->
        repository.getCalificacionFinal(m, 1, o)
    }

    fun syncData(tipo: String, matricula: String) {
        val uniqueName = "sync_${tipo}_$matricula"
        val wm = WorkManager.getInstance(appContext)

        val remoteRequest = OneTimeWorkRequestBuilder<RemoteWorker>()
            .setInputData(workDataOf("tipo" to tipo, "matricula" to matricula)).build()
        val localRequest = OneTimeWorkRequestBuilder<LocalWorker>()
            .setInputData(workDataOf("tipo" to tipo, "matricula" to matricula, "remoteId" to remoteRequest.id.toString())).build()

        wm.beginUniqueWork(uniqueName, ExistingWorkPolicy.KEEP, remoteRequest).then(localRequest).enqueue()

        // Observación pasiva: solo para que la UI sepa si algo está corriendo
        viewModelScope.launch {
            wm.getWorkInfosForUniqueWorkLiveData(uniqueName).asFlow().collect {
                _syncState.value = it.lastOrNull()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                // Usa el mainRepository centralizado del container
                val mainRepo = application.container.mainRepository
                SNViewModel(repository = mainRepo, appContext = application.applicationContext)
            }
        }
    }
}