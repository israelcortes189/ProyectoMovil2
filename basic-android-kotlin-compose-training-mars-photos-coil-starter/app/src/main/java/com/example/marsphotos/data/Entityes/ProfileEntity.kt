package com.example.marsphotos.data.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = false)
    val matricula: String,
    val nombre: String,
    val carrera: String,
    val semActual: Int,
    val cdtosAcumulados: Int
)

