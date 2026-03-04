package com.example.marsphotos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.marsphotos.data.Entityes.CardexEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardex(items: List<CardexEntity>)

    @Query("DELETE FROM cardex WHERE matricula = :matricula")
    suspend fun clearCardexByMatricula(matricula: String)

    @Query("SELECT COUNT(*) FROM cardex WHERE matricula = :matricula")
    suspend fun countByMatricula(matricula: String): Int

    @Query("SELECT * FROM cardex WHERE matricula = :matricula")
    fun getCardexByMatricula(matricula: String): Flow<List<CardexEntity>>
}
