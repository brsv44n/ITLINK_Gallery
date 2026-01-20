package com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FullscreenImageComponentImpl(
    componentContext: ComponentContext,
    private val imageUrl: String,
    private val output: (FullscreenImageComponent.Output) -> Unit,
) : FullscreenImageComponent, ComponentContext by componentContext {

    override val imageUrlState: StateFlow<String> = MutableStateFlow(imageUrl)

    override fun onEvent(event: FullscreenImageComponent.Event) {
        when (event) {
            is FullscreenImageComponent.Event.Close -> {
                output.invoke(FullscreenImageComponent.Output.Close)
            }

            is FullscreenImageComponent.Event.ShareClick -> {
                output.invoke(FullscreenImageComponent.Output.ShareClose(imageUrl))
            }
        }
    }
}

class FullscreenImageComponentFactory {
    fun invoke(
        componentContext: ComponentContext,
        output: (FullscreenImageComponent.Output) -> Unit,
        imageUrl: String
    ): FullscreenImageComponent = FullscreenImageComponentImpl(
        componentContext = componentContext,
        imageUrl = imageUrl,
        output = output,
    )
}
