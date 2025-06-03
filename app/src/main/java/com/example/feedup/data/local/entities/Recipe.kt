package com.example.feedup.data.local.entities


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.feedup.data.local.db.Converters
import java.util.UUID

@Entity(
    tableName = "recipes",
    indices = [Index(value = ["userId"])]
)
@TypeConverters(Converters::class)
data class Recipe(
    @PrimaryKey
    val recipeId: String = UUID.randomUUID().toString(),
    val userId: String = "",

    // Informations de base
    val title: String = "",
    val description: String = "",
    val servings: Int = 1,
    val cookingTime: Int = 0, // temps de préparation et de cuisson
    val difficulty: String = "", // facile, moyen, difficile
    val cost: String = "", // bon marché, cher, etc.
    val category: String = "", // entrée, plat, dessert, etc.
    val origin: String = "", // français, italien, libanais, etc.

    // Listes d'informations
    val allergens: List<String> = emptyList(), // ["gluten", "lactose"]
    val dietTags: List<String> = emptyList(), // ["vegan", "vegetarian"]
    val equipments: List<Equipment> = emptyList(), // objets avec name et image_url

    // Médias
    val imageUrl: String = "", // pour l'instant qu'une photo par recette
    val videoUrl: String = "", // pour la vidéo de recommandation

    // Métadonnées
    val isPublic: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")

    fun toFirebaseMap(): Map<String, Any> = mapOf(
        "recipeId" to recipeId,
        "userId" to userId,
        "title" to title,
        "description" to description,
        "servings" to servings,
        "cookingTime" to cookingTime,
        "difficulty" to difficulty,
        "cost" to cost,
        "category" to category,
        "origin" to origin,
        "allergens" to allergens,
        "dietTags" to dietTags,
        "equipments" to equipments.map { mapOf("name" to it.name, "imageUrl" to it.imageUrl) },
        "imageUrl" to imageUrl,
        "videoUrl" to videoUrl,
        "isPublic" to isPublic,
        "lastUpdated" to lastUpdated,
        "createdAt" to createdAt
    )
}