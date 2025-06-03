package com.example.feedup.ui.screens

import AuthViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.feedup.R
import com.example.feedup.data.local.entities.UserProfile
import com.example.feedup.ui.components.AchievementBadge
import com.example.feedup.ui.components.PrimaryButton
import com.example.feedup.ui.components.RecipeCard
import com.example.feedup.ui.themes.SmallButtonShape
import com.example.feedup.ui.themes.errorColor
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.secondary
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.xLargePadding
import com.example.feedup.ui.themes.xSmallPadding
import com.example.feedup.ui.viewmodels.RecipeListViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    user: FirebaseUser,
    userProfile: UserProfile?,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {

    // TODO : Load recipe cookeed (history) by user
    // TODO : Load achivements

    val recipeListViewModel: RecipeListViewModel = viewModel()

    val listState = rememberLazyListState()

    val uiState by recipeListViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.recipes.isEmpty() && !uiState.isLoading) {
            recipeListViewModel.loadRecipes(user.uid)
        }
    }


    Column(Modifier.padding(horizontal = largePadding) )
    {
        Spacer(Modifier.size(xLargePadding))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = xSmallPadding)
        ){
            Text(userProfile!!.displayName, style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = { authViewModel.signOut() },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(30.dp),
                shape = SmallButtonShape,
                border = BorderStroke(1.dp, errorColor)
            ) {
                Icon(
                    painter = painterResource(R.drawable.signout),
                    contentDescription = "Back",
                    tint = errorColor
                )
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(Modifier.size(largePadding))

                // Achivements
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("My achievements", style = MaterialTheme.typography.titleSmall, color = secondary)
                    TextButton(onClick = {
                        navController.navigate("achievements")
                    }) {
                        Text("see more", textDecoration = TextDecoration.Underline, style = MaterialTheme.typography.bodyMedium, color = secondary)
                    }
                }
                Row (horizontalArrangement = Arrangement.spacedBy(smallPadding),){
                    AchievementBadge(Achievement(title = "test", iconRes = R.drawable.casserole_icon, description = ""), Modifier)
                    AchievementBadge(Achievement(title = "test", iconRes = R.drawable.oven_icon, description = ""), Modifier)
                    AchievementBadge(Achievement(title = "test", iconRes = R.drawable.fridge_icon, description = ""), Modifier)
                }

                // Preferences :
                Spacer(Modifier.size(xLargePadding))
                Text("Preferences", style = MaterialTheme.typography.titleSmall, color = secondary)
                PrimaryButton(
                    onClick = {
                        navController.navigate("preferences")
                    },
                    text = "Changer mes préférences",
                    modifier = Modifier.padding(vertical = largePadding)
                )
                Spacer(Modifier.size(mediumPadding))

            }

            item {
                Text("My Recipes", style = MaterialTheme.typography.titleSmall, color = secondary)
            }
            items(uiState.recipes) { item ->
                RecipeCard(
                    item,
                    height = 160.dp,
                    onClick = {
                        navController.navigate("recipe_detail/${item.recipeId}")
                    },
                )
                if (uiState.hasMorePages){
                    TextButton(onClick = {recipeListViewModel.loadMoreRecipes(user.uid)}) {
                        Text("see more", textDecoration = TextDecoration.Underline, style = MaterialTheme.typography.bodyMedium, color = secondary)
                    }
                }
            }
            if (uiState.isLoading && uiState.recipes.isEmpty() || uiState.isLoadingMore) {
                item {
                    CircularProgressIndicator(modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally))
                }
            }
            if (!uiState.hasMorePages && uiState.recipes.isNotEmpty()) {
                item {
                    Text(
                        text = "Toutes les recettes ont été chargées",
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!uiState.hasMorePages && uiState.recipes.isEmpty()) {
                item {
                    Text(
                        text = "Créer vos propre recettes !!",
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                PrimaryButton(text = "Create a Recipe", onClick = {})
            }

//            item {
//                Spacer(Modifier.size(xLargePadding))
//                Text("Meal History", style = MaterialTheme.typography.titleSmall, color = secondary)
//            }
//            items(uiState.recipes) { item ->
//                RecipeCard(
//                    item,
//                    height = 160.dp,
//                    onClick = {
//                        navController.navigate("recipe_detail/${item.recipeId}")
//                    },
//                )
//            }



        }
    }
}

