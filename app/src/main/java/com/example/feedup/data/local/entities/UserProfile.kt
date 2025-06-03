package com.example.feedup.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",

    // Préférences alimentaires
    val dietaryPreferences: String = "", // végétarien, vegan, sans gluten, etc.
    val allergies: List<String> = emptyList(),
    val goals: List<String> = emptyList(),

    // Métadonnées
    @get:PropertyName("isProfileComplete")
    @set:PropertyName("isProfileComplete")
    var isProfileComplete: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")

    fun toFirebaseMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "email" to email,
        "displayName" to displayName,
        "profileImageUrl" to profileImageUrl,
        "dietaryPreferences" to dietaryPreferences,
        "allergies" to allergies,
        "goals" to goals,
        "isProfileComplete" to isProfileComplete,
        "lastUpdated" to lastUpdated,
        "createdAt" to createdAt
    )
}