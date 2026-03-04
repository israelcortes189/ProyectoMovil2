package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.repository.MainRepository
import com.google.gson.Gson

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

    override suspend fun doWork(): Result {
        return try {
            val tipo = inputData.getString("tipo") ?: return Result.failure()
            val matricula = inputData.getString("matricula") ?: return Result.failure()

            val result = when (tipo) {
                "perfil" -> mainRepository.remoteRepository.profile()
                "carga" -> mainRepository.remoteRepository.cargaAcademica()
                "cardex" -> mainRepository.remoteRepository.cardex(3)
                "califUnidades" -> mainRepository.remoteRepository.calificacionesPorUnidad()
                "califFinal" -> mainRepository.remoteRepository.calificacionFinal(9)
                else -> null
            }

            Log.d("REMOTE_WORKER", "start tipo=$tipo matricula=$matricula")
            val json = Gson().toJson(result)
            Log.d("REMOTE_WORKER", "output json length=${json.length}")
            val output = workDataOf("tipo" to tipo, "matricula" to matricula, "result" to json)
            return Result.success(output)
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
