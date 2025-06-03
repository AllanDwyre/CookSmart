package com.example.feedup.data.repository

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Flow pour observer les changements d'état d'authentification
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            Log.d(TAG, "Auth state changed: ${firebaseAuth.currentUser?.email}")
            trySend(firebaseAuth.currentUser)
        }

        auth.addAuthStateListener(authStateListener)

        // Envoyer l'état initial
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            Log.d(TAG, "User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed: ${e.message}")
        }
    }

    // Connexion avec email et mot de passe
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign in successful for: $email")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Inscription avec email et mot de passe
    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign in successful for: $email")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Méthodes utilitaires
    fun isUserLoggedIn(): Boolean = currentUser != null

    fun getCurrentUserId(): String? = currentUser?.uid

    fun getCurrentUserEmail(): String? = currentUser?.email

    fun getCurrentUserDisplayName(): String? = currentUser?.displayName
}