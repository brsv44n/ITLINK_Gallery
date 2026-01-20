package com.brsv.itlink_gallery.presentation.ui_kit

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.brsv.itlink_gallery.R

@Composable
fun ErrorScreen(
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
