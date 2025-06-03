package com.example.feedup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.feedup.R
import com.example.feedup.ui.themes.ButtonShape
import com.example.feedup.ui.themes.onBackground
import com.example.feedup.ui.themes.secondary

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search your recipes",
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        leadingIcon = {
            Icon(painter = painterResource(R.drawable.search), contentDescription = "Search Icon")
        },
        shape = ButtonShape,
        colors = TextFieldDefaults.colors().copy(
            focusedContainerColor = onBackground,
            unfocusedContainerColor = onBackground,

            focusedPlaceholderColor = secondary,
            unfocusedPlaceholderColor = secondary,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        )
    )
}
