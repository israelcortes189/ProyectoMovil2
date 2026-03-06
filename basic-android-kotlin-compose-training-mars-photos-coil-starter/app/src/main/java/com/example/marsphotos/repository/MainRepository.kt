package com.example.marsphotos.repository

import android.util.Log
import com.example.marsphotos.data.Entityes.CalificacionFinalEntity
import com.example.marsphotos.data.Entityes.CalificacionUnidadEntity
import com.example.marsphotos.data.Entityes.CardexEntity
import com.example.marsphotos.data.Entityes.CargaEntity
import com.example.marsphotos.data.Entityes.ProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull


// decide si los datos se deben de obteners desde la web o desde la base de datos local
class MainRepository(
    val localRepository: LocalRepository,
    val remoteRepository: NetworSNRepository
) {
    suspend fun acceso(m: String, p: String): String {
        return remoteRepository.acceso(m, p)
    }

    suspend fun getProfile(matricula: String, online: Boolean): ProfileEntity? {
        return if (online) {
            val remote = remoteRepository.profile()
            remote?.let {
                val entity = ProfileEntity(
                    matricula = it.matricula,
                    nombre = it.nombre,
                    carrera = it.carrera,
                    semActual = it.semActual,
                    cdtosAcumulados = it.cdtosAcumulados
                )
                localRepository.insertProfile(entity)
            }
            localRepository.getProfile(matricula).first()
        } else {
            localRepository.getProfile(matricula).first()
        }
    }

    fun hasSession(): Boolean = remoteRepository.hasSession()

    fun logout() {
        remoteRepository.logout()
    }

    suspend fun getCardex(matricula: String, lineamiento: Int, online: Boolean): List<CardexEntity>? {
        return if (online) {
            // 1) Consultar remoto
            val result = remoteRepository.cardex(lineamiento)
            result?.let { (items) ->
                // 2) Guardar en Room
                localRepository.insertCardex(matricula, items)
                }
            // 3) Devolver desde Room
            localRepository.getCardex(matricula).firstOrNull()
        } else {
            // Solo leer desde Room
            localRepository.getCardex(matricula).firstOrNull()
        }
    }

    suspend fun getCargaAcademica(matricula: String, online: Boolean): List<CargaEntity>? {
        return if (online) {
            // 1) Consultar remoto
            val result = remoteRepository.cargaAcademica()
            result?.let { items ->
                // 2) Guardar en Room
                localRepository.insertCarga(matricula, items)
            }
            // 3) Devolver desde Room
            localRepository.getCarga(matricula).firstOrNull()
        } else {
            // Solo leer desde Room
            localRepository.getCarga(matricula).firstOrNull()
        }
    }

    suspend fun getCalificacionesPorUnidad(matricula: String, online: Boolean): List<CalificacionUnidadEntity>? {
        return if (online) {
            // 1) Consultar remoto
            val result = remoteRepository.calificacionesPorUnidad()
            Log.d("REPO_CALIF", "getCalificacionesPorUnidad(m=$matricula, online=$online) -> ${result?.size ?: "null"}")
            result?.let { items ->
                // 2) Guardar en Room
                localRepository.insertCalificaciones(matricula, items)
            }
            // 3) Devolver desde Room
            localRepository.getCalificaciones(matricula).firstOrNull()
        } else {
            // Solo leer desde Room
            localRepository.getCalificaciones(matricula).firstOrNull()
        }
    }

    suspend fun getCalificacionFinal(matricula: String, modEducativo: Int, online: Boolean
    ): List<CalificacionFinalEntity>? {
        return if (online) {
            // 1) Consultar remoto
            val result = remoteRepository.calificacionFinal(modEducativo)
            result?.let { items ->
                // 2) Guardar en Room
                localRepository.insertCalificacionFinal(matricula, items)
            }
            // 3) Devolver desde Room
            localRepository.getCalificacionFinal(matricula).firstOrNull()
        } else {
            // Solo leer desde Room
            localRepository.getCalificacionFinal(matricula).firstOrNull()
        }
    }
}










