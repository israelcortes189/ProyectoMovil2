package com.example.marsphotos.data

class LocalSNRepository(
    private val dao: ProfileStudentDao
) {

    fun getProfile() = dao.getProfile()

    suspend fun saveProfile(profile: ProfileStudent) {
        val entity = ProfileStudentEntity(
            matricula = profile.matricula,
            nombre = profile.nombre,
            carrera = profile.carrera,
            semestre = profile.semestre,
            creditos = profile.creditos
        )
        dao.insertProfile(entity)
    }
}