package com.brsv.itlink_gallery.presentation.main_screen.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brsv.itlink_gallery.R
import com.brsv.itlink_gallery.presentation.main_screen.ContentUiState
import com.brsv.itlink_gallery.presentation.main_screen.MainScreenComponent
import com.brsv.itlink_gallery.presentation.main_screen.PreviewMainScreenComponent
import com.brsv.itlink_gallery.presentation.main_screen.fullscreen_image.FullscreenImageContent
import com.brsv.itlink_gallery.presentation.main_screen.models.UiContentItem
import com.brsv.itlink_gallery.presentation.main_screen.models.UiImage
import com.brsv.itlink_gallery.presentation.ui.theme.ITLINK_GalleryTheme
import com.brsv.itlink_gallery.presentation.ui_kit.ErrorScreen
import com.brsv.itlink_gallery.presentation.ui_kit.ProgressLoader
import com.brsv.itlink_gallery.presentation.ui_kit.RetryableAsyncImage

@Composable
fun MainScreenContent(
    modifier: Modifier,
    component: MainScreenComponent
) {
    val uiState by component.uiState.collectAsState()

    val context = LocalContext.current

    //Вынес создание интента сюда просто потому что в данном контексте проще
    //но лучше создать отдельный UseCase для отправки интентов
    LaunchedEffect(component.uiEvents) {
        component.uiEvents.collect { event ->
            when (event) {
                is MainScreenComponent.UiEvent.ShareImage -> {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.imageUrl)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share"))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val state = uiState) {
                is ContentUiState.Loading -> {
                    ProgressLoader(modifier = Modifier.align(Alignment.Center))
                }

                is ContentUiState.Error -> {
                    ErrorScreen(
                        modifier = Modifier.fillMaxSize(),
                        errorText = state.error ?: stringResource(R.string.error_label_unknown),
                        onRetryClicked = { component.onEvent(MainScreenComponent.Event.RetryClick) }
                    )
                }

                is ContentUiState.Success -> {
                    ImagesGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        successUiState = state,
                        onItemClicked = { component.onEvent(MainScreenComponent.Event.ItemClick(it)) }
                    )
                }
            }
        }
        val fullscreenImageSlot by component.fullscreenImageComponent.subscribeAsState()
        fullscreenImageSlot.child?.let {
            FullscreenImageContent(
                modifier = Modifier,
                component = it.instance
            )
        }
    }
}

@Composable
private fun ImagesGrid(
    modifier: Modifier,
    successUiState: ContentUiState.Success,
    onItemClicked: (String) -> Unit
) {

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        successUiState.items.forEachIndexed { index, contentItem ->
            when (contentItem) {
                is UiContentItem.Text -> {
                    item(key = "content_item_$index") {
                        Box(
                            modifier = Modifier.size(100.dp)
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = contentItem.text
                            )
                        }
                    }
                }

                is UiContentItem.Image -> {
                    item(key = "content_item_$index") {
                        RetryableAsyncImage(
                            modifier = Modifier.size(100.dp),
                            imageUrl = contentItem.image.url,
                            onImageClicked = {
                                onItemClicked(contentItem.image.url)
                            },
                            isPreview = contentItem.image is UiImage.Preview,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewMainScreenContent() {
    ITLINK_GalleryTheme {
        MainScreenContent(
            modifier = Modifier.fillMaxSize(),
            component = PreviewMainScreenComponent()
        )
    }
}
