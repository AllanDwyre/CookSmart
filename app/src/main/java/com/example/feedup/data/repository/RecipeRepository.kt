package com.example.feedup.data.repository

import android.content.Context
import android.util.Log
import com.example.feedup.data.local.db.AppDatabase
import com.example.feedup.data.local.entities.Equipment
import com.example.feedup.data.local.entities.Recipe
import com.example.feedup.data.local.entities.RecipeIngredient
import com.example.feedup.data.local.entities.RecipeStep
import com.example.feedup.data.local.entities.RecipeWithDetails
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RecipeRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val recipeDao = database.recipeDao()
    private val recipeStepDao = database.recipeStepDao()
    private val recipeIngredientDao = database.recipeIngredientDao()
    private val firestore = FirebaseFirestore.getInstance()

    private var lastVisibleDocument: DocumentSnapshot? = null

    companion object {
        private const val TAG = "RecipeRepository"
        private const val RECIPES_COLLECTION = "recipes"
        private const val REVIEWS_COLLECTION = "reviews"
        private const val CACHE_EXPIRY_TIME = 6 * 60 * 60 * 1000L // 6 heures pour les recettes
        private const val PAGE_SIZE = 9
    }

    // === Gestion des recettes complètes ===
    suspend fun createRecipe(
        recipe: Recipe,
        steps: List<RecipeStep>,
        ingredients: List<RecipeIngredient>
    ): Boolean {
        return try {
            val recipeId = if (recipe.recipeId.isEmpty()) {
                UUID.randomUUID().toString()
            } else {
                recipe.recipeId
            }

            val finalRecipe = recipe.copy(
                recipeId = recipeId,
                lastUpdated = System.currentTimeMillis()
            )
            val finalSteps = steps.map { it.copy(recipeId = recipeId) }
            val finalIngredients = ingredients.map { it.copy(recipeId = recipeId) }

            recipeDao.upsertRecipe(finalRecipe)
            recipeStepDao.upsertSteps(finalSteps)
            recipeIngredientDao.upsertIngredients(finalIngredients)

            Log.d(TAG, "Recipe saved locally: $recipeId")

            if (finalRecipe.isPublic) {
                syncRecipeToFirebase(finalRecipe, finalSteps, finalIngredients)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating recipe: ${e.message}")
            false
        }
    }

    suspend fun getRecipeWithDetails(
        recipeId: String,
        forceRefresh: Boolean = false
    ): RecipeWithDetails? {
        return try {
            val localRecipe = recipeDao.getRecipeWithDetails(recipeId)

            val isCacheValid = localRecipe?.recipe?.let { recipe ->
                (System.currentTimeMillis() - recipe.lastUpdated) < CACHE_EXPIRY_TIME
            } == true

            if (isCacheValid && !forceRefresh) {
                Log.d(TAG, "Returning cached recipe: $recipeId")
                return localRecipe
            }

            Log.d(TAG, "Fetching recipe from Firebase: $recipeId")
            val firebaseRecipe = getRecipeFromFirebase(recipeId)

            firebaseRecipe?.let { recipe ->
                // Mettre à jour le cache local
                recipeDao.upsertRecipe(
                    recipe.recipe.copy(lastUpdated = System.currentTimeMillis()),
                )
                recipeStepDao.upsertSteps(recipe.steps)
                recipeIngredientDao.upsertIngredients(recipe.ingredients)

                Log.d(TAG, "Recipe cached locally: $recipeId")
                recipe
            } ?: localRecipe

        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipe: ${e.message}")
            recipeDao.getRecipeWithDetails(recipeId)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRecipeFromFirebaseData(
        documentId: String,
        data: Map<String, Any>,
    ): Recipe {
        val recipeData = data["recipe"] as? Map<String, Any> ?: data
        val equipmentsData = recipeData["equipments"] as? List<Map<String, Any>> ?: emptyList()
        val equipments = equipmentsData.map { equipmentMap ->
            Equipment(
                name = equipmentMap["name"] as? String ?: "",
                imageUrl = equipmentMap["imageUrl"] as? String ?: "",
            )
        }

        return Recipe(
            recipeId = recipeData["recipeId"] as? String ?: documentId,
            userId = recipeData["userId"] as? String ?: "",
            title = recipeData["title"] as? String ?: "",
            description = recipeData["description"] as? String ?: "",
            cookingTime = (recipeData["cookingTime"] as? Long)?.toInt() ?: 0,
            difficulty = recipeData["difficulty"] as? String ?: "",
            cost = recipeData["cost"] as? String ?: "",
            category = recipeData["category"] as? String ?: "",
            origin = recipeData["origin"] as? String ?: "",
            dietTags = recipeData["dietTags"] as? List<String> ?: emptyList(),
            allergens = recipeData["allergens"] as? List<String> ?: emptyList(),
            equipments = equipments,
            servings = (recipeData["servings"] as? Long)?.toInt() ?: 1,
            imageUrl = recipeData["imageUrl"] as? String ?: "",
            isPublic = recipeData["isPublic"] as? Boolean == true,
            createdAt = recipeData["createdAt"] as? Long ?: System.currentTimeMillis(),
            lastUpdated = recipeData["lastUpdated"] as? Long ?: System.currentTimeMillis(),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseIngredientsFromFirebaseData(
        recipeId: String,
        data: Map<String, Any>,
    ): List<RecipeIngredient> {
        val ingredientsData = data["ingredients"] as? List<Map<String, Any>> ?: emptyList()
        return ingredientsData.map { ingredientMap ->
            RecipeIngredient(
                recipeId = recipeId,
                name = ingredientMap["name"] as? String ?: "",
                isOptional = ingredientMap["isOptional"] as? Boolean == true,
                quantity = ingredientMap["quantity"] as? String ?: "",
                imageUrl = ingredientMap["imageUrl"] as? String ?: "",
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseStepsFromFirebaseData(
        recipeId: String,
        data: Map<String, Any>,
    ): List<RecipeStep> {
        val stepsData = data["steps"] as? List<Map<String, Any>> ?: emptyList()
        return stepsData.mapIndexed { index, stepMap ->
            RecipeStep(
                recipeId = recipeId,
                stepNumber = stepMap["stepNumber"] as? Int ?: (index + 1),
                instruction = stepMap["instruction"] as? String ?: "",
                hasTimer = stepMap["hasTimer"] as? Boolean == true,
                timerDurationMinutes = (stepMap["timerDurationMinutes"] as? Long)?.toInt(),
                timerLabel = stepMap["timerLabel"] as? String ?: "",
            )
        }
    }

    private suspend fun getRecipeFromFirebase(recipeId: String): RecipeWithDetails? {
        return try {
            val recipeDoc = firestore.collection(RECIPES_COLLECTION)
                .document(recipeId)
                .get()
                .await()

            if (!recipeDoc.exists()) return null

            val data = recipeDoc.data ?: return null

            val recipe = parseRecipeFromFirebaseData(recipeId, data)
            val ingredients = parseIngredientsFromFirebaseData(recipeId, data)
            val steps = parseStepsFromFirebaseData(recipeId, data)

            val (averageRating, reviewCount) = getRecipeRatingFromFirebase(recipeId)

            RecipeWithDetails(
                recipe = recipe,
                steps = steps,
                ingredients = ingredients,
                averageRating = averageRating,
                reviewCount = reviewCount
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recipe from Firebase: ${e.message}")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun getPublicRecipesWithDetailsFromFirebase(
        pageSize: Int
    ): List<RecipeWithDetails> {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            val query = if (lastVisibleDocument != null) {
                firestore.collection("recipes")
                    .whereEqualTo("recipe.isPublic", true)
                    .orderBy("recipe.createdAt", Query.Direction.DESCENDING)
                    .startAfter(lastVisibleDocument!!)
                    .limit(pageSize.toLong())
            } else {
                firestore.collection("recipes")
                    .whereEqualTo("recipe.isPublic", true)
                    .orderBy("recipe.createdAt", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
            }

            val snapshot = query.get().await()
            Log.d(TAG, "Firebase query returned ${snapshot.documents.size} documents")

            lastVisibleDocument = if (snapshot.documents.isNotEmpty()) {
                snapshot.documents.last()
            } else {
                null
            }

            val recipeIds = snapshot.documents.map { it.id }
            val ratingsMap = getBatchRecipeRatingsFromFirebase(recipeIds)

            snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    val recipe = parseRecipeFromFirebaseData(document.id, data)
                    val ingredients = parseIngredientsFromFirebaseData(document.id, data)
                    val steps = parseStepsFromFirebaseData(document.id, data)

                    val (averageRating, reviewCount) = ratingsMap[document.id] ?: Pair(0f, 0)

                    Log.d(TAG, "Recipe ${document.id}: rating=$averageRating, reviews=$reviewCount")

                    RecipeWithDetails(
                        recipe = recipe,
                        ingredients = ingredients,
                        steps = steps,
                        averageRating = averageRating,
                        reviewCount = reviewCount
                    )

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing recipe document ${document.id}: ${e.message}")
                    null
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching public recipes with details from Firebase: ${e.message}")
            emptyList()
        }
    }


    private suspend fun getBatchRecipeRatingsFromFirebase(recipeIds: List<String>): Map<String, Pair<Float, Int>> {
        val ratingsMap = mutableMapOf<String, Pair<Float, Int>>()

        try {
            recipeIds.chunked(10).forEach { batch ->
                batch.forEach { recipeId ->
                    try {
                        val (rating, count) = getRecipeRatingFromFirebase(recipeId)
                        ratingsMap[recipeId] = Pair(rating, count)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting rating for recipe $recipeId: ${e.message}")
                        ratingsMap[recipeId] = Pair(0f, 0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch rating fetch: ${e.message}")
        }

        return ratingsMap
    }

    private suspend fun syncRecipeToFirebase(
        recipe: Recipe,
        steps: List<RecipeStep>,
        ingredients: List<RecipeIngredient>
    ) {
        try {
            val recipeData = recipe.toFirebaseMap().toMutableMap()
            recipeData["steps"] = steps.map { it.toFirebaseMap() }
            recipeData["ingredients"] = ingredients.map { it.toFirebaseMap() }

            firestore.collection(RECIPES_COLLECTION)
                .document(recipe.recipeId)
                .set(recipeData)
                .await()
            Log.d(TAG, "Recipe synced to Firebase: ${recipe.recipeId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing recipe to Firebase: ${e.message}")
        }
    }

    // === Récupération des listes ===

    suspend fun getUserRecipes(userId: String): List<RecipeWithDetails> {
        return try {
            recipeDao.getUserRecipesWithDetails(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user recipes: ${e.message}")
            emptyList()
        }
    }

    fun resetPagination() {
        lastVisibleDocument = null
    }

    suspend fun getPublicRecipesWithDetails(
        page: Int = 0,
        pageSize: Int = PAGE_SIZE,
        forceRefresh: Boolean = false
    ): List<RecipeWithDetails> {
        return try {
            if (page == 0 || forceRefresh) {
                lastVisibleDocument = null
            }

            if (page > 0) {
                Log.d(TAG, "Loading page $page from Firebase directly")
                return getPublicRecipesWithDetailsFromFirebase(pageSize)
            }

            // Pour la première page, vérifier le cache
            val localRecipes = recipeDao.getPublicRecipesWithDetails()
            val isCacheValid = localRecipes.isNotEmpty() &&
                    localRecipes.firstOrNull()?.recipe?.let { recipe ->
                        (System.currentTimeMillis() - recipe.lastUpdated) < CACHE_EXPIRY_TIME
                    } == true

            if (isCacheValid && !forceRefresh) {
                Log.d(TAG, "Returning cached public recipes with details for page: $page")
                return localRecipes.take(pageSize)
            }

            Log.d(TAG, "Fetching public recipes with details from Firebase for page: $page")

            val firebaseRecipes = getPublicRecipesWithDetailsFromFirebase(pageSize)

            // Pour la première page, on met à jour le cache local
            if (page == 0 && firebaseRecipes.isNotEmpty()) {
                recipeDao.deleteAllPublicRecipes()
                Log.d(TAG, "All cached public recipes deleted")
                firebaseRecipes.forEach { recipeWithDetails ->
                    recipeDao.upsertRecipe(recipeWithDetails.recipe)
                    recipeStepDao.upsertSteps(recipeWithDetails.steps)
                    recipeIngredientDao.upsertIngredients(recipeWithDetails.ingredients)
                }
                Log.d(TAG, "Cached ${firebaseRecipes.size} complete public recipes for page: $page")
            }

            firebaseRecipes

        } catch (e: Exception) {
            Log.e(TAG, "Error getting public recipes with details: ${e.message}")
            if (page == 0) {
                val localRecipes = recipeDao.getPublicRecipesWithDetails()
                localRecipes.take(pageSize)
            } else {
                emptyList()
            }
        }
    }

    private suspend fun getRecipeRatingFromFirebase(recipeId: String): Pair<Float, Int> {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            val reviewsSnapshot = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            Log.d(TAG, "Found ${reviewsSnapshot.documents.size} reviews for recipe $recipeId")

            if (reviewsSnapshot.isEmpty) {
                Pair(0f, 0)
            } else {
                val ratings = reviewsSnapshot.documents.mapNotNull { doc ->
                    val rating = doc.getDouble("rating")?.toFloat()
                    Log.d(TAG, "Review rating: $rating")
                    rating
                }

                val average = if (ratings.isNotEmpty()) {
                    val avg = ratings.average().toFloat()
                    Log.d(TAG, "Average rating for $recipeId: $avg (${ratings.size} reviews)")
                    avg
                } else {
                    0f
                }

                Pair(average, ratings.size)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ratings for recipe $recipeId: ${e.message}")
            Pair(0f, 0)
        }
    }

    // === Avoir les steps ===
    suspend fun getRecipeSteps(recipeId: String): List<RecipeStep> {
        return try {
            val localSteps = recipeStepDao.getStepsForRecipe(recipeId)

            if (localSteps.isNotEmpty()) {
                Log.d(TAG, "Returning cached steps for recipe: $recipeId")
                return localSteps
            }

            Log.d(TAG, "Fetching recipe steps from Firebase: $recipeId")
            val recipeWithDetails = getRecipeFromFirebase(recipeId)

            recipeWithDetails?.let { recipe ->
                recipeStepDao.upsertSteps(recipe.steps)
                Log.d(TAG, "Steps cached locally for recipe: $recipeId")
                recipe.steps
            } ?: emptyList()

        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipe steps: ${e.message}")
            recipeStepDao.getStepsForRecipe(recipeId)
        }
    }
}