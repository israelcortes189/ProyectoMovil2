package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.Entityes.ProfileEntity
import com.example.marsphotos.model.entityes.CalificacionFinalItem
import com.example.marsphotos.model.entityes.CalificacionUnidadItem
import com.example.marsphotos.model.entityes.CardexItem
import com.example.marsphotos.model.entityes.CargaItem
import com.example.marsphotos.model.entityes.ProfileStudent
import com.example.marsphotos.repository.MainRepository
import com.google.gson.Gson
import java.util.UUID
import androidx.work.ListenableWorker.Result as WorkResult

class LocalWorker(
    context: Context,
    params: WorkerParameters,
    private val mainRepository: MainRepository
) : CoroutineWorker(context, params) {

    // Constructor secundario para instanciación por reflexión
    constructor(context: Context, params: WorkerParameters) : this(context, params,
        (context.applicationContext as MarsPhotosApplication).container.mainRepository
    )

    private val gson = Gson()

    override suspend fun doWork(): WorkResult {
        val tipo = inputData.getString("tipo") ?: return WorkResult.failure()
        val matricula = inputData.getString("matricula") ?: return WorkResult.failure()
        val remoteIdStr = inputData.getString("remoteId")

        try {
            // 1. Obtener el JSON (del RemoteWorker o del input directo)
            val resultJson = getResultJson(remoteIdStr)
            if (resultJson.isNullOrBlank() || resultJson == "null") {
                Log.e("LOCAL_WORKER", "No hay JSON para $tipo")
                return WorkResult.failure()
            }

            // 2. Procesar según el tipo
            when (tipo) {
                "perfil" -> saveProfile(resultJson)
                "cardex" -> {
                    val items = gson.fromJson(resultJson, Array<CardexItem>::class.java).toList()
                    mainRepository.localRepository.insertCardex(matricula, items)
                }
                "carga" -> {
                    val items = gson.fromJson(resultJson, Array<CargaItem>::class.java).toList()
                    mainRepository.localRepository.insertCarga(matricula, items)
                }
                "califUnidades" -> {
                    val items = gson.fromJson(resultJson, Array<CalificacionUnidadItem>::class.java).toList()
                    mainRepository.localRepository.insertCalificaciones(matricula, items)
                }
                "califFinal" -> {
                    val items = gson.fromJson(resultJson, Array<CalificacionFinalItem>::class.java).toList()
                    mainRepository.localRepository.insertCalificacionFinal(matricula, items)
                }
                else -> return WorkResult.failure()
            }

            Log.d("LOCAL_WORKER", "Insertado con éxito: $tipo")
            return WorkResult.success(workDataOf("status" to "inserted"))

        } catch (e: Exception) {
            Log.e("LOCAL_WORKER", "Error en LocalWorker: ${e.message}")
            return WorkResult.retry()
        }
    }

    /**
     * Intenta recuperar el JSON del output del RemoteWorker
     */
    private fun getResultJson(remoteIdStr: String?): String? {
        if (remoteIdStr.isNullOrBlank()) return null
        return try {
            val remoteId = UUID.fromString(remoteIdStr)
            val workInfo = WorkManager.getInstance(applicationContext).getWorkInfoById(remoteId).get()
            workInfo?.outputData?.getString("result")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lógica específica para el perfil (maneja el mapeo de DTO a Entity)
     */
    private suspend fun saveProfile(json: String) {
        try {
            // Intento 1: Como Entity
            val entity = gson.fromJson(json, ProfileEntity::class.java)
            if (entity.matricula.isNullOrBlank()) throw Exception()
            mainRepository.localRepository.insertProfile(entity)
        } catch (e: Exception) {
            // Fallback: Como DTO
            val dto = gson.fromJson(json, ProfileStudent::class.java)
            val entity = ProfileEntity(
                matricula = dto.matricula,
                nombre = dto.nombre,
                carrera = dto.carrera,
                semActual = dto.semActual,
                cdtosAcumulados = dto.cdtosAcumulados
            )
            mainRepository.localRepository.insertProfile(entity)
        }
    }
}




