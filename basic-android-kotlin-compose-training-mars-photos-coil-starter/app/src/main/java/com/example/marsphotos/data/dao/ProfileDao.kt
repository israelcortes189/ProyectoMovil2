package com.example.marsphotos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.marsphotos.data.Entityes.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profile WHERE matricula = :matricula")
    fun getProfile(matricula: String): Flow<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profile")
    fun getAllProfiles(): Flow<List<ProfileEntity>>
}
