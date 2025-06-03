package com.example.feedup.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.feedup.ui.themes.secondary

@Composable
fun ProgressBar(
    progress: Float, // between 0f and 1f
    modifier: Modifier = Modifier,
    backgroundColor: Color = secondary,
    progressColor: Color = Color.Black,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.spring()
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(percent = 50))
                .background(backgroundColor)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(3.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(percent = 50))
                .background(progressColor)
        )
    }
}
