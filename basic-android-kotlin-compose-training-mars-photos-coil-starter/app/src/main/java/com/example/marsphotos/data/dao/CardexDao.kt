package com.example.marsphotos.data.dao

import android.content.ContentValues
import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    // --- Añadir estas dos APIs para ContentProvider ---
    @Query("""
        SELECT rowid AS _id,
               matricula,
               claveMateria,
               claveOficial,
               materia,
               creditos,
               calificacion,
               acreditacion,
               semestre,
               periodo,
               anio
        FROM cardex
        WHERE (:matricula IS NULL OR matricula = :matricula)
    """)
    fun getCardexCursor(matricula: String?): Cursor

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCardexEntity(entity: CardexEntity): Long

    @Transaction
    fun insertFromContentValues(values: ContentValues): Long {
        val matricula = values.getAsString("matricula") ?: throw IllegalArgumentException("matricula required")
        val claveMateria = values.getAsString("claveMateria") ?: throw IllegalArgumentException("claveMateria required")
        val entity = CardexEntity(
            matricula = matricula,
            claveMateria = claveMateria,
            claveOficial = values.getAsString("claveOficial") ?: "",
            materia = values.getAsString("materia") ?: "",
            creditos = values.getAsInteger("creditos") ?: 0,
            calificacion = values.getAsInteger("calificacion") ?: 0,
            acreditacion = values.getAsString("acreditacion") ?: "",
            semestre = values.getAsString("semestre") ?: "",
            periodo = values.getAsString("periodo") ?: "",
            anio = values.getAsString("anio") ?: ""
        )
        return insertCardexEntity(entity)
    }
}
