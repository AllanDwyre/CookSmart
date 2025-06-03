package com.example.feedup.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithDetails(
    @Embedded val recipe: Recipe,

    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    val steps: List<RecipeStep>,

    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    val ingredients: List<RecipeIngredient>,

    val averageRating: Float,
    val reviewCount: Int,
)