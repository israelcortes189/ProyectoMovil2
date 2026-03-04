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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * UI state for the Home screen
 */
sealed interface SNUiState {
    data class Success(val accesoLogin: String ) : SNUiState
    object Error : SNUiState
    object Loading : SNUiState
}


class SNViewModel(private val repository: MainRepository,
                  private val appContext: Context
    ) : ViewModel() {

    /** The mutable State that stores the status of the most recent request */
    var snUiState: SNUiState by mutableStateOf(SNUiState.Loading)
        private set

    fun hasSession(): Boolean =
        repository.hasSession()

    //Funcion para cerrar sesion en el repositori, asignar perfil como nulo
    // y asignar el estado de la sesion como cargando
    fun logout() {
        // Logout explícito: borra sesión en repo y estado en memoria.
        repository.logout()
        profileState = null
        snUiState = SNUiState.Loading

        // Borra prefs de sesión solo cuando el usuario cierra sesión desde UI
        val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.d("LOGOUT", "Sesión cerrada y prefs limpiadas")
    }

    var profileState by mutableStateOf<ProfileEntity?>(null)

    var isLoading by mutableStateOf(false)
        private set

    fun loadProfileFromLocal(matriculaParam: String? = null) {
        val matricula = matriculaParam ?: getSavedMatricula()
        if (matricula.isNullOrBlank()) {
            snUiState = SNUiState.Error
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val perfil = repository.localRepository.getProfile(matricula).firstOrNull()
                if (perfil != null) {
                    profileState = perfil
                    snUiState = SNUiState.Success("LoadedFromRoom")
                } else {
                    snUiState = SNUiState.Error
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL", "Error loadProfileFromLocal", e)
                snUiState = SNUiState.Error
            } finally {
                isLoading = false
            }
        }
    }


    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
        checkSession()
    }

    //Funcion para chacar si hay sesion activa en el repositori
    //tambien revisar si hay un perfil activo, en caso contrario ejecuta en el repositorio
    //la funcion cerrar secion para que se mande al flujo pedir un nuevo inicio de secion
    private fun checkSession() {
        viewModelScope.launch {
            val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
            val hasSessionPref = prefs.getBoolean("hasSession", false)

            if (!repository.hasSession() && !hasSessionPref) {
                snUiState = SNUiState.Error
                return@launch
            }

            // Reconstruir matricula en memoria si hace falta
            if (repository.remoteRepository.currentMatricula == null) {
                val saved = prefs.getString("matricula", null)
                if (!saved.isNullOrBlank()) {
                    repository.remoteRepository.currentMatricula = saved
                }
            }

            val matricula = repository.remoteRepository.currentMatricula ?: return@launch

            // Intento online si hay internet
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

            // Offline fallback: cargar desde Room
            val offlineProfile = repository.getProfile(matricula, online = false)
            if (offlineProfile != null) {
                profileState = offlineProfile
                snUiState = SNUiState.Success("Offline")
            } else {
                repository.logout()
                prefs.edit().clear().apply()
                snUiState = SNUiState.Error
            }
        }
    }


    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [MutableList].
     */
    fun accesoSN(usuario: String, password: String) {
        viewModelScope.launch {
            snUiState = SNUiState.Loading
            try {
                val result = repository.acceso(usuario, password)
                if (result == "OK") {
                    val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
                    val matricula = repository.remoteRepository.currentMatricula
                    prefs.edit().putString("matricula", matricula).putBoolean("hasSession", true).apply()

                    if (matricula != null && isOnline(appContext)) {
                        // Encolar sincronización de perfil (remote -> local)
                        syncData("perfil", matricula)
                    } else {
                        // fallback a datos locales
                        loadProfileFromLocal(matricula)
                    }

                    snUiState = SNUiState.Success(result)
                } else {
                    snUiState = SNUiState.Error
                }
                Log.d("LOGIN", "Resultado del repo: '$result'")
            } catch (e: Exception) {
                Log.e("LOGIN", "Error en accesoSN", e)
                snUiState = SNUiState.Error
            }
        }
    }

    private fun getSavedMatricula(): String? {
        val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
        return repository.remoteRepository.currentMatricula ?: prefs.getString("matricula", null)
    }

    private val _cardexState = MutableStateFlow<List<CardexEntity>>(emptyList())
    val cardexState: StateFlow<List<CardexEntity>> = _cardexState

    fun loadCardex(matricula: String?, lineamiento: Int, online: Boolean) {
        viewModelScope.launch {
            snUiState = SNUiState.Loading
            try {
                // Si no nos pasan matrícula, intentamos recuperar la guardada
                val mat = matricula ?: getSavedMatricula()
                if (mat.isNullOrBlank()) {
                    Log.e("VIEWMODEL_CARDEX", "No hay matrícula disponible para cargar cardex")
                    snUiState = SNUiState.Error
                    return@launch
                }

                // Si pedimos online y hay conexión, intentamos obtener del servidor
                val cardexList: List<CardexEntity>? = if (online && isOnline(appContext)) {
                    try {
                        repository.getCardex(mat, lineamiento, true)
                    } catch (e: Exception) {
                        Log.w("VIEWMODEL_CARDEX", "Error obteniendo cardex online, fallback a local", e)
                        repository.getCardex(mat, lineamiento, false)
                    }
                } else {
                    // Forzar lectura local/offline
                    repository.getCardex(mat, lineamiento, false)
                }

                if (!cardexList.isNullOrEmpty()) {
                    _cardexState.value = cardexList
                    Log.d("VIEWMODEL_CARDEX", "Cardex recibido: $cardexList")

                    // Si obtuvimos datos online, encolamos sincronización para mantener local actualizado
                    if (online && isOnline(appContext)) {
                        // guardamos matrícula en prefs por consistencia (igual que en accesoSN)
                        val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
                        prefs.edit().putString("matricula", mat).apply()

                        // Encolar sync para cardex (remote -> local)
                        syncData("cardex", mat)
                    }

                    snUiState = SNUiState.Success("CardexLoaded")
                } else {
                    _cardexState.value = emptyList()
                    Log.e("VIEWMODEL_CARDEX", "No se recibió información del cardex (matricula=$mat, lineamiento=$lineamiento, online=$online)")
                    snUiState = SNUiState.Error
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL_CARDEX", "Error cargando cardex (matricula=$matricula)", e)
                _cardexState.value = emptyList()
                snUiState = SNUiState.Error
            }
        }
    }

    private val _calificacionesState = MutableStateFlow<List<CalificacionUnidadEntity>>(emptyList())
    val calificacionesState: StateFlow<List<CalificacionUnidadEntity>> = _calificacionesState

    fun loadCalificacionesPorUnidad(matricula: String?, online: Boolean) {
        viewModelScope.launch {
            snUiState = SNUiState.Loading
            try {
                Log.d("VIEWMODEL_CALIF_UNIDADES", "loadCalificacionesPorUnidad called matricula=$matricula online=$online")
                val mat = matricula ?: getSavedMatricula()
                Log.d("VIEWMODEL_CALIF_UNIDADES", "resolved matricula=$mat")

                if (mat.isNullOrBlank()) {
                    Log.e("VIEWMODEL_CALIF_UNIDADES", "No hay matrícula disponible para cargar calificaciones por unidad")
                    snUiState = SNUiState.Error
                    return@launch
                }

                val califList: List<CalificacionUnidadEntity>? = if (online && isOnline(appContext)) {
                    try {
                        repository.getCalificacionesPorUnidad(mat, true)
                    } catch (e: Exception) {
                        Log.w("VIEWMODEL_CALIF_UNIDADES", "Error obteniendo calificaciones online, fallback a local", e)
                        repository.getCalificacionesPorUnidad(mat, false)
                    }
                } else {
                    repository.getCalificacionesPorUnidad(mat, false)
                }

                if (!califList.isNullOrEmpty()) {
                    _calificacionesState.value = califList
                    Log.d("VIEWMODEL_CALIF_UNIDADES", "Calificaciones recibidas: $califList")

                    if (online && isOnline(appContext)) {
                        val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
                        prefs.edit().putString("matricula", mat).apply()
                        syncData("califUnidades", mat)
                    }

                    snUiState = SNUiState.Success("CalifUnidadesLoaded")
                } else {
                    _calificacionesState.value = emptyList()
                    Log.e("VIEWMODEL_CALIF_UNIDADES", "No se recibió información de calificaciones por unidad (matricula=$mat, online=$online)")
                    snUiState = SNUiState.Error
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL_CALIF_UNIDADES", "Error cargando calificaciones por unidad (matricula=$matricula)", e)
                _calificacionesState.value = emptyList()
                snUiState = SNUiState.Error
            }
        }
    }


    private val _cargaState = MutableStateFlow<List<CargaEntity>>(emptyList())
    val cargaState: StateFlow<List<CargaEntity>> = _cargaState

    fun loadCargaAcademica(matricula: String?, online: Boolean) {
        viewModelScope.launch {
            snUiState = SNUiState.Loading
            try {
                val mat = matricula ?: getSavedMatricula()
                if (mat.isNullOrBlank()) {
                    Log.e("VIEWMODEL_CARGA", "No hay matrícula disponible para cargar carga académica")
                    snUiState = SNUiState.Error
                    return@launch
                }

                val cargaList: List<CargaEntity>? = if (online && isOnline(appContext)) {
                    try {
                        repository.getCargaAcademica(mat, true)
                    } catch (e: Exception) {
                        Log.w("VIEWMODEL_CARGA", "Error obteniendo carga online, fallback a local", e)
                        repository.getCargaAcademica(mat, false)
                    }
                } else {
                    repository.getCargaAcademica(mat, false)
                }

                if (!cargaList.isNullOrEmpty()) {
                    _cargaState.value = cargaList
                    Log.d("VIEWMODEL_CARGA", "Carga académica recibida: $cargaList")

                    if (online && isOnline(appContext)) {
                        val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
                        prefs.edit().putString("matricula", mat).apply()
                        syncData("carga", mat)
                    }

                    snUiState = SNUiState.Success("CargaLoaded")
                } else {
                    _cargaState.value = emptyList()
                    Log.e("VIEWMODEL_CARGA", "No se recibió información de carga académica (matricula=$mat, online=$online)")
                    snUiState = SNUiState.Error
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL_CARGA", "Error cargando carga académica (matricula=$matricula)", e)
                _cargaState.value = emptyList()
                snUiState = SNUiState.Error
            }
        }
    }



    private val _califFinalState = MutableStateFlow<List<CalificacionFinalEntity>>(emptyList())
    val califFinalState: StateFlow<List<CalificacionFinalEntity>> = _califFinalState

    fun loadCalificacionFinal(matricula: String?, modEducativo: Int, online: Boolean) {
        viewModelScope.launch {
            snUiState = SNUiState.Loading
            try {
                val mat = matricula ?: getSavedMatricula()
                if (mat.isNullOrBlank()) {
                    Log.e("VIEWMODEL_CALIF_FINAL", "No hay matrícula disponible para cargar calificación final")
                    snUiState = SNUiState.Error
                    return@launch
                }

                val califFinalList: List<CalificacionFinalEntity>? = if (online && isOnline(appContext)) {
                    try {
                        repository.getCalificacionFinal(mat, modEducativo, true)
                    } catch (e: Exception) {
                        Log.w("VIEWMODEL_CALIF_FINAL", "Error obteniendo calificación final online, fallback a local", e)
                        repository.getCalificacionFinal(mat, modEducativo, false)
                    }
                } else {
                    repository.getCalificacionFinal(mat, modEducativo, false)
                }

                if (!califFinalList.isNullOrEmpty()) {
                    _califFinalState.value = califFinalList
                    Log.d("VIEWMODEL_CALIF_FINAL", "Calificación final recibida: $califFinalList")

                    if (online && isOnline(appContext)) {
                        val prefs = appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
                        prefs.edit().putString("matricula", mat).apply()
                        syncData("califFinal", mat)
                    }

                    snUiState = SNUiState.Success("CalifFinalLoaded")
                } else {
                    _califFinalState.value = emptyList()
                    Log.e("VIEWMODEL_CALIF_FINAL", "No se recibió información de calificación final (matricula=$mat, online=$online)")
                    snUiState = SNUiState.Error
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL_CALIF_FINAL", "Error cargando calificación final (matricula=$matricula)", e)
                _califFinalState.value = emptyList()
                snUiState = SNUiState.Error
            }
        }
    }


    // Estado de sincronización
    private val _syncState = MutableStateFlow<WorkInfo?>(null)
    val syncState: StateFlow<WorkInfo?> = _syncState

    fun syncData(tipo: String, matricula: String) {
        val uniqueName = "sync_${tipo}_$matricula"

        // Input para el remote
        val remoteInput = workDataOf("tipo" to tipo, "matricula" to matricula)

        val remoteRequest = OneTimeWorkRequestBuilder<RemoteWorker>()
            .setInputData(workDataOf("tipo" to tipo, "matricula" to matricula))
            .build()

        val localRequest = OneTimeWorkRequestBuilder<LocalWorker>()
            .setInputData(workDataOf(
                "tipo" to tipo,
                "matricula" to matricula,
                "remoteId" to remoteRequest.id.toString()
            ))
            .build()

        WorkManager.getInstance(appContext)
            .beginUniqueWork("sync_${tipo}_$matricula", ExistingWorkPolicy.REPLACE, remoteRequest)
            .then(localRequest)
            .enqueue()


        // Pasamos el id del remote al local para que el LocalWorker pueda leer el output del remote
        val localInput = workDataOf("tipo" to tipo, "matricula" to matricula, "remoteId" to remoteRequest.id.toString())

        // Observa el remoteRequest por id (para mostrar "sincronizando")
        WorkManager.getInstance(appContext)
            .getWorkInfoByIdLiveData(remoteRequest.id)
            .asFlow()
            .onEach { info -> _syncState.value = info }
            .launchIn(viewModelScope)

        // Observa el UniqueWork para detectar cuando toda la cadena terminó
        WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(uniqueName)
            .asFlow()
            .onEach { infos ->
                val lastInfo = infos.lastOrNull()
                Log.d("SYNC", "Encolando sync_${tipo}_$matricula remoteId=${remoteRequest.id}")

                _syncState.value = lastInfo
                if (lastInfo?.state == WorkInfo.State.SUCCEEDED) {
                    when (tipo) {
                        "perfil" -> loadProfileFromLocal(matricula)
                        "cardex" -> loadCardex(matricula, lineamiento = 0, online = false)
                        "carga" -> loadCargaAcademica(matricula, online = false)
                        "califUnidades" -> loadCalificacionesPorUnidad(matricula, online = false)
                        "califFinal" -> loadCalificacionFinal(matricula, modEducativo = 0, online = false)
                    }
                }
            }
            .launchIn(viewModelScope)
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










