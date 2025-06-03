package com.example.feedup.data.local.entities


import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.example.feedup.data.local.db.Converters

@Entity(
    tableName = "favorites",
    primaryKeys = ["recipeId", "userId"],
    indices = [Index(value = ["userId"]), Index(value = ["recipeId"])]
)
@TypeConverters(Converters::class)
data class Favorite(
    val recipeId: String = "",
    val userId: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")

    fun toFirebaseMap(): Map<String, Any> = mapOf(
        "recipeId" to recipeId,
        "userId" to userId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}