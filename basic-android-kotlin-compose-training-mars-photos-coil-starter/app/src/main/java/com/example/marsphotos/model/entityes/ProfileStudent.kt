package com.example.marsphotos.model.entityes


data class ProfileStudent(
    val matricula: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val semActual: Int = 0,
    val cdtosAcumulados: Int = 0
)
