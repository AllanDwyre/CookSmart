package com.example.feedup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.onBackground
import com.example.feedup.ui.themes.onPrimary
import com.example.feedup.ui.themes.primary
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.xSmallPadding

@Composable
fun FilterChipRow(
    selectedCategory : String,
    onSelectedChanged: (String) -> Unit,
    categories : List<String> = listOf("Lunch", "Breakfast", "Dinner", "Dessert")
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(smallPadding)
    ) {
        categories.forEach { category ->
            FilterChip(
                text = category,
                isSelected = selectedCategory == category,
                onClick = { onSelectedChanged(category) }
            )
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) onPrimary else onBackground
    val textColor = if (isSelected) accent else primary
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .padding(horizontal = xSmallPadding)
            .clip(ButtonShape)
            .background(color = backgroundColor)
            .clickable(
                indication = ripple(color = accent.copy(alpha = 0.5f), bounded = true),
                interactionSource = interactionSource,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
