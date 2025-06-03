package com.example.feedup.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedup.data.local.entities.RecipeReview
import com.example.feedup.data.repository.AuthRepository
import com.example.feedup.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeRateUiState(
    val rating: Int = 3,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val hasChanges: Boolean = false
)

class RecipeRateViewModel(application: Application) : AndroidViewModel(application) {

    private val reviewRepository = ReviewRepository(application)
    private val authRepository = AuthRepository()
    private val _uiState = MutableStateFlow(RecipeRateUiState())
    val uiState: StateFlow<RecipeRateUiState> = _uiState.asStateFlow()

    fun updateRating(newRating: Int) {
        _uiState.value = _uiState.value.copy(
            rating = newRating,
            hasChanges = true,
            error = null
        )
    }

    fun updateComment(newComment: String) {
        _uiState.value = _uiState.value.copy(
            comment = newComment,
            hasChanges = newComment.isNotBlank() || _uiState.value.rating != 3,
            error = null
        )
    }

    fun submitReview(recipeId: String) {
        val currentState = _uiState.value

        if (!currentState.hasChanges) {
            _uiState.value = currentState.copy(
                error = "Aucune modification à sauvegarder"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isSubmitting = true,
                error = null
            )

            try {

                val userId = authRepository.getCurrentUserId()

                if (userId == null){
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = "Erreur lors de l'envoi de votre avis, vous semblez être déconnectez"
                    )
                    return@launch
                }
                
                val review = RecipeReview(
                    recipeId = recipeId,
                    userId = userId,
                    rating = currentState.rating.toFloat(),
                    comment = currentState.comment.trim(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val success = reviewRepository.createOrUpdateReview(review)

                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        isSubmitted = true,
                        hasChanges = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = "Erreur lors de l'envoi de votre avis"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Erreur inattendue"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = RecipeRateUiState()
    }
}