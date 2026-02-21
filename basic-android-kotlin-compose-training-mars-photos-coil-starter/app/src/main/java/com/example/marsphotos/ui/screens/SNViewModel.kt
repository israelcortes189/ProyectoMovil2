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

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.MarsPhotosRepository
import com.example.marsphotos.data.SNRepository
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.ProfileStudent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface SNUiState {
    data class Success(val accesoLogin: String) : SNUiState
    object Error : SNUiState
    object Loading : SNUiState
}


class SNViewModel(private val repository: SNRepository) : ViewModel() {
    fun hasSession(): Boolean =
        repository.hasSession()

    var profileState by mutableStateOf<ProfileStudent?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadProfile() {
        if (!repository.hasSession()) return
        viewModelScope.launch {
            isLoading = true
            try {
                profileState = repository.profile()
            } catch (e: Exception) {
                Log.e("PROFILE", "Error cargando perfil", e)
            } finally {
                isLoading = false
            }
        }
    }

    /** The mutable State that stores the status of the most recent request */
    var snUiState: SNUiState by mutableStateOf(SNUiState.Loading)
        private set

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            if (repository.hasSession()) {
                loadProfile()
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
                    snUiState = SNUiState.Success(result)
                    loadProfile()
                } else {
                    snUiState = SNUiState.Error
                }

                Log.d("LOGIN", "Resultado del repo: '$result'")
            } catch (e: Exception) {
                snUiState = SNUiState.Error
            }
        }
    }

    /**
     * Factory for [MarsViewModel] that takes [MarsPhotosRepository] as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val snRepository = application.container.snRepository
                SNViewModel(repository = snRepository)
            }
        }
    }
}
