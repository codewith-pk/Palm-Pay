package com.codewithpk.palmpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EnrolledPalmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace if user re-enrolls
    suspend fun insertEnrolledPalm(enrolledPalm: EnrolledPalm)

    @Query("SELECT * FROM enrolled_palms WHERE userId = :userId")
    fun getEnrolledPalmByUserId(userId: String): Flow<EnrolledPalm?>

    @Query("SELECT * FROM enrolled_palms LIMIT 1") // For this demo project , just get any enrolled palm
    fun getAnyEnrolledPalm(): Flow<EnrolledPalm?>

    @Query("DELETE FROM enrolled_palms WHERE userId = :userId")
    suspend fun deleteEnrolledPalm(userId: String)
}