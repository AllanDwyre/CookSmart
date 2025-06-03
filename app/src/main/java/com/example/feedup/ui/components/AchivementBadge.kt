package com.example.feedup.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.feedup.ui.screens.Achievement
import com.example.feedup.ui.themes.SmallButtonShape
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.onBackground
import com.example.feedup.ui.themes.onPrimary
import com.example.feedup.ui.themes.secondary

@Composable
fun AchievementBadge(achievement: Achievement, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(SmallButtonShape)
            .background(onPrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(achievement.iconRes),
            contentDescription = achievement.title,
            tint = if(achievement.isUnlocked) accent else secondary,
            modifier = Modifier.size(20.dp)
        )
    }
}


@Composable
fun AchievementBadge(
    achievement: Achievement,
    modifier: Modifier,
    isSelected: Boolean = false,
    onSelected: () -> Unit = {}
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(SmallButtonShape)
            .background(
                if (achievement.isUnlocked) onPrimary else onBackground
            )
            .clickable { onSelected() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onSelected()
                    },
                    onDrag = { _, dragAmount ->
                    },
                    onDragEnd = {
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(achievement.iconRes),
            contentDescription = achievement.title,
            tint = if (achievement.isUnlocked) {
              accent
            } else {
                secondary
            },
            modifier = Modifier.size(20.dp)
        )
    }
}