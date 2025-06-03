package com.example.feedup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.disable
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.white

/**
 * Bouton principal stylisé pour les actions importantes
 */
@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
    text: String = "Text",
    onClick: () -> Unit,
    enabled: Boolean = true

) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = disable,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    ) {
        if (content == null){
            Text(text, style = MaterialTheme.typography.labelLarge, color = white)
        } else{
            content.invoke()
        }
    }
}

/**
 * Bouton stylisé pour les actions avec des choix
 */
@Composable
fun DualButtonRow(
    modifier: Modifier = Modifier,
    rightText: String = "Continuez",
    leftText: String = "Annuler",
    onRightClick: () -> Unit,
    onLeftClick: () -> Unit,
    leftContent: @Composable (() -> Unit)? = null,
    rightContent: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    enabledLeft: Boolean = true,
    enabledRight: Boolean = true,
    leftButtonModifier: Modifier = Modifier.height(50.dp),
    leftButtonPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(smallPadding)
    ) {
        // ----- Button Outline (gauche) -----
        OutlinedButton(
            onClick = onLeftClick,
            modifier = leftButtonModifier,
            enabled = enabled && enabledLeft,
            shape = ButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary,
                disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            contentPadding = leftButtonPadding,
        ) {
            if (leftContent == null)
                Text(leftText, style = MaterialTheme.typography.labelLarge)
            else
                leftContent.invoke()
        }

        // ----- Bouton Plein (droit) -----
        PrimaryButton(
            text = rightText,
            onClick = onRightClick,
            enabled = enabled && enabledRight,
            content = rightContent
        )
    }
}