package com.example.feedup.ui.themes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Petites formes (boutons, champs texte, etc.)
    small = RoundedCornerShape(5.dp),

    // Formes moyennes (cartes, dialogues, etc.)
    medium = RoundedCornerShape(10.dp),

    // Grandes formes (bottom sheets, etc.)
    large = RoundedCornerShape(16.dp),

    extraLarge = RoundedCornerShape(24.dp)
)

val CardShape = RoundedCornerShape(15.dp)
val ButtonShape = RoundedCornerShape(10.dp)
val SmallButtonShape = RoundedCornerShape(5.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
val FullyRoundedShape = RoundedCornerShape(50)