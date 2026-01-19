package com.brsv.itlink_gallery.presentation.main_screen.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brsv.itlink_gallery.R
import com.brsv.itlink_gallery.presentation.main_screen.ContentUiState
import com.brsv.itlink_gallery.presentation.main_screen.MainScreenComponent
import com.brsv.itlink_gallery.presentation.main_screen.PreviewMainScreenComponent
import com.brsv.itlink_gallery.presentation.main_screen.models.UiContentItem
import com.brsv.itlink_gallery.presentation.main_screen.models.UiImage
import com.brsv.itlink_gallery.presentation.ui.theme.ITLINK_GalleryTheme
import com.brsv.itlink_gallery.presentation.ui_kit.RetryableAsyncImage

@Composable
fun MainScreenContent(
    modifier: Modifier,
    component: MainScreenComponent
) {
    val uiState by component.uiState.collectAsState()

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
private fun ProgressLoader(
    modifier: Modifier
) {
    CircularProgressIndicator(
        modifier = modifier,
        strokeWidth = 4.dp,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ErrorScreen(
    modifier: Modifier,
    errorText: String,
    onRetryClicked: () -> Unit
) {
    Box(modifier = modifier) {
        Toast.makeText(
            LocalContext.current,
            errorText,
            Toast.LENGTH_SHORT
        ).show()

        Button(
            modifier = Modifier.align(Alignment.Center),
            onClick = onRetryClicked
        ) {
            Text(
                text = stringResource(R.string.action_retry),
            )
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
