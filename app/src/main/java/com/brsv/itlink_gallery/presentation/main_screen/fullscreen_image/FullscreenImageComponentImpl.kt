package com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image

import com.arkivanov.decompose.ComponentContext

class FullscreenImageComponentImpl(
    componentContext: ComponentContext,
    private val imageUrl: String,
    private val output: (FullscreenImageComponent.Output) -> Unit,
) : FullscreenImageComponent, ComponentContext by componentContext {
    override fun onEvent(event: FullscreenImageComponent.Event) {
        TODO("Not yet implemented")
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
