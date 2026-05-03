package com.restrusher.partypuzz.ui.views.createPlayer

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.restrusher.partypuzz.R
import java.io.File

@Composable
fun EditPlayerCard(
    imageUri: Uri,
    modifier: Modifier = Modifier,
    avatarRes: Int? = null,
    existingPhotoPath: String? = null,
    onShuffleClick: (() -> Unit)? = null
) {
    Box(modifier = modifier.size(240.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
        ) {
            when {
                imageUri.path?.isNotEmpty() == true -> AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(id = R.string.player_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                existingPhotoPath != null -> AsyncImage(
                    model = File(existingPhotoPath),
                    contentDescription = stringResource(id = R.string.player_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                else -> Image(
                    painter = painterResource(id = avatarRes ?: R.drawable.img_dummy_avatar),
                    contentDescription = stringResource(id = R.string.player_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (onShuffleClick != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onShuffleClick() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_random),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
