package com.example.feedup.ui.screens

import AuthViewModel
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.feedup.data.local.entities.UserProfile
import com.example.feedup.ui.components.FilterChipRow
import com.example.feedup.ui.components.RecipeCard
import com.example.feedup.ui.components.SearchBar
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.xLargePadding
import com.example.feedup.ui.viewmodels.FavoritesViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun FavoritesScreen(
    user: FirebaseUser,
    userProfile: UserProfile?,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {

    val recipeListViewModel: FavoritesViewModel = viewModel()
    val listState = rememberLazyListState()
    val uiState by recipeListViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (!uiState.isLoading) {
            recipeListViewModel.loadFavorites(user.uid, isRefresh = true)
        }
    }

    // Détection du scroll pour la pagination
    LaunchedEffect(listState, uiState.favorites.size) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex to totalItemsNumber
        }.collect { (lastVisibleIndex, totalItems) ->
            if (lastVisibleIndex >= totalItems - 2 &&
                totalItems > 0 &&
                uiState.hasMorePages &&
                !uiState.isLoadingMore &&
                !uiState.isLoading) {
                recipeListViewModel.loadMoreFavorites()
            }
        }
    }

    Column(Modifier.padding(horizontal = largePadding))
    {
        Spacer(Modifier.size(xLargePadding))
        Text("Favorites", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.size(mediumPadding))
        SearchBar(
            query = uiState.searchQuery,
            onQueryChanged = { recipeListViewModel.updateSearchQuery(it) }
        )
        Spacer(Modifier.size(mediumPadding))
        FilterChipRow(
            selectedCategory = uiState.selectedCategory,
            onSelectedChanged = { recipeListViewModel.updateSelectedCategory(it) },
            categories = listOf("All", "entrée", "plat", "dessert",)
        )
        Spacer(Modifier.size(mediumPadding))
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.favorites) { item ->
                RecipeCard(
                    item.details,
                    height = 140.dp,
                    onClick = {
                        navController.navigate("recipe_detail/${item.details.recipe.recipeId}")
                    },
                )
            }

            if (uiState.isLoading && uiState.favorites.isEmpty() || uiState.isLoadingMore) {
                item {
                    CircularProgressIndicator(modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally))
                }
            }

            if (!uiState.hasMorePages && uiState.favorites.isNotEmpty()) {
                item {
                    Text(
                        text = "Toutes les favories ont été chargées",
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
