package com.brsv.itlink_gallery.presentation.main_screen

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image.FullscreenImageComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewMainScreenComponent : MainScreenComponent {
    override val uiState: StateFlow<ContentUiState>
        get() = MutableStateFlow(ContentUiState.Loading)
    override val uiEvents: SharedFlow<MainScreenComponent.UiEvent>
        get() = TODO("Not yet implemented")

    override fun onEvent(event: MainScreenComponent.Event) {
        TODO("Not yet implemented")
    }

    override val fullscreenImageComponent: Value<ChildSlot<*, FullscreenImageComponent>>
        get() = TODO("Not yet implemented")

}
