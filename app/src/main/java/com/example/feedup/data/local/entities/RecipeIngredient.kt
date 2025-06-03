package com.example.feedup.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "name"],
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        childColumns = ["recipeId"],
        parentColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["recipeId"])]
)
data class RecipeIngredient(
    val recipeId: String = "",
    val name: String = "",

    val imageUrl: String = "",
    val quantity: String = "", // "200g", "2 pièces", etc.
    val isOptional: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
) {
    constructor() : this("","")

    fun toFirebaseMap(): Map<String, Any> = mapOf(
        "name" to name,
        "imageUrl" to imageUrl,
        "quantity" to quantity,
        "isOptional" to isOptional
    )
}