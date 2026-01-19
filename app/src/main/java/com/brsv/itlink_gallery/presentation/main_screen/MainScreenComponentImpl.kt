package com.brsv.itlink_gallery.presentation.main_screen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image.FullscreenImageComponent
import com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image.FullscreenImageComponentFactory
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject

class MainScreenComponentImpl @Inject constructor(
    private val fullscreenImageComponentFactory: FullscreenImageComponentFactory,
    private val viewModel: MainScreenViewModel,
    componentContext: ComponentContext
) : MainScreenComponent, ComponentContext by componentContext {

    override val uiState: StateFlow<ContentUiState> = viewModel.uiState

    override val uiEvents: SharedFlow<MainScreenComponent.UiEvent> = viewModel.uiEvents

    override fun onEvent(event: MainScreenComponent.Event) {
        when (event) {
            is MainScreenComponent.Event.ItemClick -> {
                fullscreenImageSlotNavigation.activate(
                    FullscreenImageNavConfig.FullscreenImage(
                        event.imageUrl
                    )
                )
            }

            is MainScreenComponent.Event.RetryClick -> {
                TODO("call retry method from viewmodel")
                //viewModel
            }
        }
    }

    override val fullscreenImageComponent: Value<ChildSlot<*, FullscreenImageComponent>>
        get() = _fullscreenImageSlot

    private val fullscreenImageSlotNavigation = SlotNavigation<FullscreenImageNavConfig>()

    private val _fullscreenImageSlot = childSlot(
        source = fullscreenImageSlotNavigation,
        handleBackButton = true,
        serializer = FullscreenImageNavConfig.serializer(),
        key = "fullscreen_image_slot"
    ) { config, context ->
        when (config) {
            is FullscreenImageNavConfig.FullscreenImage -> {
                fullscreenImageComponentFactory.invoke(
                    componentContext = context,
                    output = ::onFullscreenImageOutput,
                    imageUrl = config.imageUrl
                )
            }
        }
    }

    private fun onFullscreenImageOutput(output: FullscreenImageComponent.Output) {
        when (output) {
            FullscreenImageComponent.Output.Close -> {
                fullscreenImageSlotNavigation.dismiss()
            }
        }
    }

    @Serializable
    private sealed class FullscreenImageNavConfig {

        @Serializable
        data class FullscreenImage(val imageUrl: String) : FullscreenImageNavConfig()
    }
}

class MainScreenComponentFactory @Inject constructor(
    private val viewModel: MainScreenViewModel,
    private val fullscreenImageComponentFactory: FullscreenImageComponentFactory
) {
    fun invoke(
        componentContext: ComponentContext
    ): MainScreenComponent = MainScreenComponentImpl(
        componentContext = componentContext,
        viewModel = viewModel,
        fullscreenImageComponentFactory = fullscreenImageComponentFactory
    )
}
