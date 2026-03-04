package com.example.marsphotos.workers

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.marsphotos.repository.LocalRepository
import com.example.marsphotos.repository.MainRepository


class AppWorkerFactory(
    private val mainRepository: MainRepository,
    private val localRepository: LocalRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d("WM-WorkerFactory", "createWorker requested: $workerClassName")
        return when (workerClassName) {
            RemoteWorker::class.java.name -> {
                Log.d("WM-WorkerFactory", "Creating RemoteWorker via factory")
                RemoteWorker(appContext, workerParameters, mainRepository)
            }
            LocalWorker::class.java.name -> {
                Log.d("WM-WorkerFactory", "Creating LocalWorker via factory")
                LocalWorker(appContext, workerParameters, localRepository)
            }
            else -> {
                Log.d("WM-WorkerFactory", "Not handled by AppWorkerFactory: $workerClassName")
                null
            }
        }
    }
}


