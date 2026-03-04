package com.example.marsphotos.data.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calificaciones_unidad",
        primaryKeys = ["matricula","materia","grupo"])
data class CalificacionUnidadEntity(
    val matricula: String,
    val materia: String,
    val grupo: String,
    val observaciones: String,
    val unidadesActivas: String,
    val c1: String?,
    val c2: String?,
    val c3: String?,
    val c4: String?,
    val c5: String?,
    val c6: String?,
    val c7: String?,
    val c8: String?,
    val c9: String?,
    val c10: String?,
    val c11: String?,
    val c12: String?,
    val c13: String?
)
