package com.example.feedup.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.feedup.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getUserProfileFlow(userId: String): Flow<UserProfile?>

    @Upsert()
    suspend fun insertOrUpdateUserProfile(userProfile: UserProfile)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfile)

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfileById(userId: String)

    @Query("SELECT COUNT(*) FROM user_profiles WHERE userId = :userId")
    suspend fun isUserProfileExists(userId: String): Int
}