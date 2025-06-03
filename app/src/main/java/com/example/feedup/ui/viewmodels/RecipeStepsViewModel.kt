package com.example.feedup.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedup.data.local.entities.RecipeStep
import com.example.feedup.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeStepsUiState(
    val steps: List<RecipeStep> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipeStepsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)
    private val _uiState = MutableStateFlow(RecipeStepsUiState())
    val uiState: StateFlow<RecipeStepsUiState> = _uiState.asStateFlow()

    fun loadSteps(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val steps = repository.getRecipeSteps(recipeId)
                _uiState.value = _uiState.value.copy(
                    steps = steps,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Erreur lors du chargement des étapes",
                    isLoading = false
                )
            }
        }
    }
}