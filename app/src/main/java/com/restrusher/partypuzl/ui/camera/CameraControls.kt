package com.restrusher.partypuzl.ui.camera

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.CameraBackdrop
import com.restrusher.partypuzl.ui.theme.CameraControlContent
import com.restrusher.partypuzl.ui.theme.CameraControlScrim
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme

/** Bottom bar of the live camera: close, shutter and lens flip. */
@Composable
fun CameraControls(
    isCapturing: Boolean,
    onClose: () -> Unit,
    onShutter: () -> Unit,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CameraIconButton(R.drawable.ic_close, stringResource(R.string.camera_close), onClose)
        ShutterButton(isCapturing = isCapturing, onClick = onShutter)
        CameraIconButton(R.drawable.ic_flip_camera, stringResource(R.string.camera_flip), onFlip)
    }
}

@Composable
internal fun CameraIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(CameraControlScrim)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = CameraControlContent
        )
    }
}

@Composable
private fun ShutterButton(isCapturing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .border(4.dp, CameraControlContent, CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isCapturing) {
            CircularProgressIndicator(color = CameraControlContent)
        } else {
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CameraControlContent)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = stringResource(R.string.camera_shutter),
                    tint = CameraBackdrop
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CameraControlsPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        CameraControls(isCapturing = false, onClose = {}, onShutter = {}, onFlip = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CameraControlsDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        CameraControls(isCapturing = true, onClose = {}, onShutter = {}, onFlip = {})
    }
}
