package com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image

interface FullscreenImageComponent {
    sealed interface Event {
        data object Close : Event
        data object RetryClicked : Event
    }

    sealed interface Output {
        data object Close : Output
    }

    fun onEvent(event: Event)
}
