package com.example.marsphotos.data.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calificaciones_finales",
        primaryKeys = ["matricula","materia","grupo"])
data class CalificacionFinalEntity(
    val matricula: String,
    val materia: String,
    val grupo: String,
    val calif: Int,
    val acreditacion: String,
    val observaciones: String
)

