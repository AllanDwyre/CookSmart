
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedup.data.local.entities.UserProfile
import com.example.feedup.data.repository.AuthRepository
import com.example.feedup.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val userProfileRepository = UserRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                if (user != null) {
                    // Utilisateur connecté - charger son profil
                    loadUserProfile(user)
                } else {
                    // Utilisateur déconnecté - nettoyer l'état
                    _uiState.update {
                        AuthUiState(
                            user = null,
                            userProfile = null,
                            isLoading = false,
                            needsProfileSetup = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadUserProfile(user: FirebaseUser) {
        _uiState.update { it.copy(isLoading = true) }

        try {
            // Essayer de récupérer le profil existant
            var userProfile = userProfileRepository.getUserProfile(user.uid)

            // Si aucun profil n'existe, créer un profil initial
            if (userProfile == null) {
                userProfile = userProfileRepository.createInitialProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: ""
                )
            }

            _uiState.update {
                it.copy(
                    user = user,
                    userProfile = userProfile,
                    isLoading = false,
                    needsProfileSetup = !userProfile.isProfileComplete
                )
            }

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    user = user,
                    userProfile = null,
                    isLoading = false,
                    needsProfileSetup = true,
                    error = "Erreur lors du chargement du profil: ${e.message}"
                )
            }
        }
    }

    fun updateUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            val success = userProfileRepository.saveUserProfile(userProfile)
            if (success) {
                _uiState.update {
                    it.copy(
                        userProfile = userProfile,
                        needsProfileSetup = !userProfile.isProfileComplete,
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // Nettoyer le cache avant de se déconnecter
            _uiState.value.user?.let { user ->
                userProfileRepository.clearCache(user.uid)
            }
            authRepository.signOut()
        }
    }

    // Connection avec email et mot de passe
    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            authRepository.signInWithEmailAndPassword(email, password)
        }
    }

    // Inscription avec email et mot de passe
    fun signUpWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            authRepository.signUpWithEmailAndPassword(email, password)
        }
    }


    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.value.user?.let { user ->
                val refreshedProfile = userProfileRepository.forceSyncFromCloud(user.uid)
                refreshedProfile?.let { profile ->
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            needsProfileSetup = !profile.isProfileComplete
                        )
                    }
                }
            }
        }
    }
}

data class AuthUiState(
    val user: FirebaseUser? = null,
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = true,
    val needsProfileSetup: Boolean = false,
    val error: String? = null
)