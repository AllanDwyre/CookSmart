package com.example.feedup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.mediumPadding
import com.example.feedup.ui.themes.secondary

enum class SelectionMode {
    SINGLE,    // Un seul choix
    MULTIPLE   // Plusieurs choix
}


@Composable
fun SelectableGrid(
    items: List<String>,
    modifier: Modifier = Modifier,
    selectedItems: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    selectionMode: SelectionMode = SelectionMode.MULTIPLE,
    columns: Int = 2,
    itemSpacing: Dp = mediumPadding,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        modifier = modifier
    ) {
        items(items.size) { index ->
            val item = items[index]
            val isSelected = selectedItems.contains(item)

            SelectableGridItem(
                item = item,
                isSelected = isSelected,
                onClick = {
                    val newSelection = when (selectionMode) {
                        SelectionMode.SINGLE -> {
                            if (isSelected) emptySet() else setOf(item)
                        }
                        SelectionMode.MULTIPLE -> {
                            if (isSelected) {
                                selectedItems - item
                            } else {
                                selectedItems + item
                            }
                        }
                    }
                    onSelectionChange(newSelection)
                }
            )
        }
    }
}

@Composable
fun SelectableGridItem(
    item: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) accent else secondary
    val textColor = if (isSelected) accent else secondary
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = accent
                ),
                onClick = onClick
            ),
        shape = ButtonShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item,
                color = textColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}
