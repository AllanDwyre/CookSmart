package com.example.feedup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.feedup.R
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.black
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.navigationColor
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.white
import com.example.feedup.ui.themes.xSmallPadding
import com.example.feedup.ui.viewmodels.RecipeListItem
import java.util.Locale


@Composable
fun RecipeCard(data : RecipeListItem, onClick : () -> Unit = {}, height: Dp = 230.dp) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = ButtonShape,
        colors = CardDefaults.cardColors().copy(
            containerColor = navigationColor
        ),
        onClick = onClick
    ) {
        Box {
            SubcomposeAsyncImage(
                model = data.recipe.imageUrl,
                contentDescription = "Recipe image",
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(navigationColor)
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to black.copy(alpha = .7f),
                                0.35f to black.copy(alpha = .2f),
                                0.65f to black.copy(alpha = .2f),
                                1f to black.copy(alpha = .7f),
                            )
                        )
                    )

            )

            Text(
                text = data.authorName,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(vertical = largePadding, horizontal = mediumPadding),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )

            Row(
                Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(vertical = largePadding, horizontal = mediumPadding)
                    .background(black.copy(alpha = .4f))
                    .padding(horizontal = smallPadding, vertical = 2.dp)
                    .clip(
                        RoundedCornerShape(22.dp)
                    ),
                horizontalArrangement = Arrangement.spacedBy(xSmallPadding),
                verticalAlignment = Alignment.CenterVertically

            ){
                Icon(painter = painterResource(R.drawable.star), "", tint = white)
                Text(
                    text = String.format(Locale.US, "%.1f", data.averageRating.toDouble()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }


            Column(
                Modifier
                    .align(alignment = Alignment.BottomStart)
                    .padding(vertical = largePadding, horizontal = mediumPadding),
                verticalArrangement = Arrangement.spacedBy(xSmallPadding),
            ){
                Text(
                    text = data.recipe.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Row(horizontalArrangement = Arrangement.spacedBy(smallPadding)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(xSmallPadding)) {
                        Icon(
                            painter = painterResource(R.drawable.timer),
                            "",
                            tint = white,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "${data.recipe.cookingTime} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(xSmallPadding)) {
                        Icon(
                            painter = painterResource(R.drawable.difficulty_icon),
                            "",
                            tint = white,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = data.recipe.difficulty,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }


        }
    }
}