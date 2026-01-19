package com.brsv.itlink_gallery.presentation.main_screen.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.brsv.itlink_gallery.presentation.main_screen.MainScreenViewModel
import com.brsv.itlink_gallery.presentation.ui.theme.ITLINK_GalleryTheme

@Composable
fun MainScreenContent(
    modifier: Modifier,
    viewModel: MainScreenViewModel = hiltViewModel()
) {

}

@Composable
@Preview
fun PreviewMainScreenContent() {
    ITLINK_GalleryTheme {
        MainScreenContent(modifier = Modifier.fillMaxSize())
    }
}
