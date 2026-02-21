package com.example.marsphotos.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_student")
data class ProfileStudentEntity(

    @PrimaryKey
    val matricula: String,

    val nombre: String,
    val carrera: String,
    val semestre: String,
    val creditos: String
)