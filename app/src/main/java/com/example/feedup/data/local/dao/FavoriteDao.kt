package com.example.feedup.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.feedup.data.local.entities.Favorite

@Dao
interface FavoriteDao {
    @Upsert
    suspend fun upsertFavorite(favorite: Favorite)
    @Upsert
    suspend fun upsertFavorites(favorites: List<Favorite>)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesForUser(userId: String)

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getFavorites(userId : String) : Favorite?

    @Query("SELECT * FROM favorites WHERE userId = :userId AND recipeId= :recipeId LIMIT 1")
    suspend fun getFavoriteForRecipe(userId : String, recipeId: String) : Favorite?

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getFavoritesPaginated(userId: String, limit: Int, offset: Int): List<Favorite>

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    suspend fun getFavoritesCount(userId: String): Int

}