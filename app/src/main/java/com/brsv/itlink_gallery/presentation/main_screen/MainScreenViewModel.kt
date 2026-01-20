package com.brsv.itlink_gallery.presentation.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brsv.itlink_gallery.domain.models.toUiItem
import com.brsv.itlink_gallery.domain.repository.MainRepository
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

    private val _uiState = MutableStateFlow<ContentUiState>(ContentUiState.Loading)
    val uiState: StateFlow<ContentUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<MainScreenComponent.UiEvent>()
    val uiEvents: SharedFlow<MainScreenComponent.UiEvent> = _uiEvents

    init {
        observeContent()
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { ContentUiState.Loading }

            contentRepository.getAllItems()
                .onFailure { error ->
                    _uiState.update {
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
                _uiState.update {
                    ContentUiState.Success(
                        items = uiItems
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun reload() {
        observeContent()
        loadInitial()
    }

    fun shareImage(imageUrl: String) {
        viewModelScope.launch {
            _uiEvents.emit(MainScreenComponent.UiEvent.ShareImage(imageUrl))
        }
    }

}
