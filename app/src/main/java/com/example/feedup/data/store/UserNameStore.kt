package com.example.feedup.data.store

import android.content.Context
import android.util.Log
import com.example.feedup.data.repository.UserRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Store centralisé pour gérer le cache des noms d'utilisateurs
 * Évite les appels répétés au UserRepository pour les mêmes utilisateurs
 */
class UserNameStore private constructor(context: Context) {

    private val userRepository = UserRepository(context)

    // Cache thread-safe des noms d'utilisateurs
    private val userNameCache = ConcurrentHashMap<String, String>()

    // Cache des états de chargement pour éviter les appels multiples simultanés
    private val loadingStates = ConcurrentHashMap<String, Mutex>()

    companion object {
        private const val TAG = "UserNameStore"
        private const val DEFAULT_USER_NAME = "Utilisateur"

        @Volatile
        private var INSTANCE: UserNameStore? = null

        fun getInstance(context: Context): UserNameStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserNameStore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Récupère le nom d'affichage d'un utilisateur
     * Utilise le cache en premier, puis fait un appel réseau si nécessaire
     */
    suspend fun getUserDisplayName(userId: String): String {
        // Vérifier d'abord le cache
        userNameCache[userId]?.let { cachedName ->
            Log.d(TAG, "Returning cached name for user: $userId")
            return cachedName
        }

        // Obtenir ou créer un mutex pour cet utilisateur
        val mutex = loadingStates.getOrPut(userId) { Mutex() }

        // S'assurer qu'un seul appel par utilisateur est en cours
        return mutex.withLock {
            // Double vérification après avoir acquis le lock
            userNameCache[userId]?.let { return it }

            try {
                Log.d(TAG, "Fetching display name for user: $userId")
                val userProfile = userRepository.getUserProfile(userId)
                val displayName = userProfile?.displayName ?: DEFAULT_USER_NAME

                // Mettre en cache le résultat
                userNameCache[userId] = displayName
                Log.d(TAG, "Cached display name '$displayName' for user: $userId")

                displayName
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching display name for user $userId: ${e.message}")
                // En cas d'erreur, utiliser un nom par défaut et le mettre en cache
                val defaultName = DEFAULT_USER_NAME
                userNameCache[userId] = defaultName
                defaultName
            }
        }
    }

    /**
     * Récupère les noms d'affichage pour une liste d'utilisateurs
     * Optimise les appels en parallèle pour les utilisateurs non mis en cache
     */
    suspend fun getUserDisplayNames(userIds: List<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Séparer les utilisateurs déjà en cache et ceux à récupérer
        val cached = mutableMapOf<String, String>()
        val toFetch = mutableListOf<String>()

        userIds.forEach { userId ->
            val cachedName = userNameCache[userId]
            if (cachedName != null) {
                cached[userId] = cachedName
            } else {
                toFetch.add(userId)
            }
        }

        result.putAll(cached)

        // Récupérer les noms manquants
        toFetch.forEach { userId ->
            val displayName = getUserDisplayName(userId)
            result[userId] = displayName
        }

        return result
    }

    /**
     * Précharge les noms d'utilisateurs pour une liste donnée
     * Utile pour préremplir le cache avant d'afficher une liste de commentaires
     */
    suspend fun preloadUserNames(userIds: List<String>) {
        val uniqueUserIds = userIds.distinct()
        Log.d(TAG, "Preloading ${uniqueUserIds.size} user names")

        getUserDisplayNames(uniqueUserIds)
    }

    /**
     * Met à jour le cache avec un nom d'utilisateur spécifique
     * Utile quand on a déjà les informations utilisateur ailleurs
     */
    fun updateUserDisplayName(userId: String, displayName: String) {
        userNameCache[userId] = displayName
        Log.d(TAG, "Updated cached name for user $userId: $displayName")
    }

    /**
     * Supprime un utilisateur du cache
     * Utile quand on sait que les informations ont changé
     */
    fun invalidateUser(userId: String) {
        userNameCache.remove(userId)
        loadingStates.remove(userId)
        Log.d(TAG, "Invalidated cache for user: $userId")
    }

    /**
     * Vide complètement le cache
     * Utile lors de la déconnexion ou changement d'utilisateur
     */
    fun clearCache() {
        userNameCache.clear()
        loadingStates.clear()
        Log.d(TAG, "Cleared all user name cache")
    }

    /**
     * Retourne la taille actuelle du cache
     */
    fun getCacheSize(): Int = userNameCache.size

    /**
     * Vérifie si un utilisateur est en cache
     */
    fun isUserCached(userId: String): Boolean = userNameCache.containsKey(userId)

    /**
     * Retourne les statistiques du cache pour le debugging
     */
    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "cacheSize" to userNameCache.size,
            "loadingStatesSize" to loadingStates.size,
            "cachedUsers" to userNameCache.keys.toList()
        )
    }
}