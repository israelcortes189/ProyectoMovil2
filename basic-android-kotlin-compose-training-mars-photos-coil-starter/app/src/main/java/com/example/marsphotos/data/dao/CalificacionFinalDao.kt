package com.example.marsphotos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.marsphotos.data.Entityes.CalificacionFinalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalificacionFinalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalificacionesFinales(calificaciones: List<CalificacionFinalEntity>)

    @Query("SELECT * FROM calificaciones_finales WHERE matricula = :matricula")
    fun getCalificacionesFinalesByMatricula(matricula: String): Flow<List<CalificacionFinalEntity>>

    @Query("DELETE FROM calificaciones_finales WHERE matricula = :matricula")
    suspend fun clearCalificacionesFinalesByMatricula(matricula: String)
}
