package com.example.marsphotos.repository

import com.example.marsphotos.data.Entityes.CalificacionFinalEntity
import com.example.marsphotos.data.Entityes.CalificacionUnidadEntity
import com.example.marsphotos.data.Entityes.CardexEntity
import com.example.marsphotos.data.Entityes.CargaEntity
import com.example.marsphotos.data.Entityes.ProfileEntity
import com.example.marsphotos.data.dao.CalificacionFinalDao
import com.example.marsphotos.data.dao.CalificacionUnidadDao
import com.example.marsphotos.data.dao.CardexDao
import com.example.marsphotos.data.dao.CargaDao
import com.example.marsphotos.data.dao.ProfileDao
import com.example.marsphotos.model.entityes.CalificacionFinalItem
import com.example.marsphotos.model.entityes.CalificacionUnidadItem
import com.example.marsphotos.model.entityes.CardexItem
import com.example.marsphotos.model.entityes.CargaItem
import kotlinx.coroutines.flow.Flow

class LocalRepository(
    private val profileDao: ProfileDao,
    private val cardexDao: CardexDao,
    private val cargaDao: CargaDao,
    private val calificacionUnidadDao: CalificacionUnidadDao,
    private val calificacionFinalDao: CalificacionFinalDao
) {

    suspend fun insertCardex(matricula: String, items: List<CardexItem>) {
        val entities = items.map {
            CardexEntity(
                matricula = matricula,
                claveMateria = it.claveMateria,
                claveOficial = it.claveOficial,
                materia = it.materia,
                creditos = it.creditos,
                calificacion = it.calificacion,
                acreditacion = it.acreditacion,
                semestre = it.semestre,
                periodo = it.periodo,
                anio = it.anio
            )
        }
        cardexDao.insertCardex(entities)
    }

    fun getCardex(matricula: String): Flow<List<CardexEntity>> =
        cardexDao.getCardexByMatricula(matricula)

    suspend fun insertCarga(matricula: String, items: List<CargaItem>) {
        val entities = items.map {
            CargaEntity(
                matricula = matricula,
                claveOficial = it.clvOficial,
                materia = it.materia,
                grupo = it.grupo,
                docente = it.docente,
                creditos = it.creditos,
                estadoMateria = it.estadoMateria,
                observaciones = it.observaciones,
                semipresencial = it.semipresencial,
                lunes = it.lunes,
                martes = it.martes,
                miercoles = it.miercoles,
                jueves = it.jueves,
                viernes = it.viernes,
                sabado = it.sabado
            )
        }
        cargaDao.insertCarga(entities)
    }

    fun getCarga(matricula: String): Flow<List<CargaEntity>> =
        cargaDao.getCargaByMatricula(matricula)

    suspend fun insertCalificaciones(matricula: String, items: List<CalificacionUnidadItem>) {
        val entities = items.map {
            CalificacionUnidadEntity(
                matricula = matricula,
                materia = it.materia,
                grupo = it.grupo,
                observaciones = it.observaciones,
                unidadesActivas = it.unidadesActivas,
                c1 = it.c1,
                c2 = it.c2,
                c3 = it.c3,
                c4 = it.c4,
                c5 = it.c5,
                c6 = it.c6,
                c7 = it.c7,
                c8 = it.c8,
                c9 = it.c9,
                c10 = it.c10,
                c11 = it.c11,
                c12 = it.c12,
                c13 = it.c13
            )
        }
        calificacionUnidadDao.insertCalificaciones(entities)
    }

    fun getCalificaciones(matricula: String): Flow<List<CalificacionUnidadEntity>> =
        calificacionUnidadDao.getCalificacionesByMatricula(matricula)


    suspend fun insertCalificacionFinal(matricula: String, items: List<CalificacionFinalItem>) {
        val entities = items.map {
            CalificacionFinalEntity(
                matricula = matricula,
                materia = it.materia,
                grupo = it.grupo,
                calif = it.calif,
                acreditacion = it.acreditacion,
                observaciones = it.observaciones
            )
        }
        calificacionFinalDao.insertCalificacionesFinales(entities)
    }

    fun getCalificacionFinal(matricula: String): Flow<List<CalificacionFinalEntity>> =
        calificacionFinalDao.getCalificacionesFinalesByMatricula(matricula)


    fun getProfile(matricula: String) = profileDao.getProfile(matricula)
    suspend fun insertProfile(profile: ProfileEntity) = profileDao.insertProfile(profile)
}



