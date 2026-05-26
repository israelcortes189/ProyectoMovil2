package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.repository.MainRepository
import com.google.gson.Gson
import androidx.work.ListenableWorker.Result as WorkResult


//Este worker se encarga de traer lo datos, despues envia los datos
// al otro worker para que los guarde en la base base de datos local
class RemoteWorker(
    context: Context,
    params: WorkerParameters,
    private val mainRepository: MainRepository
) : CoroutineWorker(context, params) {

    constructor(context: Context, params: WorkerParameters) : this(
        context,
        params,
        (context.applicationContext as MarsPhotosApplication).container.mainRepository
    )

    private val gson = Gson()

    override suspend fun doWork(): WorkResult {
        val tipo = inputData.getString("tipo") ?: return WorkResult.failure()
        val matricula = inputData.getString("matricula") ?: return WorkResult.failure()

        return try {
            val result = when (tipo) {
                "perfil"-> mainRepository.remoteRepository.profile()
                "carga"-> mainRepository.remoteRepository.cargaAcademica()
                "cardex"-> mainRepository.remoteRepository.cardex(1)
                "califUnidades"-> mainRepository.remoteRepository.calificacionesPorUnidad()
                "califFinal"-> mainRepository.remoteRepository.calificacionFinal(1)
                else -> null
            }

            if (result == null) {
                Log.w("REMOTE_WORKER", "Resultado nulo para tipo=$tipo")
                return WorkResult.failure()
            }

            // Convertimos a JSON y enviamos al siguiente Worker (LocalWorker)
            val json = gson.toJson(result)
            WorkResult.success(workDataOf(
                "tipo" to tipo,
                "matricula" to matricula,
                "result" to json
            ))

        } catch (e: Exception) {
            Log.e("REMOTE_WORKER", "Error en red para $tipo: ${e.message}")
            // Usamos retry() si es un error de red (transitorio)
            WorkResult.retry()
        }
    }
}






