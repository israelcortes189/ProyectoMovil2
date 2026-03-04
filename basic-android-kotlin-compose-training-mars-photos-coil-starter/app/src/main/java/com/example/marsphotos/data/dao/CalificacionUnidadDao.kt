package com.example.marsphotos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.marsphotos.data.Entityes.CalificacionUnidadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalificacionUnidadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalificaciones(calificaciones: List<CalificacionUnidadEntity>)

    @Query("SELECT * FROM calificaciones_unidad WHERE matricula = :matricula")
    fun getCalificacionesByMatricula(matricula: String): Flow<List<CalificacionUnidadEntity>>

    @Query("DELETE FROM calificaciones_unidad WHERE matricula = :matricula")
    suspend fun clearCalificacionesByMatricula(matricula: String)
}
