package com.example.feedup.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.feedup.data.local.entities.Equipment
import com.example.feedup.data.local.entities.RecipeIngredient
import com.example.feedup.ui.themes.SmallButtonShape
import com.example.feedup.ui.themes.primary
import com.example.feedup.ui.themes.secondary
import com.example.feedup.ui.themes.smallPadding
import com.example.feedup.ui.themes.success
import com.example.feedup.ui.themes.white

// TODO :  Create Ingredient List

@Composable
fun IngredientItem(
    ingredient: RecipeIngredient,
    isCheckBox: Boolean = false,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = smallPadding)

    ) {
        if (isCheckBox) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(end = smallPadding),
                colors = CheckboxDefaults.colors().copy(
                    checkedBorderColor = success,
                    uncheckedBorderColor = primary,
                    checkedCheckmarkColor =  white,
                    checkedBoxColor = success
                )
            )
        }

        AsyncImage(
            model = ingredient.imageUrl,
            contentDescription = "ingredients",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(26.dp)
                .clip(SmallButtonShape)
        )

        Spacer(modifier = Modifier.width(smallPadding))

        Text(
            ingredient.name,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if(checked) TextDecoration.LineThrough else TextDecoration.None,
            color = if(checked) secondary else primary,
        )

        Spacer(Modifier.weight(1f))

        Text(
            ingredient.quantity,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
fun EquipmentItem(
    equipment: Equipment,
    isCheckBox: Boolean = false,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = smallPadding)

    ) {
        if (isCheckBox) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(end = smallPadding),
                colors = CheckboxDefaults.colors().copy(
                    checkedBorderColor = success,
                    uncheckedBorderColor = primary,
                    checkedCheckmarkColor =  white,
                    checkedBoxColor = success
                )
            )
        }

        AsyncImage(
            model = equipment.imageUrl,
            contentDescription = "ingredients",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(26.dp)
                .clip(SmallButtonShape)
        )

        Spacer(modifier = Modifier.width(smallPadding))

        Text(
            equipment.name,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if(checked) TextDecoration.LineThrough else TextDecoration.None,
            color = if(checked) secondary else primary,
        )
    }
}