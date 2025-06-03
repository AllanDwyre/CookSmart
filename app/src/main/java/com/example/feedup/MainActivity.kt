package com.example.feedup

import AuthViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.feedup.data.local.entities.UserProfile
import com.example.feedup.ui.components.AppBottomNavigation
import com.example.feedup.ui.screens.AchievementScreen
import com.example.feedup.ui.screens.BoardingScreen
import com.example.feedup.ui.screens.FavoritesScreen
import com.example.feedup.ui.screens.HomeScreen
import com.example.feedup.ui.screens.PreferenceScreen
import com.example.feedup.ui.screens.ProfileScreen
import com.example.feedup.ui.screens.RecipeDetailScreen
import com.example.feedup.ui.screens.RecipeRateScreen
import com.example.feedup.ui.screens.RecipeStepsScreen
import com.example.feedup.ui.screens.SplashScreen
import com.example.feedup.ui.themes.FeedUpAppTheme
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FeedUpAppTheme {
               AuthenticationFlow(authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun AuthenticationFlow(authViewModel: AuthViewModel) {
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.userProfile?.isProfileComplete) {
        println("🔄 Profile complete status: ${uiState.userProfile?.isProfileComplete}")
        println("🔄 Needs profile setup: ${uiState.needsProfileSetup}")
        println("🔄 User profile: ${uiState.userProfile}")
        Log.d("AuthenticationFlow", "Authentication flow changed: Unknown")
    }

    when {
        uiState.isLoading -> {
            SplashScreen()
        }
        uiState.user == null -> {
            BoardingScreen(authViewModel = authViewModel)
        }
        uiState.needsProfileSetup -> {
            Scaffold(containerColor = Color.Transparent) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    PreferenceScreen(
                        userProfile = uiState.userProfile,
                        authViewModel = authViewModel,
                        user = uiState.user!!,
                    )
                }
            }
        }
        else -> {
            // Navigation principale de l'app
            MainScreen(
                user = uiState.user!!,
                userProfile = uiState.userProfile,
                authViewModel = authViewModel,
            )
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            println("Erreur: $error")
        }
    }
}

sealed class BottomNavDestination(val route: String, val icon: Int, val label: String) {
    object Home : BottomNavDestination("home", R.drawable.discover, "Discover")
    object MealPlan : BottomNavDestination("mealPlan", R.drawable.meal_plan, "Meal plan")
    object Favorites : BottomNavDestination("favorites", R.drawable.favorites, "Favorites")
    object Scan : BottomNavDestination("scan", R.drawable.scan, "Scan")
    object Profile : BottomNavDestination("profile", R.drawable.settings, "Profile")
}

@Composable
fun MainScreen(
    user: FirebaseUser,
    userProfile: UserProfile?,
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Log.d("MainNavigation", "Page route changed: ${currentDestination?.route ?: "Unknown"}")

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (shouldShowBottomBar(currentDestination?.route)) {
                AppBottomNavigation(navController, currentDestination)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavDestination.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavDestination.Home.route) {
                HomeScreen(
                    user = user,
                    userProfile = userProfile,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            composable(BottomNavDestination.MealPlan.route) {
                Text("Meal Plan Screen - À implémenter")
            }

            composable(BottomNavDestination.Scan.route) {
                Text("Scan Screen - À implémenter")
            }

            composable(BottomNavDestination.Favorites.route) {
                FavoritesScreen(
                    user = user,
                    userProfile = userProfile,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            composable(BottomNavDestination.Profile.route) {
                ProfileScreen(
                    user = user,
                    userProfile = userProfile,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            composable("preferences") {
                PreferenceScreen(
                    user = user,
                    userProfile = userProfile,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            composable("achievements") { backStackEntry ->
                AchievementScreen(
                    user = user,
                    userProfile = userProfile,
                    navController = navController
                )
            }

            composable("recipe_detail/{recipeId}") { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                RecipeDetailScreen(
                    recipeId = recipeId,
                    navController = navController
                )
            }


            composable(
                route = "recipe_steps/{recipeId}?recipeTitle={recipeTitle}&recipeImage={recipeImage}",
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.StringType },
                    navArgument("recipeTitle") { type = NavType.StringType; defaultValue = "" },
                    navArgument("recipeImage") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
                val recipeImage = backStackEntry.arguments?.getString("recipeImage") ?: ""

                RecipeStepsScreen(
                    recipeId = recipeId,
                    recipeTitle = recipeTitle,
                    recipeImage = recipeImage,
                    navController = navController
                )
            }

            composable(
                route = "recipe_review/{recipeId}?recipeTitle={recipeTitle}&recipeImage={recipeImage}",
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.StringType },
                    navArgument("recipeTitle") { type = NavType.StringType; defaultValue = "" },
                    navArgument("recipeImage") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
                val recipeImage = backStackEntry.arguments?.getString("recipeImage") ?: ""

                RecipeRateScreen(
                    recipeId = recipeId,
                    recipeTitle = recipeTitle,
                    recipeImage = recipeImage,
                    navController = navController
                )
            }
        }
    }
}

private fun shouldShowBottomBar(route: String?): Boolean {
    return when {
        route == null -> true
        route.startsWith("home") -> true
        route.startsWith("mealPlan") -> true
        route.startsWith("favorites") -> true
        route.startsWith("scan") -> true
        route.startsWith("profile") -> true
        else -> false
    }
}