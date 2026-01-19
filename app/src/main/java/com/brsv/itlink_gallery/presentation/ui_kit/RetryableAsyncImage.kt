package com.brsv.itlink_gallery.presentation.ui_kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision

@Composable
fun RetryableAsyncImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    onImageClicked: (() -> Unit)? = null,
    contentDescription: String? = null,
    isPreview: Boolean = false
) {
    var retryKey by remember { mutableIntStateOf(0) }
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val imageRequest = remember(imageUrl, retryKey, isPreview) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .apply {
                if (isPreview) precision(Precision.INEXACT)
            }
            .build()
    }

    Box(modifier = modifier) {
        AsyncImage(
            modifier = Modifier.align(Alignment.Center),
            model = imageRequest,
            contentDescription = contentDescription,
            onState = { state ->
                when (state) {
                    is AsyncImagePainter.State.Error -> {
                        showError = true
                    }

                    is AsyncImagePainter.State.Success -> {
                        showError = false
                    }

                    else -> {}
                }
            }
        )

        if (showError) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        retryKey++
                        showError = false
                    }
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry load",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (onImageClicked != null && !showError) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onImageClicked),
                contentAlignment = Alignment.Center
            ) { }
        }
    }
}
