package com.example.feedup.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.feedup.data.local.dao.FavoriteDao
import com.example.feedup.data.local.dao.RecipeDao
import com.example.feedup.data.local.dao.RecipeIngredientDao
import com.example.feedup.data.local.dao.RecipeReviewDao
import com.example.feedup.data.local.dao.RecipeStepDao
import com.example.feedup.data.local.dao.UserProfileDao
import com.example.feedup.data.local.entities.Favorite
import com.example.feedup.data.local.entities.Recipe
import com.example.feedup.data.local.entities.RecipeIngredient
import com.example.feedup.data.local.entities.RecipeReview
import com.example.feedup.data.local.entities.RecipeStep
import com.example.feedup.data.local.entities.UserProfile

@Database(
    entities = [
        UserProfile::class,
        Recipe::class, RecipeStep::class, RecipeIngredient::class, RecipeReview::class,
        Favorite::class
               ],
    version = 15,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun recipeStepDao(): RecipeStepDao
    abstract fun recipeReviewDao(): RecipeReviewDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "feed_up_database"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
