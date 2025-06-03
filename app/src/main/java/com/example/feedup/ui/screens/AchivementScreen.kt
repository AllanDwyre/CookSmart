package com.example.feedup.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.feedup.R
import com.example.feedup.data.local.entities.UserProfile
import com.example.feedup.ui.components.AchievementBadge
import com.example.feedup.ui.themes.SmallButtonShape
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.xLargePadding
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

data class Achievement(
    val title: String,
    val description: String,
    val iconRes: Int,
    val isUnlocked: Boolean = true
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AchievementScreen(
    user: FirebaseUser,
    userProfile: UserProfile?,
    navController: NavHostController
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    // val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val achievements = remember {
        listOf(
            Achievement("Oven Flamber", "You used the oven for more than 15h", R.drawable.oven_icon, false),
            Achievement("Oven Novice", "You used the oven for more than 2h", R.drawable.oven_icon),
            Achievement("Master Chef", "Cooked 100 meals", R.drawable.flame_icon),
            Achievement("Consistent Cooker", "Cooked a meal every day for a month", R.drawable.fridge_icon),
            Achievement("1h Recipe done !", "Cooked a recipe that took you 1 hour", R.drawable.timer),
            Achievement("Oven User", "You used the oven for more than 10h", R.drawable.oven_icon),
            Achievement("Food Party", "Used a recipe that have 10 différent ingredient", R.drawable.fridge_icon),
            Achievement("Tool Specialist", "You used more that 15 different tools !", R.drawable.casserole_icon, false),
            Achievement("Marathon", "Cooked a recipe that took you 2 hours", R.drawable.timer, false),
        )
    }

    val listState = rememberLazyListState()

    val itemWidth = 80.dp + 16.dp // Taille du badge + spacing
    // val itemWidthPx = with(density) { itemWidth.toPx() }

    val initialPadding = (screenWidth - 80.dp) / 2

    // Fonction pour calculer l'index central
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2

            val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }

            centerItem?.index ?: 0
        }
    }

    LaunchedEffect(centerIndex) {
        selectedIndex = centerIndex
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (!isScrolling) {
                    val targetIndex = centerIndex
                    // val targetOffset = targetIndex * itemWidthPx

                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = targetIndex,
                            scrollOffset = 0
                        )
                    }
                }
            }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = largePadding, vertical = xLargePadding)
    ) {
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

        Spacer(modifier = Modifier.size(mediumPadding))

        Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal)
        Text("Try to have all the achievements", style = MaterialTheme.typography.labelMedium)

        Spacer(Modifier.weight(1.5f))

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = initialPadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(achievements.size) { index ->
                val achievement = achievements[index]
                val isSelected = index == selectedIndex

                AchievementBadge(
                    achievement = achievement,
                    modifier = Modifier
                        .padding(horizontal = if (isSelected) 8.dp else 0.dp ),
                    isSelected = isSelected,
                    onSelected = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(
                                index = index,
                                scrollOffset = 0
                            )
                        }
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = {
                val slideDirection = if (targetState > initialState) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(400),
                    initialOffsetX = { width -> width * slideDirection }
                ) + fadeIn(animationSpec = tween(400))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(400),
                        targetOffsetX = { width -> -width * slideDirection }
                    ) + fadeOut(animationSpec = tween(200))
                )
            },
            label = "achievement_content"
        ) { index ->
            val currentAchievement = achievements[index]
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentAchievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(smallPadding))
                Text(
                    text = currentAchievement.description,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = mediumPadding)
                )
            }
        }

        Spacer(Modifier.weight(6f))
    }
}