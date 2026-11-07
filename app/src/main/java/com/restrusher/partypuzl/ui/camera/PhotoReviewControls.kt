package com.restrusher.partypuzl.ui.camera

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.CameraControlContent
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme

/** Bottom bar shown over a just-taken photo: retake it or keep it. */
@Composable
fun PhotoReviewControls(
    onRetake: () -> Unit,
    onUsePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.camera_retake), color = CameraControlContent)
        }
        Button(onClick = onUsePhoto, modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.camera_use_photo))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PhotoReviewControlsPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        PhotoReviewControls(onRetake = {}, onUsePhoto = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhotoReviewControlsDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        PhotoReviewControls(onRetake = {}, onUsePhoto = {})
    }
}
