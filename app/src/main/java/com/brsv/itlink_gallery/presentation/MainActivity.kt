package com.brsv.itlink_gallery.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.brsv.itlink_gallery.App
import com.brsv.itlink_gallery.presentation.main_screen.ui.MainScreenContent
import com.brsv.itlink_gallery.presentation.ui.theme.ITLINK_GalleryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainScreenComponentFactory = (application as App).mainScreenComponentFactory
        val mainScreenComponent = mainScreenComponentFactory.invoke(defaultComponentContext())

        enableEdgeToEdge()
        setContent {
            ITLINK_GalleryTheme {
                MainScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    component = mainScreenComponent
                )
            }
        }
    }
}
