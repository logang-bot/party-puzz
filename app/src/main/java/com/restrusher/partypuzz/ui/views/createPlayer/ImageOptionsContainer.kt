package com.restrusher.partypuzz.ui.views.createPlayer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R

@Composable
fun ImageOptionsContainer(
    capturedImageUri: Uri,
    takePictureAction: () -> Unit,
    generateRandomImageAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPhoto = capturedImageUri.path?.isNotEmpty() == true
    val pillShape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .clip(pillShape)
            .background(Color.Transparent)
            .padding(4.dp)
    ) {
        SegmentOption(
            selected = !hasPhoto,
            iconRes = R.drawable.ic_random,
            label = stringResource(R.string.generated),
            onClick = generateRandomImageAction,
            modifier = Modifier.weight(1f)
        )
        SegmentOption(
            selected = hasPhoto,
            iconRes = R.drawable.ic_camera,
            label = stringResource(R.string.take_photo),
            onClick = takePictureAction,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentOption(
    selected: Boolean,
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}
