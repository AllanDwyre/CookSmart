package com.example.feedup.ui.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


private val ColorScheme = lightColorScheme(
    primary = primary,
    secondary = secondary,
    tertiary = accent,

    onPrimary = onPrimary,
    onSecondary = primary,

    onBackground = onBackground,


    error = errorColor,
    onError = Color.White,
    errorContainer = errorColor.copy(alpha = 0.1f),
    onErrorContainer = errorColor,

    surface = primary,
    onSurface = primary


)

@Composable
fun FeedUpAppTheme(
    content: @Composable () -> Unit
) {


    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        shapes = Shapes,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradient)
        ) {
            content()
        }
    }
}
