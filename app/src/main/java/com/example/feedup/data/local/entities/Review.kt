package com.example.feedup.data.local.entities


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.google.firebase.firestore.PropertyName

@Entity(
    tableName = "recipe_reviews",
    primaryKeys = ["recipeId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["recipeId"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index(value = ["recipeId"]),  Index(value = ["userId"]) ]
)
data class RecipeReview(
    val recipeId: String = "",
    val userId: String = "",

    val rating: Float = 0f,
    val comment: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("lastUpdated")
    @set:PropertyName("lastUpdated")
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "")

    fun toFirebaseMap(): Map<String, Any> = mapOf(
        "recipeId" to recipeId,
        "userId" to userId,
        "rating" to rating,
        "comment" to comment,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
