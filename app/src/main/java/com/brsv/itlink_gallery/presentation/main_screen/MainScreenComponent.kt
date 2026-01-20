package com.brsv.itlink_gallery.presentation.main_screen

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image.FullscreenImageComponent
import com.brsv.itlink_gallery.presentation.main_screen.models.UiContentItem
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MainScreenComponent {
    val uiState: StateFlow<ContentUiState>

    val uiEvents: SharedFlow<UiEvent>

    fun onEvent(event: Event)

    val fullscreenImageComponent: Value<ChildSlot<*, FullscreenImageComponent>>

    sealed interface Event {
        data class ItemClick(val imageUrl: String) : Event
        data object RetryClick : Event
    }

    sealed interface UiEvent {
        data class ShareImage(val imageUrl: String) : UiEvent
    }

    sealed interface Output {
        data object Exit : Output
    }
}

@Stable
sealed class ContentUiState {
    @Stable
    data object Loading : ContentUiState()

    @Stable
    data class Error(val error: String?) : ContentUiState()

    @Stable
    data class Success(
        val items: List<UiContentItem> = emptyList(),
    ) : ContentUiState()
}
