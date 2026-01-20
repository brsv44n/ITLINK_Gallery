package com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image

import kotlinx.coroutines.flow.StateFlow

interface FullscreenImageComponent {

    val imageUrlState: StateFlow<String>

    sealed interface Event {
        data object Close : Event
        data object ShareClick : Event
    }

    sealed interface Output {
        data object Close : Output
        data class ShareClose(val imageUrl: String) : Output
    }

    fun onEvent(event: Event)
}
