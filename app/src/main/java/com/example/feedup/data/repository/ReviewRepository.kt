package com.example.feedup.data.repository

import android.content.Context
import android.util.Log
import com.example.feedup.data.local.db.AppDatabase
import com.example.feedup.data.local.entities.RecipeReview
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

data class ReviewsPage(
    val reviews: List<RecipeReview>,
    val hasMore: Boolean,
    val lastDocument: DocumentSnapshot?
)

class ReviewRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val recipeReviewDao = database.recipeReviewDao()
    private val firestore = FirebaseFirestore.getInstance()

    private val lastVisibleDocuments = mutableMapOf<String, DocumentSnapshot?>()

    companion object {
        private const val TAG = "ReviewRepository"
        private const val REVIEWS_COLLECTION = "reviews"
        private const val CACHE_EXPIRY_TIME = 6 * 60 * 60 * 1000L // 6 heures
        private const val PAGE_SIZE = 10
    }

    /**
     * Crée ou met à jour une review
     */
    suspend fun createOrUpdateReview(review: RecipeReview): Boolean {
        return try {
            val updatedReview = review.copy(updatedAt = System.currentTimeMillis())
            recipeReviewDao.upsertReview(updatedReview)

            val documentId = "${review.recipeId}_${review.userId}"
            firestore.collection(REVIEWS_COLLECTION)
                .document(documentId)
                .set(updatedReview.toFirebaseMap())
                .await()

            Log.d(TAG, "Review created/updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/updating review: ${e.message}")
            false
        }
    }

    /**
     * Supprime une review
     */
    suspend fun deleteReview(recipeId: String, userId: String): Boolean {
        return try {
            // Suppression locale
            recipeReviewDao.deleteReview(recipeId, userId)

            // Suppression Firestore
            val documentId = "${recipeId}_${userId}"
            firestore.collection(REVIEWS_COLLECTION)
                .document(documentId)
                .delete()
                .await()

            Log.d(TAG, "Review deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting review: ${e.message}")
            false
        }
    }

    /**
     * Récupère une review spécifique d'un utilisateur pour une recette
     */
    suspend fun getReviewByUserAndRecipe(recipeId: String, userId: String): RecipeReview? {
        return try {
            // Essayer d'abord depuis le cache local
            val localReview = recipeReviewDao.getReviewByUserAndRecipe(recipeId, userId)
            if (localReview != null && !isCacheExpired(localReview.updatedAt)) {
                return localReview
            }

            // Sinon, récupérer depuis Firestore
            val documentId = "${recipeId}_${userId}"
            val document = firestore.collection(REVIEWS_COLLECTION)
                .document(documentId)
                .get()
                .await()

            if (document.exists()) {
                val review = document.toObject(RecipeReview::class.java)
                review?.let {
                    // Mettre à jour le cache local
                    recipeReviewDao.upsertReview(it)
                }
                review
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting review: ${e.message}")
            // Fallback sur le cache local en cas d'erreur réseau
            recipeReviewDao.getReviewByUserAndRecipe(recipeId, userId)
        }
    }

    /**
     * Récupère les reviews d'une recette avec pagination
     */
    suspend fun getReviewsForRecipe(
        recipeId: String,
        loadMore: Boolean = false,
        sortBy: ReviewSortOption = ReviewSortOption.NEWEST_FIRST
    ): ReviewsPage {
        return try {
            val cacheKey = "${recipeId}_${sortBy.name}"

            // Si ce n'est pas un "load more", reset la pagination
            if (!loadMore) {
                lastVisibleDocuments[cacheKey] = null
            }

            var query = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("recipeId", recipeId)
                .limit(PAGE_SIZE.toLong())

            // Appliquer le tri
            query = when (sortBy) {
                ReviewSortOption.NEWEST_FIRST -> query.orderBy("createdAt", Query.Direction.DESCENDING)
                ReviewSortOption.OLDEST_FIRST -> query.orderBy("createdAt", Query.Direction.ASCENDING)
                ReviewSortOption.HIGHEST_RATING -> query.orderBy("rating", Query.Direction.DESCENDING)
                ReviewSortOption.LOWEST_RATING -> query.orderBy("rating", Query.Direction.ASCENDING)
            }

            // Appliquer la pagination
            lastVisibleDocuments[cacheKey]?.let { lastDoc ->
                query = query.startAfter(lastDoc)
            }

            val querySnapshot = query.get().await()
            val reviews = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeReview::class.java)
            }

            // Mettre à jour le cache local
            recipeReviewDao.upsertReviews(reviews)

            // Sauvegarder le dernier document pour la pagination
            val lastDocument = querySnapshot.documents.lastOrNull()
            lastVisibleDocuments[cacheKey] = lastDocument

            ReviewsPage(
                reviews = reviews,
                hasMore = reviews.size == PAGE_SIZE,
                lastDocument = lastDocument
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting reviews for recipe: ${e.message}")
            // Fallback sur le cache local
            val localReviews = recipeReviewDao.getReviewsForRecipe(recipeId)
            ReviewsPage(
                reviews = localReviews,
                hasMore = false,
                lastDocument = null
            )
        }
    }

    /**
     * Récupère toutes les reviews d'un utilisateur avec pagination
     */
    suspend fun getReviewsByUser(
        userId: String,
        loadMore: Boolean = false
    ): ReviewsPage {
        return try {
            val cacheKey = "user_$userId"

            if (!loadMore) {
                lastVisibleDocuments[cacheKey] = null
            }

            var query = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE.toLong())

            lastVisibleDocuments[cacheKey]?.let { lastDoc ->
                query = query.startAfter(lastDoc)
            }

            val querySnapshot = query.get().await()
            val reviews = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeReview::class.java)
            }

            // Mettre à jour le cache local
            recipeReviewDao.upsertReviews(reviews)

            val lastDocument = querySnapshot.documents.lastOrNull()
            lastVisibleDocuments[cacheKey] = lastDocument

            ReviewsPage(
                reviews = reviews,
                hasMore = reviews.size == PAGE_SIZE,
                lastDocument = lastDocument
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting reviews by user: ${e.message}")
            val localReviews = recipeReviewDao.getReviewsByUser(userId)
            ReviewsPage(
                reviews = localReviews,
                hasMore = false,
                lastDocument = null
            )
        }
    }

    /**
     * Calcule les statistiques des reviews pour une recette
     */
    suspend fun getRecipeReviewStats(recipeId: String): ReviewStats? {
        return try {
            val querySnapshot = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            val reviews = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeReview::class.java)
            }

            if (reviews.isEmpty()) {
                return null
            }

            val totalReviews = reviews.size
            val averageRating = reviews.map { it.rating }.average().toFloat()
            val ratingDistribution = reviews.groupBy { it.rating.toInt() }
                .mapValues { it.value.size }

            ReviewStats(
                totalReviews = totalReviews,
                averageRating = averageRating,
                ratingDistribution = ratingDistribution
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipe stats: ${e.message}")
            null
        }
    }

    /**
     * Récupère les reviews en temps réel pour une recette
     */
    fun getReviewsForRecipeRealTime(recipeId: String): Flow<List<RecipeReview>> = flow {
        try {
            firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("recipeId", recipeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to reviews: ${error.message}")
                        return@addSnapshotListener
                    }

                    val reviews = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(RecipeReview::class.java)
                    } ?: emptyList()

                    // Mettre à jour le cache local
                    reviews.forEach { review ->
                        // Note: Ici vous devriez utiliser une coroutine pour l'insertion
                        // recipeReviewDao.insertOrUpdate(review)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up real-time listener: ${e.message}")
        }
    }

    /**
     * Nettoie le cache de pagination
     */
    fun clearPaginationCache() {
        lastVisibleDocuments.clear()
    }

    /**
     * Vérifie si le cache est expiré
     */
    private fun isCacheExpired(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME
    }
}

/**
 * Options de tri pour les reviews
 */
enum class ReviewSortOption {
    NEWEST_FIRST,
    OLDEST_FIRST,
    HIGHEST_RATING,
    LOWEST_RATING
}

/**
 * Statistiques des reviews pour une recette
 */
data class ReviewStats(
    val totalReviews: Int,
    val averageRating: Float,
    val ratingDistribution: Map<Int, Int> // Note (1-5) -> Nombre de reviews
)