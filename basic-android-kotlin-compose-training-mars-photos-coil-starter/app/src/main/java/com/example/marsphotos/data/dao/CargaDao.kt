package com.example.marsphotos.data.dao

import android.content.ContentValues
import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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


    // --- Añadir estas dos APIs para ContentProvider ---
    @Query("""
        SELECT rowid AS _id,
               matricula,
               claveOficial,
               materia,
               grupo,
               docente,
               creditos,
               estadoMateria,
               observaciones,
               semipresencial,
               lunes,
               martes,
               miercoles,
               jueves,
               viernes,
               sabado
        FROM carga_academica
        WHERE (:matricula IS NULL OR matricula = :matricula)
    """)
    fun getCargaCursor(matricula: String?): Cursor

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCargaEntity(entity: CargaEntity): Long

    @Transaction
    fun insertFromContentValues(values: ContentValues): Long {
        val matricula = values.getAsString("matricula") ?: throw IllegalArgumentException("matricula required")
        val claveOficial = values.getAsString("claveOficial") ?: throw IllegalArgumentException("claveOficial required")
        val entity = CargaEntity(
            matricula = matricula,
            claveOficial = claveOficial,
            materia = values.getAsString("materia") ?: "",
            grupo = values.getAsString("grupo") ?: "",
            docente = values.getAsString("docente") ?: "",
            creditos = values.getAsInteger("creditos") ?: 0,
            estadoMateria = values.getAsString("estadoMateria") ?: "",
            observaciones = values.getAsString("observaciones") ?: "",
            semipresencial = values.getAsString("semipresencial") ?: "",
            lunes = values.getAsString("lunes") ?: "",
            martes = values.getAsString("martes") ?: "",
            miercoles = values.getAsString("miercoles") ?: "",
            jueves = values.getAsString("jueves") ?: "",
            viernes = values.getAsString("viernes") ?: "",
            sabado = values.getAsString("sabado") ?: ""
        )
        return insertCargaEntity(entity)
    }
}
