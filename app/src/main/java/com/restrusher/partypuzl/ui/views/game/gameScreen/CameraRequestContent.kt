package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PageBackground
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.TintStrength
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ink

/** The photo-moment background the card really sits on, so the artboards show it in place. */
private val previewPhotoMomentBackground = PageBackground.TintedGlow(
    tint = AccentPink,
    strength = TintStrength.Prominent,
    centerYRatio = 0.2f,
    baseStop = 0.7f
)

@Composable
internal fun CameraRequestContent(
    onCameraRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.camera_request_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.camera_request_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Standard),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onCameraRequested,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.camera_request_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.tap_to_dismiss),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "CameraRequest – Light", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun CameraRequestLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(modifier = Modifier.fillMaxSize().appBackground(previewPhotoMomentBackground)) {
            CameraRequestContent(
                onCameraRequested = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "CameraRequest – Dark", showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun CameraRequestDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(modifier = Modifier.fillMaxSize().appBackground(previewPhotoMomentBackground)) {
            CameraRequestContent(
                onCameraRequested = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
