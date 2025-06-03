package com.example.feedup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.feedup.R
import com.example.feedup.data.local.entities.RecipeReview
import com.example.feedup.data.local.entities.RecipeWithDetails
import com.example.feedup.ui.components.DualButtonRow
import com.example.feedup.ui.components.EquipmentItem
import com.example.feedup.ui.components.IngredientItem
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.SmallButtonShape
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.errorColor
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.navigationColor
import com.example.feedup.ui.themes.onPrimary
import com.example.feedup.ui.themes.secondary
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.statusColor
import com.example.feedup.ui.themes.white
import com.example.feedup.ui.themes.xLargePadding
import com.example.feedup.ui.themes.xSmallPadding
import com.example.feedup.ui.viewmodels.RecipeDetailsUiState
import com.example.feedup.ui.viewmodels.RecipeDetailsViewModel

@Composable
fun RecipeDetailScreen(navController: NavHostController, recipeId: String) {
    val recipeDetailsViewModel: RecipeDetailsViewModel = viewModel()
    val uiState by recipeDetailsViewModel.uiState.collectAsState()

    LaunchedEffect(recipeId) {
        if (uiState.recipe?.recipe?.recipeId != recipeId) {
            recipeDetailsViewModel.loadRecipe(recipeId)
        }
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Erreur: ${uiState.error}",
                        color = errorColor,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        onClick = { recipeDetailsViewModel.loadRecipe(recipeId) }
                    ) {
                        Text("Réessayer")
                    }
                }
            }
        }

        uiState.recipe != null -> {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0,0,0,0),
                bottomBar = {
                    DetailedScreenBottomBar(
                        navController,
                        recipeId,
                        uiState.recipe!!.recipe.title,
                        uiState.recipe!!.recipe.imageUrl,
                        uiState.isFavorite,
                        recipeDetailsViewModel
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ){
                    item {
                        DetailedRecipeHeader(navController, uiState.recipe!!)
                    }

                    item {
                        val scrollState = rememberScrollState()

                        Column(
                            Modifier.padding(horizontal = largePadding),
                            verticalArrangement = Arrangement.spacedBy(smallPadding),
                        ) {
                            Spacer(Modifier.size(largePadding))
                            Row (Modifier.horizontalScroll(scrollState)){
                                if (uiState.recipe!!.recipe.category.isNotEmpty()) {
                                    TagChip(uiState.recipe!!.recipe.category)
                                }
                                if (uiState.recipe!!.recipe.origin.isNotEmpty()) {
                                    TagChip(uiState.recipe!!.recipe.origin)
                                }
                                uiState.recipe!!.recipe.dietTags.forEach { tag ->
                                    TagChip(tag)
                                }
                            }
                            Spacer(Modifier.size(xSmallPadding))

                            if (uiState.recipe!!.recipe.description.isNotEmpty()) {
                                Text("Description", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    uiState.recipe!!.recipe.description,
                                    color = secondary
                                )
                                Spacer(Modifier.size(xSmallPadding))
                            }

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Ingrédients", style = MaterialTheme.typography.titleSmall)
                                Text("${uiState.recipe!!.ingredients.size} items", style = MaterialTheme.typography.labelSmall)
                            }

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = smallPadding),
                            ) {
                                uiState.recipe!!.ingredients.forEach { ingredient ->
                                    IngredientItem(ingredient)
                                }
                            }
                            Spacer(Modifier.size(largePadding))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Equipments", style = MaterialTheme.typography.titleSmall)
                                Text("${uiState.recipe!!.recipe.equipments.size} items", style = MaterialTheme.typography.labelSmall)
                            }

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = smallPadding),
                            ) {
                                uiState.recipe!!.recipe.equipments.forEach { equipment ->
                                    EquipmentItem(equipment)
                                }
                            }
                            Spacer(Modifier.size(largePadding))
                        }
                    }

                    item {
                        ReviewsSection(uiState)
                    }

                    if (uiState.reviews.isNotEmpty()) {
                        items(uiState.reviews.size) { index ->
                            val review = uiState.reviews[index]
                            // Passer le viewModel au ReviewItem
                            ReviewItem(review = review, viewModel = recipeDetailsViewModel)

                            // Charger plus de reviews quand on approche de la fin
                            if (index >= uiState.reviews.size - 2 && uiState.hasMoreReviews && !uiState.isLoadingMoreReviews) {
                                LaunchedEffect(Unit) {
                                    recipeDetailsViewModel.loadMoreReviews()
                                }
                            }
                        }

                        if (uiState.isLoadingMoreReviews) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun TagChip(tag : String) {
    Box(
        modifier = Modifier
            .padding(horizontal = xSmallPadding)
            .clip(ButtonShape)
            .background(color = onPrimary)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = tag,
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetailedRecipeHeader(navController: NavHostController, recipe: RecipeWithDetails) {
    Box(
        Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = recipe.recipe.imageUrl,
            contentDescription = "Image recipe",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(.4f)
                .aspectRatio(180f / 285f)
                .clip(
                    ButtonShape.copy(
                        topStart = CornerSize(0),
                        topEnd = CornerSize(0),
                        bottomEnd = CornerSize(0),
                        bottomStart = CornerSize(10.dp)
                    )
                )
                .align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(.6f)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(xSmallPadding)) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(30.dp),
                    shape = SmallButtonShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Back"
                    )
                }
                OutlinedButton(
                    onClick = { /* TODO: Share */ },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(30.dp),
                    shape = SmallButtonShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = "Back"
                    )
                }
            }
            Spacer(Modifier.height(xLargePadding))
            Text(
                recipe.recipe.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(xLargePadding))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(smallPadding),
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("${recipe.recipe.cookingTime} minutes", style = MaterialTheme.typography.labelLarge)
                Icon(
                    painterResource(R.drawable.timer),
                    contentDescription = "Time",
                    tint = secondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(smallPadding))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(smallPadding),
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(recipe.recipe.difficulty, style = MaterialTheme.typography.labelLarge)
                Icon(
                    painterResource(R.drawable.difficulty_icon),
                    contentDescription = "Difficulty",
                    tint = secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
@Composable
private fun ReviewsSection(uiState: RecipeDetailsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = largePadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Avis (${uiState.reviewStats?.totalReviews ?: 0})",
                style = MaterialTheme.typography.titleSmall
            )

            // Affichage de la note moyenne seulement s'il y a des avis
            uiState.reviewStats?.let { stats ->
                if (stats.totalReviews > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.fill_star), // Assure-toi d'avoir cette icône
                            contentDescription = "Rating",
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            String.format("%.1f", stats.averageRating),
                            style = MaterialTheme.typography.labelMedium,
                            color = accent
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(smallPadding))

        // Message quand il n'y a pas d'avis
        if (uiState.reviews.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = largePadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.star), // ou une autre icône appropriée
                        contentDescription = "Pas d'avis",
                        tint = secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "Aucun avis pour le moment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondary
                    )
                    Text(
                        "Soyez le premier à donner votre avis !",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondary
                    )
                }
            }
        }
    }
}
@Composable
private fun ReviewItem(
    review: RecipeReview,
    viewModel: RecipeDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName = uiState.userNames[review.userId] ?: "Chargement..."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = largePadding, vertical = smallPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nom d'utilisateur avec indicateur de chargement
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    userName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (userName == "Chargement...") secondary else MaterialTheme.colorScheme.onSurface
                )

                // Petit indicateur de chargement si le nom est en cours de récupération
                if (userName == "Chargement..." && uiState.isLoadingUserNames) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = secondary
                    )
                }
            }

            // Note avec étoiles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        painter = painterResource(
                            if (index < review.rating.toInt()) R.drawable.fill_star
                            else R.drawable.star
                        ),
                        contentDescription = "Star ${index + 1}",
                        tint = if (index < review.rating.toInt()) accent else secondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (review.comment.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = secondary
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(
            formatDate(review.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = secondary
        )
    }
}
// Fonction utilitaire pour formater la date
private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
@Composable
private fun DetailedScreenBottomBar(
    navController: NavHostController,
    recipeId: String,
    recipeTitle: String,
    recipeImage: String,
    isFavorite: Boolean,
    recipeDetailsViewModel: RecipeDetailsViewModel
) {
    val verticalGradient = Brush.verticalGradient(
        listOf(
            statusColor,
            navigationColor
        )
    )

    val favoriteIcon = if(isFavorite) R.drawable.remove_favorite else R.drawable.add_favorite

    NavigationBar(Modifier.background(verticalGradient), containerColor = Color.Transparent) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = largePadding, vertical = mediumPadding)

        ) {
            DualButtonRow(
                onLeftClick = {
                    recipeDetailsViewModel.changeFavoriteState(!isFavorite)
                },
                leftButtonPadding = PaddingValues(0.dp),
                onRightClick = { navController.navigate("recipe_steps/${recipeId}?recipeTitle=${recipeTitle}&recipeImage=${recipeImage}")},
                leftButtonModifier = Modifier.size(50.dp),
                rightContent = {
                    Row {
                        Text(
                            "Start Cooking",
                            style = MaterialTheme.typography.labelLarge,
                            color = white
                        )
                        Icon(Icons.Filled.PlayArrow, "Start icon", tint = white)
                    }
                },
                leftContent = {
                    Icon(
                        painter = painterResource(favoriteIcon),
                        contentDescription = "Add to favorites",
                        tint = if(isFavorite) errorColor else secondary
                    )
                }
            )
        }
    }
}