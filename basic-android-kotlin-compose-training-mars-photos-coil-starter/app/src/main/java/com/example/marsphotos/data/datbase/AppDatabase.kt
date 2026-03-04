package com.example.marsphotos.data.datbase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
/**
 * Clase de base de datos con patrón Singleton.
 */
@Database(
    entities = [ProfileEntity::class, CardexEntity::class, CargaEntity::class, CalificacionUnidadEntity::class, CalificacionFinalEntity::class],
    version = 6, //
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun cardexDao(): CardexDao
    abstract fun cargaDao(): CargaDao
    abstract fun CalificacionUnidadDao(): CalificacionUnidadDao
    abstract fun CalificacionFinalDao(): CalificacionFinalDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sicenet_database"
                )
                    .fallbackToDestructiveMigration() // destruye y recrea si cambia el esquema
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

