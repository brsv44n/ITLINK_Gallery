package com.brsv.itlink_gallery.presentation.main_screen.models

import androidx.compose.runtime.Immutable

@Immutable
sealed interface UiImage {

    val url: String

    @Immutable
    data class Preview(
        override val url: String
    ) : UiImage

    @Immutable
    data class Original(
        override val url: String
    ) : UiImage
}
