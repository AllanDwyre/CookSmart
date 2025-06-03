package com.example.feedup.data.repository

import android.content.Context
import android.util.Log
import com.example.feedup.data.local.db.AppDatabase
import com.example.feedup.data.local.entities.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userProfileDao = database.userProfileDao()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "UserProfileRepository"
        private const val USERS_COLLECTION = "users"
        private const val CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 heures en millisecondes
    }

    // Récupérer le profil avec stratégie cache-first
    suspend fun getUserProfile(userId: String, forceRefresh: Boolean = false): UserProfile? {
        return try {
            val localProfile = userProfileDao.getUserProfile(userId)

            //  Vérifier si le cache est encore valide
            val isCacheValid = localProfile?.let { profile ->
                (System.currentTimeMillis() - profile.lastUpdated) < CACHE_EXPIRY_TIME
            } ?: false

            if (isCacheValid && !forceRefresh) {
                Log.d(TAG, "Returning cached profile for user: $userId")
                return localProfile
            }

            Log.d(TAG, "Fetching profile from Firebase for user: $userId")
            val firebaseProfile = getProfileFromFirebase(userId)

            // Mettre à jour le cache local
            firebaseProfile?.let { profile ->
                userProfileDao.insertOrUpdateUserProfile(profile.copy(lastUpdated = System.currentTimeMillis()))
                Log.d(TAG, "Profile cached locally for user: $userId")
            }

            firebaseProfile ?: localProfile

        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile: ${e.message}")
            userProfileDao.getUserProfile(userId)
        }
    }

    private suspend fun getProfileFromFirebase(userId: String): UserProfile? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                Log.d(TAG, "Raw Firestore data: ${document.data}")
                Log.d(TAG, "Mapped profile: ${document.toObject(UserProfile::class.java)}")

                document.toObject(UserProfile::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Firebase: ${e.message}")
            null
        }
    }

    suspend fun saveUserProfile(userProfile: UserProfile): Boolean {
        return try {
            val updatedProfile = userProfile.copy(lastUpdated = System.currentTimeMillis())

            // 1. Sauvegarder localement d'abord (plus rapide)
            userProfileDao.insertOrUpdateUserProfile(updatedProfile)
            Log.d(TAG, "Profile saved locally for user: ${userProfile.userId}")

            // 2. Synchroniser avec Firebase en arrière-plan
            syncToFirebase(updatedProfile)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile: ${e.message}")
            false
        }
    }

    private suspend fun syncToFirebase(userProfile: UserProfile) {
        try {
            firestore.collection(USERS_COLLECTION)
                .document(userProfile.userId)
                .set(userProfile.toFirebaseMap())
                .await()
            Log.d(TAG, "Profile synced to Firebase for user: ${userProfile.userId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Firebase: ${e.message}")
            // En cas d'échec, on pourrait implémenter une queue de synchronisation
        }
    }

    suspend fun createInitialProfile(userId: String, email: String, displayName: String): UserProfile {
        val initialProfile = UserProfile(
            userId = userId,
            email = email,
            displayName = displayName,
            isProfileComplete = false,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        saveUserProfile(initialProfile)
        return initialProfile
    }


    suspend fun clearCache(userId: String) {
        try {
            userProfileDao.deleteUserProfileById(userId)
            Log.d(TAG, "Cache cleared for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache: ${e.message}")
        }
    }
    suspend fun forceSyncFromCloud(userId: String): UserProfile? {
        return getUserProfile(userId, forceRefresh = true)
    }

    /**
     * Récupère l'ID de l'utilisateur actuellement connecté
     */
    fun getCurrentUserId(): String? {
        return try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID: ${e.message}")
            null
        }
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    fun isUserLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }
}