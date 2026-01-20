package com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image.FullscreenImageComponent.Event
import com.brsv.itlink_gallery.presentation.ui_kit.FullScreenImageViewer

@Composable
fun FullscreenImageContent(
    modifier: Modifier,
    component: FullscreenImageComponent
) {
    val imageUrlState by component.imageUrlState.collectAsState()

    FullScreenImageViewer(
        modifier = modifier,
        imageUrl = imageUrlState,
        onDismiss = { component.onEvent(Event.Close) },
        shareImage = { component.onEvent(Event.ShareClick) }
    )
}
