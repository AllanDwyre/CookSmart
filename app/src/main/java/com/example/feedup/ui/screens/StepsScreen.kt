package com.example.feedup.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.feedup.R
import com.example.feedup.ui.components.DualButtonRow
import com.example.feedup.ui.components.ProgressBar
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.SmallButtonShape
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.errorColor
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.white
import com.example.feedup.ui.themes.xLargePadding
import com.example.feedup.ui.viewmodels.RecipeStepsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeStepsScreen(navController: NavHostController, recipeId: String, recipeTitle: String, recipeImage : String) {

    val viewModel: RecipeStepsViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recipeId) {
        viewModel.loadSteps(recipeId)
    }

    when {
        uiState.isLoading  -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${uiState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        uiState.steps.isNotEmpty() -> {
        val steps = uiState.steps

        val pagerState = rememberPagerState { steps.size }
        val scope = rememberCoroutineScope()


        Column(
            modifier = Modifier .fillMaxSize()
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
            ProgressBar(
                progress = (pagerState.currentPage.toFloat() + 1f + pagerState.currentPageOffsetFraction) / pagerState.pageCount.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp, vertical = xLargePadding)
            )
            // Steps content
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = largePadding, vertical = largePadding),

            ) { page ->
                val step = steps[page]
                Column(
                    modifier = Modifier .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    Text(
                        text = "Étape ${step.stepNumber}",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = step.instruction,
                        style = MaterialTheme.typography.labelLarge
                    )

                    if (step.hasTimer) {
                        StepTimer(step.timerDurationMinutes!!)
                    }
                }
            }


            DualButtonRow(
                modifier = Modifier.padding(largePadding),
                onLeftClick = {
                    if (pagerState.currentPage == 0 )
                    {
                        navController.navigateUp()
                    }
                    else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                },
                onRightClick = {
                    if (pagerState.currentPage == pagerState.pageCount - 1 )
                    {
                        navController.navigate("recipe_review/${recipeId}?recipeTitle=${recipeTitle}&recipeImage=${recipeImage}")
                    }
                    else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                leftText = "go back",
                rightText = if (pagerState.currentPage == pagerState.pageCount - 1 ) "Bon Appetit" else "Next Step"
            )
        }
    }
}
}
@Composable
fun StepTimer(durationInMinute : Int) {
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableIntStateOf(durationInMinute * 60) }

    LaunchedEffect(isTimerRunning, timeRemaining) {
        if (isTimerRunning && timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        } else if (timeRemaining == 0) {
            isTimerRunning = false
        }
    }

    // Format MM:SS
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val formattedTime = String.format("%d:%02d", minutes, seconds)

    Box(Modifier.fillMaxWidth()){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
                .combinedClickable(
                    indication = ripple(
                        bounded = true,
                        color = accent,
                        radius = 70.dp
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        if (timeRemaining > 0) {
                            isTimerRunning = !isTimerRunning
                        }
                    },
                    onLongClick = {
                        timeRemaining = durationInMinute * 60
                        isTimerRunning = false
                    },
                )
                .padding(largePadding)
        ) {
            Icon(painter = painterResource(R.drawable.timer), "timer", tint = accent)
            Text(formattedTime, style = MaterialTheme.typography.titleLarge, color = if(timeRemaining < 10) errorColor else accent)
            Text(
                when {
                    timeRemaining == 0 -> "Long press to restart"
                    isTimerRunning -> "Tap to pause"
                    else -> "Tap to start"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Light
            )
        }
    }
}