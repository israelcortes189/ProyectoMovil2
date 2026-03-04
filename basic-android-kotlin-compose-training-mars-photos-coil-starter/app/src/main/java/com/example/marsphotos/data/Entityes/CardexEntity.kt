package com.example.marsphotos.data.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cardex",
    primaryKeys = ["matricula", "claveMateria"])
data class CardexEntity(
    val matricula: String,
    val claveMateria: String,
    val claveOficial: String,
    val materia: String,
    val creditos: Int,
    val calificacion: Int,
    val acreditacion: String,
    val semestre: String,
    val periodo: String,
    val anio: String
)

