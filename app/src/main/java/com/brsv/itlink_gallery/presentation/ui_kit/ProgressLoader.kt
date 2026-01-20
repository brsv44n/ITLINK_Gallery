package com.brsv.itlink_gallery.presentation.ui_kit

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressLoader(
    modifier: Modifier
) {
    CircularProgressIndicator(
        modifier = modifier,
        strokeWidth = 4.dp,
        color = MaterialTheme.colorScheme.primary,
    )
}
