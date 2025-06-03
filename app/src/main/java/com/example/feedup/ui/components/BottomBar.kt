package com.example.feedup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.example.feedup.BottomNavDestination
import com.example.feedup.ui.themes.accent
import com.example.feedup.ui.themes.largePadding
import com.example.feedup.ui.themes.secondary

@Composable
fun AppBottomNavigation(navController: NavHostController, currentDestination: NavDestination?) {

    val items = listOf(
        BottomNavDestination.Home,
        BottomNavDestination.MealPlan,
        BottomNavDestination.Scan,
        BottomNavDestination.Favorites,
        BottomNavDestination.Profile
    )
    BottomAppBar(
        containerColor = Color(0xFFEEF3F5),
        contentPadding = PaddingValues(horizontal = largePadding)
    ) {
        items.forEachIndexed { index, destination  ->
            val isSelected = currentDestination?.route == destination.route

            BottomNavItem(
                icon = ImageVector.vectorResource(id = destination.icon),
                label = destination.label,
                isSelected = isSelected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            if (index != items.lastIndex) {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) accent else secondary
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.run {
            clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = accent,
                    radius = 35.dp
                ),
            )
                .padding(8.dp)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = contentColor)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}