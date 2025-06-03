package com.example.feedup.ui.themes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

import com.example.feedup.R

val DancingScript = FontFamily(
    Font(R.font.dancing_script_regular),
)

val Poppins = FontFamily(
    Font(R.font.poppins_thin, FontWeight.Thin ),
    Font(R.font.poppins_thinitalic, FontWeight.Thin, FontStyle.Italic),

    Font(R.font.poppins_extralight, FontWeight.ExtraLight),
    Font(R.font.poppins_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),

    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_lightitalic, FontWeight.Light, FontStyle.Italic),

    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),

    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_mediumitalic, FontWeight.Medium, FontStyle.Italic),

    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),

    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_bolditalic, FontWeight.Bold, FontStyle.Italic),
)

val Typography = Typography(

    // Tout ce qui est lié au branding
    displayMedium = TextStyle(
        fontFamily = DancingScript,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        color = primary,
        // lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Tout ce qui est lié préférence, rate your meal ...
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize = 34.sp,
        color = primary,
        // lineHeight = 40.sp,
        letterSpacing = 0.25.sp
    ),

    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize = 24.sp,
        color = primary,
        // lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Tout ce qui est lié au title en mode  Step 1, Description, ...
    titleSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        color = primary,
        // lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Tout ce qui est text
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = primary,
        // lineHeight = 40.sp,
        letterSpacing = 0.02.em
    ),

    // Tout ce qui est sous titre ( En dessous de Scan your fridge)
    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        color = secondary,
        // lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Tout ce qui est Button
    labelLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        color = secondary,
        fontSize = 16.sp,
        // lineHeight = 40.sp,
        letterSpacing = 1.25.sp
    ),

    // Tout ce qui est label comme des badge
    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = secondary,
        // lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Les label de navigation, Discover, Meal plan, ...
    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = secondary,
        // lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
)