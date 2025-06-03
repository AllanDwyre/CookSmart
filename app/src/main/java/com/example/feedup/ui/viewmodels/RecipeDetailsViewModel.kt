package com.example.feedup.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedup.data.local.entities.Favorite
import com.example.feedup.data.local.entities.RecipeReview
import com.example.feedup.data.local.entities.RecipeWithDetails
import com.example.feedup.data.repository.FavoriteRepository
import com.example.feedup.data.repository.RecipeRepository
import com.example.feedup.data.repository.ReviewRepository
import com.example.feedup.data.repository.ReviewSortOption
import com.example.feedup.data.repository.ReviewStats
import com.example.feedup.data.repository.UserRepository
import com.example.feedup.data.store.UserNameStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailsUiState(
    val recipe: RecipeWithDetails? = null,
    val reviews: List<RecipeReview> = emptyList(),
    val isFavorite : Boolean = false,
    val reviewStats: ReviewStats? = null,
    val currentUserReview: RecipeReview? = null,
    val userNames: Map<String, String> = emptyMap(), // Nouveau: cache des noms d'utilisateurs
    val isLoading: Boolean = false,
    val isLoadingMoreReviews: Boolean = false,
    val isLoadingUserNames: Boolean = false, // Nouveau: état de chargement des noms
    val hasMoreReviews: Boolean = false,
    val error: String? = null,
    val reviewSortOption: ReviewSortOption = ReviewSortOption.NEWEST_FIRST
)

