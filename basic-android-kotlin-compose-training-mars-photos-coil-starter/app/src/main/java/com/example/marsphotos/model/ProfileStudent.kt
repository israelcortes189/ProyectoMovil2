package com.example.marsphotos.model

import kotlinx.serialization.Serializable


data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val semestre: String = "",
    val creditos: String = ""
)
