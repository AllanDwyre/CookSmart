package com.example.feedup.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "recipe_steps",
    primaryKeys = ["recipeId", "stepNumber"],
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        childColumns = ["recipeId"],
        parentColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["recipeId"])]
)
data class RecipeStep(
    val recipeId: String = "",
    val stepNumber: Int = 0, // Ordre des étapes
    val instruction: String = "",

    // Paramètres de timer
    val hasTimer: Boolean = false,
    val timerDurationMinutes: Int? = null,
    val timerLabel: String = "", // "Cuisson", "Repos", etc.

    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", 0)

    fun toFirebaseMap(): Map<String, Any> = mapOf(
        "stepNumber" to stepNumber,
        "instruction" to instruction,
        "hasTimer" to hasTimer,
        "timerDurationMinutes" to (timerDurationMinutes ?: 0),
        "timerLabel" to timerLabel
    )
}