package com.example.feedup.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val target: Int,
    val unlockedAt: Long? = null,
    val category: String // "cooking", "time", "recipes"
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: String = "user_stats",
    val totalCookingTime: Int = 0, // en minutes
    val recipesCompleted: Int = 0,
    val ovenUsageTime: Int = 0
)