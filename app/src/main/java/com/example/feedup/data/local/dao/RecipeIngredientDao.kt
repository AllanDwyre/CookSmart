package com.example.feedup.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.feedup.data.local.entities.RecipeIngredient


@Dao
interface RecipeIngredientDao {

    @Upsert
    suspend fun upsertIngredient(ingredient: RecipeIngredient)

    @Upsert
    suspend fun upsertIngredients(ingredients: List<RecipeIngredient>)

    @Delete
    suspend fun deleteIngredient(ingredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteAllIngredientsForRecipe(recipeId: String)

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY name")
    suspend fun getIngredientsForRecipe(recipeId: String): List<RecipeIngredient>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId AND isOptional = 0")
    suspend fun getRequiredIngredientsForRecipe(recipeId: String): List<RecipeIngredient>
}