package com.example.feedup.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.feedup.R
import com.example.feedup.ui.components.DualButtonRow
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.SmallButtonShape
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.onBackground
import com.example.feedup.ui.themes.secondary
import com.example.feedup.ui.themes.white
import com.example.feedup.ui.themes.xLargePadding
import com.example.feedup.ui.viewmodels.RecipeRateViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeRateScreen(navController: NavHostController, recipeId: String, recipeTitle: String, recipeImage : String) {

    val viewModel: RecipeRateViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = false }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // Image header with recipe title overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Recipe image (placeholder)
            AsyncImage(
                model = recipeImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        ButtonShape.copy(
                            topEnd = CornerSize(0.dp),
                            topStart = CornerSize(0.dp)
                        )
                    ),
                contentScale = ContentScale.Crop
            )

            // Dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        ButtonShape.copy(
                            topEnd = CornerSize(0.dp),
                            topStart = CornerSize(0.dp)
                        )
                    )
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    )
            )

            Column(Modifier
                .align(alignment = Alignment.TopStart)
                .padding(horizontal = mediumPadding, vertical = xLargePadding),
                verticalArrangement = Arrangement.spacedBy(largePadding)){
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(30.dp),
                    shape = SmallButtonShape,
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Back",
                        tint = white
                    )
                }

                // Recipe title overlay
                Text(
                    text = recipeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
        }
        // Steps content

        Text(
            text = buildAnnotatedString {
                append("Rate your ")
                withStyle(style = SpanStyle(color = accent)) {
                    append("meal")
                }
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(largePadding)
        )

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = largePadding)
            )
        }

        Spacer(Modifier.weight(2.3f))
        Box(Modifier.fillMaxWidth().wrapContentHeight()) {
            SwipeRatingBar(
                uiState.rating, onRatingChange = { viewModel.updateRating(it)},
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(Modifier.weight(.5f))
        TextField(
            value = uiState.comment,
            placeholder = {Text("What is your though ...")},
            onValueChange = {viewModel.updateComment(it)},
            modifier = Modifier
                .padding(largePadding)
                .fillMaxWidth()
                .weight(2.5f),
            shape = ButtonShape,
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = onBackground,
                unfocusedContainerColor = onBackground,

                focusedPlaceholderColor = secondary,
                unfocusedPlaceholderColor = secondary,

                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        DualButtonRow(
            modifier = Modifier.padding(vertical = xLargePadding, horizontal = largePadding),
            onLeftClick = {navController.navigate("home")},
            onRightClick = {
                viewModel.submitReview(recipeId)
            } ,
            leftText = "skip",
            rightText =  if (uiState.isSubmitting) "Sending..." else "Send your rating",
            enabledRight = uiState.hasChanges && !uiState.isSubmitting,
        )
    }
}


@Composable
fun SwipeRatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
) {
    var localRating by remember { mutableIntStateOf(rating) }
    var tempRating by remember { mutableIntStateOf(rating) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(rating) {
        if (!isDragging) {
            localRating = rating
            tempRating = rating
        }
    }

    Row(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                val starWidth = 45.dp.toPx() + 8.dp.toPx() // taille + espacement

                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> {
                                isDragging = true
                                val position = event.changes.first().position.x - 16.dp.toPx() // offset du padding
                                val newRating = ((position / starWidth).toInt() + 1)
                                    .coerceIn(0, maxRating)
                                tempRating = newRating
                            }
                            PointerEventType.Move -> {
                                if (isDragging) {
                                    val position = event.changes.first().position.x - 16.dp.toPx()
                                    val newRating = ((position / starWidth).toInt() + 1)
                                        .coerceIn(0, maxRating)
                                    tempRating = newRating
                                }
                            }
                            PointerEventType.Release -> {
                                if (isDragging) {
                                    localRating = tempRating
                                    onRatingChange(localRating)
                                    isDragging = false
                                }
                            }
                        }
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..maxRating) {
            val isSelected = i <= (if (isDragging) tempRating else localRating)

            Icon(
                painter = painterResource(
                    if (isSelected) R.drawable.fill_star else R.drawable.star
                ),
                contentDescription = "Étoile $i sur $maxRating",
                tint = accent ,
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp)
                    .scale(if (isDragging && i == tempRating) 1.15f else 1f)
                    .graphicsLayer {
                        alpha = if (isDragging) 0.9f else 1f
                    }
            )
        }
    }
}