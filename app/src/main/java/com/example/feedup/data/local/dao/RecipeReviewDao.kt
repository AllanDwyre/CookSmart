package com.example.feedup.data.local.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.feedup.data.local.entities.RecipeReview


@Dao
interface RecipeReviewDao {

    @Upsert
    suspend fun upsertReview(review: RecipeReview)

    @Upsert
    suspend fun upsertReviews(reviews: List<RecipeReview>)

    @Query("DELETE FROM recipe_reviews WHERE recipeId = :recipeId AND userId = :userId")
    suspend fun deleteReview(recipeId: String, userId: String)

    @Query("DELETE FROM recipe_reviews WHERE recipeId = :recipeId")
    suspend fun deleteAllReviewsForRecipe(recipeId: String)

    @Query("DELETE FROM recipe_reviews WHERE userId = :userId")
    suspend fun deleteAllReviewsByUser(userId: String)

    @Query("SELECT * FROM recipe_reviews WHERE recipeId = :recipeId AND userId = :userId")
    suspend fun getReviewByUserAndRecipe(recipeId: String, userId: String): RecipeReview?

    @Query("SELECT * FROM recipe_reviews WHERE recipeId = :recipeId ORDER BY createdAt DESC")
    suspend fun getReviewsForRecipe(recipeId: String): List<RecipeReview>

    @Query("SELECT * FROM recipe_reviews WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getReviewsByUser(userId: String): List<RecipeReview>

    // === Statistiques des avis ===
    @Query("SELECT COUNT(*) FROM recipe_reviews WHERE recipeId = :recipeId")
    suspend fun getReviewCountForRecipe(recipeId: String): Int

    @Query("SELECT AVG(rating) FROM recipe_reviews WHERE recipeId = :recipeId")
    suspend fun getAverageRatingForRecipe(recipeId: String): Float?

    @Query("SELECT * FROM recipe_reviews WHERE recipeId = :recipeId AND rating >= :minRating ORDER BY createdAt DESC")
    suspend fun getReviewsWithMinRating(recipeId: String, minRating: Float): List<RecipeReview>

}