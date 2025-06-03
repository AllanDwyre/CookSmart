package com.example.feedup.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedup.data.local.entities.Recipe
import com.example.feedup.data.repository.RecipeRepository
import com.example.feedup.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeListItem(
    val recipeId: String,
    val authorName: String,
    val reviewCount: Int,
    val averageRating: Float,

    val recipe : Recipe,
)

data class RecipeListUiState(
    val recipes: List<RecipeListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.RECIPE_RECENT,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0
)

enum class SortOption {
    RECENT_FAVORITE,
    OLDEST_FAVORITE,
    RECIPE_RECENT,
    RATING,
    COOKING_TIME,
    ALPHABETICAL
}

class RecipeListViewModel(application: Application) : AndroidViewModel(application) {

    private val recipeRepository = RecipeRepository(application)
    private val userRepository = UserRepository(application)

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private var allRecipes: List<RecipeListItem> = emptyList()
    private var filteredRecipes: List<RecipeListItem> = emptyList()

    companion object {
        private const val PAGE_SIZE = 9
    }

    fun loadRecipes(userId: String? = null, isRefresh: Boolean = false) {
        if (_uiState.value.isLoading && !isRefresh) {
            return
        }

        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    currentPage = 0,
                    hasMorePages = true,
                    recipes = emptyList()
                )
                allRecipes = emptyList()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            try {
                val page = if (isRefresh) 0 else _uiState.value.currentPage

                val recipesWithRating = if (userId != null) {
                    recipeRepository.getUserRecipes(userId)
                } else {
                    recipeRepository.getPublicRecipesWithDetails(
                        page = page,
                        pageSize = PAGE_SIZE,
                        forceRefresh = isRefresh
                    )
                }

                val recipeItems = recipesWithRating.map { recipeWithRating ->
                    val authorName = getUserName(recipeWithRating.recipe.userId)

                    RecipeListItem(
                        recipeId = recipeWithRating.recipe.recipeId,
                        recipe = recipeWithRating.recipe,
                        authorName = authorName,
                        averageRating = recipeWithRating.averageRating,
                        reviewCount = recipeWithRating.reviewCount,
                    )
                }

                if (isRefresh) {
                    allRecipes = recipeItems
                } else {
                    allRecipes = allRecipes + recipeItems
                }

                val hasMorePages = recipeItems.size == PAGE_SIZE

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = null,
                    hasMorePages = hasMorePages,
                    currentPage = if (hasMorePages) page + 1 else page
                )

                applyFiltersAndSort()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "Erreur lors du chargement des recettes: ${e.message}"
                )
            }
        }
    }

    fun loadMoreRecipes(userId: String? = null) {
        val currentState = _uiState.value

        if (currentState.isLoadingMore || currentState.isLoading || !currentState.hasMorePages) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)

            try {
                val recipesWithRating = if (userId != null) {
                    emptyList()
                } else {
                    recipeRepository.getPublicRecipesWithDetails(
                        page = currentState.currentPage,
                        pageSize = PAGE_SIZE
                    )
                }

                val recipeItems = recipesWithRating.map { recipeWithRating ->
                    val authorName = getUserName(recipeWithRating.recipe.userId)

                    RecipeListItem(
                        recipeId = recipeWithRating.recipe.recipeId,
                        recipe = recipeWithRating.recipe,
                        authorName = authorName,
                        averageRating = recipeWithRating.averageRating,
                        reviewCount = recipeWithRating.reviewCount,
                    )
                }

                val existingIds = allRecipes.map { it.recipeId }.toSet()
                val newRecipes = recipeItems.filter { it.recipeId !in existingIds }

                allRecipes = allRecipes + newRecipes
                val hasMorePages = recipeItems.size == PAGE_SIZE

                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    hasMorePages = hasMorePages,
                    currentPage = if (hasMorePages) currentState.currentPage + 1 else currentState.currentPage
                )

                applyFiltersAndSort()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = "Erreur lors du chargement de plus de recettes: ${e.message}"
                )
            }
        }
    }

    private suspend fun getUserName(userId: String): String {
        return try {
            userRepository.getUserProfile(userId)?.displayName ?: "Utilisateur inconnu"
        } catch (e: Exception) {
            "Utilisateur inconnu"
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }

    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFiltersAndSort()
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(sortBy = sortOption)
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val currentState = _uiState.value
        var filtered = allRecipes

        // Filtre par recherche
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { recipe ->
                recipe.recipe.title.contains(currentState.searchQuery, ignoreCase = true) ||
                        recipe.recipe.description.contains(currentState.searchQuery, ignoreCase = true) ||
                        recipe.authorName.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // Filtre par catégorie
        if (currentState.selectedCategory != "All") {
            filtered = filtered.filter { recipe ->
                recipe.recipe.category.equals(currentState.selectedCategory, ignoreCase = true)
            }
        }

        // Tri
        filtered = when (currentState.sortBy) {
            SortOption.OLDEST_FAVORITE -> filtered.sortedBy { it.recipe.title }
            SortOption.RECENT_FAVORITE -> filtered.sortedBy { it.recipe.title }
            SortOption.RECIPE_RECENT -> filtered.sortedByDescending { it.recipe.createdAt }
            SortOption.RATING -> filtered.sortedByDescending { it.averageRating }
            SortOption.COOKING_TIME -> filtered.sortedBy { it.recipe.cookingTime }
            SortOption.ALPHABETICAL -> filtered.sortedBy { it.recipe.title }
        }


        filteredRecipes = filtered
        _uiState.value = currentState.copy(recipes = filtered)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getAvailableCategories(): List<String> {
        val categories = allRecipes.map { it.recipe.category }.distinct().sorted().toMutableList()
        categories.add(0, "All")
        return categories
    }
}