package com.example.feedup.data.repository

import android.content.Context
import android.util.Log
import com.example.feedup.data.local.db.AppDatabase
import com.example.feedup.data.local.entities.Favorite
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class PaginatedFavorites(
    val favorites: List<Favorite>,
    val hasMore: Boolean,
    val totalCount: Int,
    val currentPage: Int
)

class FavoriteRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val favoriteDao = database.favoriteDao()
    private val firestore = FirebaseFirestore.getInstance()

    private var lastVisibleDocument: DocumentSnapshot? = null

    companion object {
        private const val TAG = "FavoriteRepository"
        private const val USER_COLLECTION = "users"
        private const val FAVORITE_COLLECTION = "favorites"
        private const val CACHE_EXPIRY_TIME = 6 * 60 * 60 * 1000L
        private const val PAGE_SIZE = 15
    }

    suspend fun createFavorite(favorite: Favorite): Boolean{
        return try {
            favoriteDao.upsertFavorite(favorite)

            val data = favorite.toFirebaseMap().toMutableMap()

            firestore.collection(USER_COLLECTION)
                .document(favorite.userId)
                .collection(FAVORITE_COLLECTION).document(favorite.recipeId)
                .set(data)
            true
        }
        catch (e: Exception) {
            Log.e(TAG, "Error creating recipe: ${e.message}")
            false
        }
    }

    suspend fun deleteFavorite(favorite: Favorite): Boolean{
        return try {
            favoriteDao.deleteFavorite(favorite)

            firestore.collection(USER_COLLECTION)
                .document(favorite.userId)
                .collection(FAVORITE_COLLECTION).document(favorite.recipeId)
                .delete()
            true
        }
        catch (e: Exception) {
            Log.e(TAG, "Error deleting recipe: ${e.message}")
            false
        }
    }

    suspend fun getFavoriteForRecipe(userId: String, recipeId: String, forceRefresh: Boolean = false): Favorite?{
        try {
            val localFavorite =  favoriteDao.getFavoriteForRecipe(userId, recipeId)

            val isCacheValid = localFavorite?.let { favorite ->
                (System.currentTimeMillis() - favorite.updatedAt) < CACHE_EXPIRY_TIME
            } ?: false

            if (isCacheValid && !forceRefresh) {
                Log.d(TAG, "Returning cached recipe: $recipeId")
                return localFavorite
            }

            val favoriteDoc = firestore.collection(USER_COLLECTION)
                .document(userId)
                .collection(FAVORITE_COLLECTION).document(recipeId)
                .get()
                .await()

            if (favoriteDoc.exists()) {
                val favorite = favoriteDoc.toObject(Favorite::class.java)
                favorite?.let {
                    favoriteDao.upsertFavorite(it)
                }
                return favorite!!
            } else {
                localFavorite?.let { favoriteDao.deleteFavorite(it) }
               return null
            }

        }
        catch (e: Exception) {
            Log.e(TAG, "Error getting recipe: ${e.message}")
            return favoriteDao.getFavoriteForRecipe(userId, recipeId)
        }
    }

    suspend fun isFavorite(userId: String, recipeId: String): Boolean {
        return getFavoriteForRecipe(userId, recipeId) != null
    }

    // Pagination

    suspend fun getFavorites(
        userId: String,
        page: Int = 0,
        pageSize: Int = PAGE_SIZE,
        forceRefresh: Boolean = false,
        resetPagination: Boolean = false
    ): PaginatedFavorites {
        return try {
            if (resetPagination) {
                lastVisibleDocument = null
            }

            val localFavorites = favoriteDao.getFavoritesPaginated(
                userId = userId,
                limit = pageSize,
                offset = page * pageSize
            )

            val totalLocalCount = favoriteDao.getFavoritesCount(userId)

            val isCacheValid = if (localFavorites.isNotEmpty()) {
                val oldestFavorite = localFavorites.minByOrNull { it.updatedAt }
                oldestFavorite?.let { favorite ->
                    (System.currentTimeMillis() - favorite.updatedAt) < CACHE_EXPIRY_TIME
                } == true
            } else false

            if (isCacheValid && !forceRefresh && page > 0) {
                Log.d(TAG, "Returning cached favorites for page: $page")
                return PaginatedFavorites(
                    favorites = localFavorites,
                    hasMore = (page + 1) * pageSize < totalLocalCount,
                    totalCount = totalLocalCount,
                    currentPage = page
                )
            }

            val query = if (page == 0 || resetPagination) {
                // Première page ou reset
                firestore.collection(USER_COLLECTION)
                    .document(userId)
                    .collection(FAVORITE_COLLECTION)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
            } else {
                val lastVisible = lastVisibleDocument
                if (lastVisible != null) {
                    firestore.collection(USER_COLLECTION)
                        .document(userId)
                        .collection(FAVORITE_COLLECTION)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(pageSize.toLong())
                } else {
                    Log.d(TAG, "No lastVisible document, using local cache")
                    return PaginatedFavorites(
                        favorites = localFavorites,
                        hasMore = (page + 1) * pageSize < totalLocalCount,
                        totalCount = totalLocalCount,
                        currentPage = page
                    )
                }
            }

            val snapshot = query.get().await()
            val firebaseFavorites = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Favorite::class.java)?.copy(
                    updatedAt = System.currentTimeMillis()
                )
            }

            // Sauvegarder le dernier document visible pour la pagination
            if (snapshot.documents.isNotEmpty()) {
                lastVisibleDocument= snapshot.documents.last()
            }

            // Si c'est la première page ou un reset, vider le cache local
            if (page == 0 || resetPagination) {
                favoriteDao.deleteAllFavoritesForUser(userId)
            }

            // Sauvegarder en local
            if (firebaseFavorites.isNotEmpty()) {
                favoriteDao.upsertFavorites(firebaseFavorites)
            }

            // Calculer s'il y a plus de résultats
            val hasMore = snapshot.documents.size >= pageSize

            // Compter le total (approximatif)
            val totalCount = if (page == 0) {
                firebaseFavorites.size + if (hasMore) pageSize else 0
            } else {
                favoriteDao.getFavoritesCount(userId)
            }

            PaginatedFavorites(
                favorites = firebaseFavorites,
                hasMore = hasMore,
                totalCount = totalCount,
                currentPage = page
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting favorites: ${e.message}")
            // En cas d'erreur, retourner les données du cache local
            val localFavorites = favoriteDao.getFavoritesPaginated(
                userId = userId,
                limit = pageSize,
                offset = page * pageSize
            )
            val totalLocalCount = favoriteDao.getFavoritesCount(userId)

            PaginatedFavorites(
                favorites = localFavorites,
                hasMore = (page + 1) * pageSize < totalLocalCount,
                totalCount = totalLocalCount,
                currentPage = page
            )
        }
    }

    /**
     * Refresh complet des favoris depuis Firebase
     */
    suspend fun refreshAllFavorites(userId: String): Boolean {
        return try {
            favoriteDao.deleteAllFavoritesForUser(userId)
            lastVisibleDocument = null

            val snapshot = firestore.collection(USER_COLLECTION)
                .document(userId)
                .collection(FAVORITE_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val favorites = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Favorite::class.java)?.copy(
                    updatedAt = System.currentTimeMillis()
                )
            }

            if (favorites.isNotEmpty()) {
                favoriteDao.upsertFavorites(favorites)
            }

            Log.d(TAG, "Refreshed ${favorites.size} favorites for user: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing favorites: ${e.message}")
            false
        }
    }
}
