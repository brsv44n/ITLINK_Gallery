package com.brsv.itlink_gallery.presentation.main_screen.models

import androidx.compose.runtime.Immutable

@Immutable
sealed interface UiContentItem {

    @Immutable
    data class Image(
        val image: UiImage
    ) : UiContentItem

    @Immutable
    data class Text(
        val text: String
    ) : UiContentItem
}
