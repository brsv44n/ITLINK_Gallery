package com.brsv.itlink_gallery.domain.models

import com.brsv.itlink_gallery.presentation.main_screen.models.UiContentItem
import com.brsv.itlink_gallery.presentation.main_screen.models.UiImage

sealed interface ContentItem {

    val raw: String

    data class Image(
        override val raw: String,
        val url: String
    ) : ContentItem

    data class Text(
        override val raw: String
    ) : ContentItem
}

fun ContentItem.toUiItem(
    isPreview: Boolean
): UiContentItem =
    when (this) {
        is ContentItem.Image ->
            UiContentItem.Image(
                image = if (isPreview) {
                    UiImage.Preview(url)
                } else {
                    UiImage.Original(url)
                }
            )

        is ContentItem.Text ->
            UiContentItem.Text(raw)
    }