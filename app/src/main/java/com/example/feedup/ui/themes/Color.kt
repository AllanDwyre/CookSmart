package com.example.feedup.ui.themes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val white = Color(0xFFFFFFFF)
val black = Color(0xFF000000)

val primary = Color(0xFF303030)
val secondary = Color(0xFF616161)
val accent = Color(0xFF439270)

val onPrimary = Color(0xFFB9DFCF)
val onBackground = Color(0xFFF2F2F2)

val disable = Color(0xFFB2CAC0)

val errorColor = Color(0xFFE96767)
val success = Color(0xFF4CAF50)
val info = Color(0xFF2196F3)
val warning = Color(0xFFFFC107)

val statusColor = Color(0xFFE6EDF1)
val navigationColor = Color(0xFFDBE3E7)

val BackgroundGradient = Brush.linearGradient(
        colors = listOf(
            statusColor,
            navigationColor
        ),
    )