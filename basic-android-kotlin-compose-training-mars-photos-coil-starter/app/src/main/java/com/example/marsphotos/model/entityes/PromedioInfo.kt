package com.example.marsphotos.model.entityes

data class PromedioInfo(
    val promedioGral: Double,
    val creditosAcumulados: Int,
    val creditosPlan: Int,
    val materiasCursadas: Int,
    val materiasAprobadas: Int,
    val avanceCreditos: Double
)