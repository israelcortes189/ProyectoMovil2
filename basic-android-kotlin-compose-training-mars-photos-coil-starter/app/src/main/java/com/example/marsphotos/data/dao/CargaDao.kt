package com.example.marsphotos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.marsphotos.data.Entityes.CargaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CargaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarga(carga: List<CargaEntity>)

    @Query("SELECT * FROM carga_academica WHERE matricula = :matricula")
    fun getCargaByMatricula(matricula: String): Flow<List<CargaEntity>>

    @Query("DELETE FROM carga_academica WHERE matricula = :matricula")
    suspend fun clearCargaByMatricula(matricula: String)
}
