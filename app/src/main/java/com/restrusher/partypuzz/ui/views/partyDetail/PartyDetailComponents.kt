package com.restrusher.partypuzz.ui.views.partyDetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PartyNameSection(
    name: String,
    gameModeName: String?,
    displayDate: Long,
    isEditing: Boolean,
    editedName: String,
    onEditClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = remember(displayDate) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(displayDate))
    }
    val headerText = if (gameModeName != null) "${gameModeName.uppercase()} · $dateText" else dateText.uppercase()

    Column(modifier = modifier) {
        Text(
            text = headerText,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
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
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onBackground,
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
                Button(onClick = onSave) { Text(text = stringResource(id = R.string.save)) }
                OutlinedButton(onClick = onDiscard) { Text(text = stringResource(id = R.string.discard)) }
            }
        }
    }
}

@Composable
fun StatCardRow(
    playerCount: Int,
    photoCount: Int,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        StatCard(count = playerCount, label = stringResource(R.string.players_label), modifier = Modifier.weight(1f))
        StatCard(count = photoCount, label = stringResource(R.string.photos_label), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(count: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartyPlayersGrid(
    players: List<PlayerEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.cast),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            players.forEach { player ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val avatarModifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                    when {
                        player.photoPath != null -> AsyncImage(
                            model = ImageRequest.Builder(context).data(File(player.photoPath)).build(),
                            contentDescription = stringResource(id = R.string.player_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = avatarModifier
                        )
                        player.avatarName != null -> {
                            val resId = context.resources.getIdentifier(
                                player.avatarName, "drawable", context.packageName
                            )
                            Image(
                                painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                                contentDescription = stringResource(id = R.string.player_avatar),
                                contentScale = ContentScale.Crop,
                                modifier = avatarModifier
                            )
                        }
                        else -> Image(
                            painter = painterResource(id = R.drawable.img_dummy_avatar),
                            contentDescription = stringResource(id = R.string.player_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = avatarModifier
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (player.nickName.length > 10) player.nickName.take(10) + "…" else player.nickName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
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
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.scrapbook),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.x_moments, photos.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (!hasCameraPermission) {
            Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
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
                photos.forEachIndexed { index, photo ->
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(File(photo.photoPath)).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPhotoClick(index) }
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
            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f),
            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
    ) {
        Text(
            text = stringResource(id = R.string.delete_party),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