class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val recipeRepository = RecipeRepository(application)
    private val reviewRepository = ReviewRepository(application)
    private val favoriteRepository = FavoriteRepository(application)
    private val userRepository = UserRepository(application)
    private val userNameStore = UserNameStore.getInstance(application) // Nouveau: instance du store

    private val _uiState = MutableStateFlow(RecipeDetailsUiState())
    val uiState: StateFlow<RecipeDetailsUiState> = _uiState.asStateFlow()

    private var loadedRecipeId: String? = null
    private var reviewsPageLoaded = 0

    /**
     * Charge la recette et ses reviews
     */
    fun loadRecipe(recipeId: String, isRefresh: Boolean = false) {
        if (!isRefresh && loadedRecipeId == recipeId && _uiState.value.recipe != null) {
            return
        }

        if (_uiState.value.isLoading && !isRefresh) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Charger les détails de la recette
                val recipeWithDetails = recipeRepository.getRecipeWithDetails(recipeId)

                // Charger les statistiques des reviews
                val reviewStats = reviewRepository.getRecipeReviewStats(recipeId)

                // Charger la review de l'utilisateur actuel (si connecté)
                val currentUserId = userRepository.getCurrentUserId()
                val currentUserReview = currentUserId?.let { userId ->
                    reviewRepository.getReviewByUserAndRecipe(recipeId, userId)
                }

                val isFavorite = currentUserId?.let { userId ->
                    favoriteRepository.isFavorite(userId = currentUserId,recipeId =  recipeId, )
                } == true

                // Charger la première page de reviews
                val reviewsPage = reviewRepository.getReviewsForRecipe(
                    recipeId = recipeId,
                    loadMore = false,
                    sortBy = _uiState.value.reviewSortOption
                )

                _uiState.value = _uiState.value.copy(
                    recipe = recipeWithDetails,
                    reviews = reviewsPage.reviews,
                    reviewStats = reviewStats,
                    currentUserReview = currentUserReview,
                    hasMoreReviews = reviewsPage.hasMore,
                    isFavorite = isFavorite,
                    isLoading = false,
                    error = null
                )

                loadUserNamesForReviews(reviewsPage.reviews)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement : ${e.message}"
                )
            }
        }
    }

    /**
     * Charge plus de reviews (pagination)
     */
    fun loadMoreReviews() {
        val currentState = _uiState.value
        if (currentState.isLoadingMoreReviews || !currentState.hasMoreReviews) return

        val recipeId = currentState.recipe?.recipe?.recipeId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMoreReviews = true)

            try {
                val reviewsPage = reviewRepository.getReviewsForRecipe(
                    recipeId = recipeId,
                    loadMore = true,
                    sortBy = currentState.reviewSortOption
                )

                val newReviews = currentState.reviews + reviewsPage.reviews

                _uiState.value = _uiState.value.copy(
                    reviews = newReviews,
                    hasMoreReviews = reviewsPage.hasMore,
                    isLoadingMoreReviews = false
                )

                // Charger les noms d'utilisateurs pour les nouvelles reviews
                loadUserNamesForReviews(reviewsPage.reviews)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMoreReviews = false,
                    error = "Erreur lors du chargement des reviews : ${e.message}"
                )
            }
        }
    }

    /**
     * Charge les noms d'utilisateurs pour une liste de reviews
     */
    private fun loadUserNamesForReviews(reviews: List<RecipeReview>) {
        if (reviews.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUserNames = true)

            try {
                val userIds = reviews.map { it.userId }.distinct()
                val userNames = userNameStore.getUserDisplayNames(userIds)

                // Mettre à jour l'état avec les nouveaux noms d'utilisateurs
                _uiState.value = _uiState.value.copy(
                    userNames = _uiState.value.userNames + userNames,
                    isLoadingUserNames = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingUserNames = false,
                    error = "Erreur lors du chargement des noms d'utilisateurs : ${e.message}"
                )
            }
        }
    }



    /**
     * Récupère le nom d'affichage d'un utilisateur depuis le cache ou le store
     */
    fun getUserDisplayName(userId: String): String {
        return _uiState.value.userNames[userId] ?: "Chargement..."
    }

    /**
     * Force le rechargement d'un nom d'utilisateur spécifique
     */
    fun refreshUserName(userId: String) {
        viewModelScope.launch {
            try {
                userNameStore.invalidateUser(userId)
                val displayName = userNameStore.getUserDisplayName(userId)

                _uiState.value = _uiState.value.copy(
                    userNames = _uiState.value.userNames + (userId to displayName)
                )
            } catch (e: Exception) {
                // Log l'erreur mais ne pas affecter l'UI
            }
        }
    }

    fun changeFavoriteState(newState : Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFavorite = newState
            )

            val currentUserId = userRepository.getCurrentUserId()

            currentUserId?.let {
                val recipeId = _uiState.value.recipe!!.recipe.recipeId
                val favorite = Favorite(recipeId = recipeId,
                    userId = currentUserId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                if (newState){
                    favoriteRepository.createFavorite(favorite)
                }
                else{
                    favoriteRepository.deleteFavorite(favorite)
                }

            }
        }
    }

    /**
     * Nettoie l'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Rafraîchit complètement les données
     */
    fun refresh() {
        val recipeId = _uiState.value.recipe?.recipe?.recipeId ?: return
        reviewRepository.clearPaginationCache()
        userNameStore.clearCache()
        loadRecipe(recipeId, isRefresh = true)
    }

    /**
     * Méthodes utilitaires pour l'UI
     */
    fun canLoadMore(): Boolean = _uiState.value.hasMoreReviews && !_uiState.value.isLoadingMoreReviews

    fun isCurrentUserReviewExists(): Boolean = _uiState.value.currentUserReview != null

    fun getCurrentUserRating(): Float = _uiState.value.currentUserReview?.rating ?: 0f

    fun getCurrentUserComment(): String = _uiState.value.currentUserReview?.comment ?: ""

    fun getFavoriteState(): String = _uiState.value.currentUserReview?.comment ?: ""

    /**
     * Nettoyage des ressources
     */
    override fun onCleared() {
        super.onCleared()
        reviewRepository.clearPaginationCache()
        // Note: Ne pas nettoyer le userNameStore car il est partagé
    }
}