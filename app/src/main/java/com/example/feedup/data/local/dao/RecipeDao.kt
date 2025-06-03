package com.example.feedup.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.feedup.data.local.entities.Recipe
import com.example.feedup.data.local.entities.RecipeWithDetails

@Dao
interface RecipeDao {
    @Upsert
    suspend fun upsertRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE isPublic == 1")
    suspend fun deleteAllPublicRecipes()

    @Query("DELETE FROM recipes WHERE recipeId = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)

    // === Récupération simple ===
    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId")
    suspend fun getRecipeById(recipeId: String): Recipe?

    // === Requêtes avec relations ===
    @Transaction
    @Query("""
        SELECT recipes.*, 
               IFNULL(AVG(recipe_reviews.rating), 0) AS averageRating,
               COUNT(recipe_reviews.recipeId) AS reviewCount
        FROM recipes
        LEFT JOIN recipe_reviews ON recipes.recipeId = recipe_reviews.recipeId
        WHERE recipes.isPublic = 1
        GROUP BY recipes.recipeId
        ORDER BY recipes.createdAt DESC
    """)
    suspend fun getPublicRecipesWithDetails(): List<RecipeWithDetails>

    @Transaction
    @Query("""
        SELECT recipes.*, 
               IFNULL(AVG(recipe_reviews.rating), 0) AS averageRating,
               COUNT(recipe_reviews.recipeId) AS reviewCount
        FROM recipes
        LEFT JOIN recipe_reviews ON recipes.recipeId = recipe_reviews.recipeId
        WHERE recipes.recipeId = :recipeId
        GROUP BY recipes.recipeId
    """)
    suspend fun getRecipeWithDetails(recipeId: String): RecipeWithDetails?

    @Transaction
    @Query("""
        SELECT recipes.*, 
               IFNULL(AVG(recipe_reviews.rating), 0) AS averageRating,
               COUNT(recipe_reviews.recipeId) AS reviewCount
        FROM recipes
        LEFT JOIN recipe_reviews ON recipes.recipeId = recipe_reviews.recipeId
        WHERE recipes.userId = :userId
        GROUP BY recipes.recipeId
        ORDER BY recipes.createdAt DESC
    """)
    suspend fun getUserRecipesWithDetails(userId: String): List<RecipeWithDetails>


}
