package com.example.marsphotos.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow


//guardar y consultar
@Dao
interface ProfileStudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileStudentEntity)

    @Query("SELECT * FROM profile_student LIMIT 1")
    fun getProfile(): Flow<ProfileStudentEntity?>

    @Query("DELETE FROM profile_student")
    suspend fun clearProfile()
}