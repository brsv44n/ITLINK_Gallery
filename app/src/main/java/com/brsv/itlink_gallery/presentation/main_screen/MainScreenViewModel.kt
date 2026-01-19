package com.brsv.itlink_gallery.presentation.main_screen

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brsv.itlink_gallery.domain.models.toUiItem
import com.brsv.itlink_gallery.domain.repository.MainRepository
import com.brsv.itlink_gallery.presentation.main_screen.models.UiContentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val contentRepository: MainRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ContentUiState>(ContentUiState.Loading)
    val state: StateFlow<ContentUiState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents

    init {
        observeContent()
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _state.update { ContentUiState.Loading }

            contentRepository.getAllItems()
                .onFailure { error ->
                    _state.update {
                        ContentUiState.Error(
                            error = error.message
                        )
                    }
                }
        }
    }

    private fun observeContent() {
        contentRepository.observeItems()
            .map { items ->
                items.map { it.toUiItem(isPreview = true) }
            }
            .onEach { uiItems ->
                _state.update {
                    ContentUiState.Success(
                        items = uiItems
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ItemClicked -> {

            }
        }
    }

    sealed interface UiEvent {
        data class ItemClicked(val index: Int) : UiEvent
    }

}

@Stable
sealed class ContentUiState {
    data object Loading : ContentUiState()

    data class Error(val error: String?) : ContentUiState()

    data class Success(
        val items: List<UiContentItem> = emptyList(),
    ) : ContentUiState()
}
