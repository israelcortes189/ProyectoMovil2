package com.example.marsphotos.data.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "carga_academica",
    primaryKeys = ["matricula", "claveOficial"]
)
data class CargaEntity(
    val matricula: String,
    val claveOficial: String,
    val materia: String,
    val grupo: String,
    val docente: String,
    val creditos: Int,
    val estadoMateria: String,
    val observaciones: String,
    val semipresencial: String,
    val lunes: String,
    val martes: String,
    val miercoles: String,
    val jueves: String,
    val viernes: String,
    val sabado: String
)

