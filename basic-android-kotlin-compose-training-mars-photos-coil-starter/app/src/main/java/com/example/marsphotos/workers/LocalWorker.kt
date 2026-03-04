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
import com.example.marsphotos.repository.LocalRepository
import com.google.gson.Gson
import java.util.UUID

class LocalWorker(
    context: Context,
    params: WorkerParameters,
    private val localRepository: LocalRepository
) : CoroutineWorker(context, params) {

    constructor(context: Context, params: WorkerParameters) : this(
        context,
        params,
        (context.applicationContext as MarsPhotosApplication).container.localRepository
    )

    override suspend fun doWork(): Result {
        try {
            val tipo = inputData.getString("tipo") ?: return Result.failure()
            val matricula = inputData.getString("matricula") ?: return Result.failure()
            val remoteIdStr = inputData.getString("remoteId") // puede venir desde syncData

            // 1) Intentar obtener result desde el remote usando remoteId
            var resultJson: String? = null
            if (!remoteIdStr.isNullOrBlank()) {
                try {
                    val remoteId = UUID.fromString(remoteIdStr)
                    val workInfo = WorkManager.getInstance(applicationContext).getWorkInfoById(remoteId).get()
                    resultJson = workInfo?.outputData?.getString("result")
                    Log.d("LOCAL_WORKER", "Leído output del RemoteWorker id=$remoteIdStr, length=${resultJson?.length ?: 0}")
                } catch (e: Exception) {
                    Log.w("LOCAL_WORKER", "No se pudo leer output del remoteId: $remoteIdStr -> ${e.message}")
                }
            }

            // 2) Si no hay result desde remoteId, intentar leer "result" directamente del inputData
            if (resultJson.isNullOrBlank()) {
                resultJson = inputData.getString("result")
                Log.d("LOCAL_WORKER", "Result tomado de inputData, length=${resultJson?.length ?: 0}")
            }

            if (resultJson.isNullOrBlank()) {
                Log.e("LOCAL_WORKER", "No hay result JSON para tipo=$tipo matricula=$matricula")
                return Result.failure()
            }

            val gson = Gson()

            when (tipo) {
                "perfil" -> {
                    // El remote puede devolver un DTO (ProfileStudent) o ya una ProfileEntity serializada.
                    try {
                        // Intentar parsear como ProfileEntity (caso ideal si Remote ya envía entity)
                        val perfilEntity = gson.fromJson(resultJson, ProfileEntity::class.java)
                        if (!perfilEntity.matricula.isNullOrBlank()) {
                            localRepository.insertProfile(perfilEntity)
                            Log.d("LOCAL_WORKER", "input remoteId=${inputData.getString("remoteId")}, tipo=$tipo, matricula=$matricula")
                            Log.d("LOCAL_WORKER", "Leído result length=${resultJson?.length ?: 0}")
// después de insertar
                            Log.d("LOCAL_WORKER", "Insertado tipo=$tipo matricula=$matricula")

                        } else {
                            throw Exception("ProfileEntity vacío")
                        }
                    } catch (e: Exception) {
                        // Fallback: parsear DTO y mapear a Entity
                        try {
                            val perfilDto = gson.fromJson(resultJson, ProfileStudent::class.java)
                            val entity = ProfileEntity(
                                matricula = perfilDto.matricula,
                                nombre = perfilDto.nombre,
                                carrera = perfilDto.carrera,
                                semActual = perfilDto.semActual,
                                cdtosAcumulados = perfilDto.cdtosAcumulados
                            )
                            localRepository.insertProfile(entity)
                            Log.d("LOCAL_WORKER", "ProfileStudent mapeado e insertado: ${entity.matricula}")
                        } catch (ex: Exception) {
                            Log.e("LOCAL_WORKER", "Error parseando perfil: ${ex.message}")
                            return Result.failure()
                        }
                    }
                }

                "cardex" -> {
                    val items = gson.fromJson(resultJson, Array<CardexItem>::class.java).toList()
                    localRepository.insertCardex(matricula, items)
                    Log.d("LOCAL_WORKER", "Cardex insertado: count=${items.size}")
                }

                "carga" -> {
                    val items = gson.fromJson(resultJson, Array<CargaItem>::class.java).toList()
                    localRepository.insertCarga(matricula, items)
                    Log.d("LOCAL_WORKER", "Carga insertada: count=${items.size}")
                }

                "califUnidades" -> {
                    val items = gson.fromJson(resultJson, Array<CalificacionUnidadItem>::class.java).toList()
                    localRepository.insertCalificaciones(matricula, items)
                    Log.d("LOCAL_WORKER", "CalifUnidades insertadas: count=${items.size}")
                }

                "califFinal" -> {
                    val items = gson.fromJson(resultJson, Array<CalificacionFinalItem>::class.java).toList()
                    localRepository.insertCalificacionFinal(matricula, items)
                    Log.d("LOCAL_WORKER", "CalifFinal insertadas: count=${items.size}")
                }

                else -> {
                    Log.w("LOCAL_WORKER", "Tipo desconocido: $tipo")
                    return Result.failure()
                }
            }

            // Indicar éxito
            return Result.success(workDataOf("status" to "inserted"))
        } catch (e: Exception) {
            Log.e("LOCAL_WORKER", "Error en LocalWorker.doWork: ${e.message}", e)
            return Result.retry()
        }
    }
}



