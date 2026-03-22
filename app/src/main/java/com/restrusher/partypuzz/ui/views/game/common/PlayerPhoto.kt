package com.restrusher.partypuzz.ui.views.game.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Player
import java.io.File

@Composable
internal fun PlayerPhoto(
    player: Player,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    when {
        player.photoPath != null -> AsyncImage(
            model = ImageRequest.Builder(context).data(File(player.photoPath)).build(),
            contentDescription = stringResource(id = R.string.player_avatar),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        player.avatarName != null -> {
            val resId = context.resources.getIdentifier(
                player.avatarName, "drawable", context.packageName
            )
            Image(
                painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                contentDescription = stringResource(id = R.string.player_avatar),
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        else -> Image(
            painter = painterResource(id = R.drawable.img_dummy_avatar),
            contentDescription = stringResource(id = R.string.player_avatar),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
