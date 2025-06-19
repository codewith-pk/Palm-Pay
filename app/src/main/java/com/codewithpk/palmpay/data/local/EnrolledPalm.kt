package com.codewithpk.palmpay.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "enrolled_palms")
data class EnrolledPalm(
    @PrimaryKey val userId: String, // User ID is the primary key for enrolled palm
    val palmImagePath: String, // Path to the captured image of the enrolled palm
    val mockFeatureData: String, // Simulated feature data (e.g., a hash or unique string)
    val enrollmentTimestamp: Long
)