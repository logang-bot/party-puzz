package com.restrusher.partypuzz.ui.views.partyDetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import java.io.File

@Composable
fun PartyNameSection(
    name: String,
    isEditing: Boolean,
    editedName: String,
    onEditClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = isEditing,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                modifier = Modifier.weight(1f),
                label = "partyNameContent"
            ) { editing ->
                if (editing) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = onNameChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!isEditing) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_edit),
                        contentDescription = stringResource(id = R.string.edit_party_name),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        AnimatedVisibility(visible = isEditing) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Button(onClick = onSave) {
                    Text(text = stringResource(id = R.string.save))
                }
                OutlinedButton(onClick = onDiscard) {
                    Text(text = stringResource(id = R.string.discard))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartyPlayersGrid(
    players: List<PlayerEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        players.forEach { player ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    player.photoPath != null -> AsyncImage(
                        model = ImageRequest.Builder(context).data(File(player.photoPath)).build(),
                        contentDescription = stringResource(id = R.string.player_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(64.dp).height(64.dp).clip(CircleShape)
                    )
                    player.avatarName != null -> {
                        val resId = context.resources.getIdentifier(
                            player.avatarName, "drawable", context.packageName
                        )
                        Image(
                            painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                            contentDescription = stringResource(id = R.string.player_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.width(64.dp).height(64.dp).clip(CircleShape)
                        )
                    }
                    else -> Image(
                        painter = painterResource(id = R.drawable.img_dummy_avatar),
                        contentDescription = stringResource(id = R.string.player_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(64.dp).height(64.dp).clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (player.nickName.length > 10) player.nickName.take(10) + "…" else player.nickName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartyPhotoAlbumSection(
    photos: List<PartyPhotoEntity>,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.party_album_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (!hasCameraPermission) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.grant_camera_permission))
            }
        } else if (photos.isEmpty()) {
            Text(
                text = stringResource(id = R.string.party_album_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                photos.forEach { photo ->
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(File(photo.photoPath)).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun DeletePartyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text(
            text = stringResource(id = R.string.delete_party),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
