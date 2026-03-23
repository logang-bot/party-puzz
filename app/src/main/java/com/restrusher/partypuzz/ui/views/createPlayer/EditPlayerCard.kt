package com.restrusher.partypuzz.ui.views.createPlayer

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.restrusher.partypuzz.R
import java.io.File

@Composable
fun EditPlayerCard(
    imageUri: Uri,
    playerName: String,
    modifier: Modifier = Modifier,
    avatarRes: Int? = null,
    existingPhotoPath: String? = null,
) {
    Box(
        modifier = modifier
            .width(300.dp)
            .height(300.dp)
            .clip(RoundedCornerShape(70.dp))
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent, colorResource(id = R.color.black)
                        )
                    )
                )
                .padding(top = 50.dp, start = 6.dp, end = 6.dp, bottom = 5.dp)
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.white),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }
    }
}