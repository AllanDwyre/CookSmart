package com.example.feedup.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedup.data.local.entities.Favorite
import com.example.feedup.data.repository.FavoriteRepository
import com.example.feedup.data.repository.RecipeRepository
import com.example.feedup.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoriteItem(
    val details: RecipeListItem,
    val favoriteAt: Long,
    val authorName: String
)

data class FavoritesUiState(
    val favorites: List<FavoriteItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val sortBy: SortOption = SortOption.RECENT_FAVORITE,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0,
    val totalCount: Int = 0
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteRepository = FavoriteRepository(application)
    private val recipeRepository = RecipeRepository(application)
    private val userRepository = UserRepository(application)

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var allFavorites: List<FavoriteItem> = emptyList()
    private var currentUserId: String? = null

    companion object {
        private const val PAGE_SIZE = 15
    }

    /**
     * Charge les favoris de l'utilisateur
     */
    fun loadFavorites(userId: String, isRefresh: Boolean = false) {
        if (_uiState.value.isLoading && !isRefresh) {
            return
        }

        if (currentUserId != userId) {
            currentUserId = userId
            allFavorites = emptyList()
            _uiState.value = FavoritesUiState()
        }

        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    currentPage = 0,
                    hasMorePages = true,
                    favorites = emptyList()
                )
                allFavorites = emptyList()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            try {
                val page = if (isRefresh) 0 else _uiState.value.currentPage

                val paginatedFavorites = favoriteRepository.getFavorites(
                    userId = userId,
                    page = page,
                    pageSize = PAGE_SIZE,
                    forceRefresh = isRefresh,
                    resetPagination = isRefresh
                )

                val favoriteItems = getFavoriteItemsWithDetails(paginatedFavorites.favorites)

                if (isRefresh) {
                    allFavorites = favoriteItems
                } else {
                    // Éviter les doublons
                    val existingIds = allFavorites.map { it.details.recipe.recipeId }.toSet()
                    val newFavorites = favoriteItems.filter { it.details.recipe.recipeId !in existingIds }
                    allFavorites = allFavorites + newFavorites
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = null,
                    hasMorePages = paginatedFavorites.hasMore,
                    currentPage = if (paginatedFavorites.hasMore) page + 1 else page,
                    totalCount = paginatedFavorites.totalCount
                )

                applyFiltersAndSort()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "Erreur lors du chargement des favoris: ${e.message}"
                )
            }
        }
    }

    /**
     * Charge plus de favoris (pagination)
     */
    fun loadMoreFavorites() {
        val currentState = _uiState.value
        val userId = currentUserId ?: return

        if (currentState.isLoadingMore || currentState.isLoading || !currentState.hasMorePages) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)

            try {
                val paginatedFavorites = favoriteRepository.getFavorites(
                    userId = userId,
                    page = currentState.currentPage,
                    pageSize = PAGE_SIZE
                )

                val favoriteItems = getFavoriteItemsWithDetails(paginatedFavorites.favorites)

                // Éviter les doublons
                val existingIds = allFavorites.map { it.details.recipe.recipeId }.toSet()
                val newFavorites = favoriteItems.filter {it.details.recipe.recipeId !in existingIds }

                allFavorites = allFavorites + newFavorites

                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    hasMorePages = paginatedFavorites.hasMore,
                    currentPage = if (paginatedFavorites.hasMore)
                        currentState.currentPage + 1 else currentState.currentPage,
                    totalCount = paginatedFavorites.totalCount
                )

                applyFiltersAndSort()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = "Erreur lors du chargement de plus de favoris: ${e.message}"
                )
            }
        }
    }

    /**
     * Ajoute une recette aux favoris
     */
    fun addToFavorites(userId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                val favorite = Favorite(
                    recipeId = recipeId,
                    userId = userId,
                    createdAt = System.currentTimeMillis()
                )

                val success = favoriteRepository.createFavorite(favorite)
                if (success) {
                    // Recharger les favoris pour mettre à jour la liste
                    loadFavorites(userId, isRefresh = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Erreur lors de l'ajout aux favoris"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de l'ajout aux favoris: ${e.message}"
                )
            }
        }
    }

    /**
     * Supprime une recette des favoris
     */
    fun removeFromFavorites(userId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                val favorite = Favorite(
                    recipeId = recipeId,
                    userId = userId
                )

                val success = favoriteRepository.deleteFavorite(favorite)
                if (success) {
                    // Supprimer de la liste locale
                    allFavorites = allFavorites.filter { it.details.recipe.recipeId != recipeId }
                    applyFiltersAndSort()

                    // Mettre à jour le count
                    _uiState.value = _uiState.value.copy(
                        totalCount = _uiState.value.totalCount - 1
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Erreur lors de la suppression des favoris"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la suppression des favoris: ${e.message}"
                )
            }
        }
    }

    /**
     * Vérifie si une recette est en favoris
     */
    fun isFavorite(recipeId: String): Boolean {
        return allFavorites.any { it.details.recipe.recipeId == recipeId }
    }

    /**
     * Met à jour la requête de recherche
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }

    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFiltersAndSort()
    }

    /**
     * Met à jour l'option de tri
     */
    fun updateSortOption(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(sortBy = sortOption)
        applyFiltersAndSort()
    }

    /**
     * Transforme les favoris en FavoriteItem avec les détails des recettes
     */
    private suspend fun getFavoriteItemsWithDetails(favorites: List<Favorite>): List<FavoriteItem> {
        return favorites.mapNotNull { favorite ->
            try {
                // Récupérer les détails de la recette
                val recipeWithRating = recipeRepository.getRecipeWithDetails(favorite.recipeId)

                if (recipeWithRating != null) {
                    val authorName = getUserName(recipeWithRating.recipe.userId)

                    FavoriteItem(
                        details = RecipeListItem(
                            recipe = recipeWithRating.recipe,
                            authorName = authorName,
                            recipeId = recipeWithRating.recipe.userId,
                            reviewCount = recipeWithRating.reviewCount,
                            averageRating = recipeWithRating.averageRating,
                        ),
                        authorName = authorName,
                        favoriteAt = favorite.createdAt
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Récupère le nom d'un utilisateur
     */
    private suspend fun getUserName(userId: String): String {
        return try {
            userRepository.getUserProfile(userId)?.displayName ?: "Utilisateur inconnu"
        } catch (e: Exception) {
            "Utilisateur inconnu"
        }
    }

    /**
     * Applique les filtres et le tri
     */
    private fun applyFiltersAndSort() {
        val currentState = _uiState.value
        var filtered = allFavorites

        // Filtre par recherche
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { favorite ->
                favorite.details.recipe.title.contains(currentState.searchQuery, ignoreCase = true) ||
                        favorite.details.recipe.description.contains(currentState.searchQuery, ignoreCase = true) ||
                        favorite.authorName.contains(currentState.searchQuery, ignoreCase = true) ||
                        favorite.details.recipe.category.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // Filtre par catégorie
        if (currentState.selectedCategory != "All") {
            filtered = filtered.filter { recipe ->
                recipe.details.recipe.category.equals(currentState.selectedCategory, ignoreCase = true)
            }
        }

        // Tri
        filtered = when (currentState.sortBy) {
            SortOption.RECENT_FAVORITE -> filtered.sortedByDescending { it.favoriteAt }
            SortOption.OLDEST_FAVORITE -> filtered.sortedBy { it.favoriteAt }
            SortOption.RECIPE_RECENT -> filtered.sortedByDescending { it.details.recipe.createdAt }
            SortOption.RATING -> filtered.sortedByDescending { it.details.averageRating }
            SortOption.COOKING_TIME -> filtered.sortedBy { it.details.recipe.cookingTime }
            SortOption.ALPHABETICAL -> filtered.sortedBy { it.details.recipe.title }
        }

        _uiState.value = currentState.copy(favorites = filtered)
    }

    /**
     * Efface l'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Refresh complet des favoris
     */
    fun refreshFavorites() {
        currentUserId?.let { userId ->
            loadFavorites(userId, isRefresh = true)
        }
    }

    /**
     * Récupère les catégories disponibles dans les favoris
     */
    fun getAvailableCategories(): List<String> {
        return allFavorites.map { it.details.recipe.category }.distinct().sorted()
    }
}