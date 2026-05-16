package com.restrusher.partypuzz.ui.views.createPlayer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.SegmentItem
import com.restrusher.partypuzz.ui.common.SegmentedSelector

@Composable
fun ImageOptionsContainer(
    capturedImageUri: Uri,
    takePictureAction: () -> Unit,
    generateRandomImageAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPhoto = capturedImageUri.path?.isNotEmpty() == true

    SegmentedSelector(
        items = listOf(
            SegmentItem(
                iconRes = R.drawable.ic_random,
                label = stringResource(R.string.generated),
                selected = !hasPhoto,
                onClick = generateRandomImageAction
            ),
            SegmentItem(
                iconRes = R.drawable.ic_camera,
                label = stringResource(R.string.take_photo),
                selected = hasPhoto,
                onClick = takePictureAction
            )
        ),
        modifier = modifier
    )
}
