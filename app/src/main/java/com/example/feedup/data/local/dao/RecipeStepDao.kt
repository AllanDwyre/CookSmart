package com.example.feedup.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.feedup.data.local.entities.RecipeStep


@Dao
interface RecipeStepDao {
    @Upsert
    suspend fun upsertStep(step: RecipeStep)

    @Upsert
    suspend fun upsertSteps(steps: List<RecipeStep>)

    @Delete
    suspend fun deleteStep(step: RecipeStep)

    @Query("DELETE FROM recipe_steps WHERE recipeId = :recipeId")
    suspend fun deleteAllStepsForRecipe(recipeId: String)

    @Query("SELECT * FROM recipe_steps WHERE recipeId = :recipeId ORDER BY stepNumber ASC")
    suspend fun getStepsForRecipe(recipeId: String): List<RecipeStep>

    // Réorganiser les numéros d'étapes après suppression
    @Query("UPDATE recipe_steps SET stepNumber = stepNumber - 1 WHERE recipeId = :recipeId AND stepNumber > :deletedStepNumber")
    suspend fun reorderStepsAfterDeletion(recipeId: String, deletedStepNumber: Int)
}