package com.restrusher.partypuzz.ui.views.parties

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val cardShape = RoundedCornerShape(20.dp)
private const val MAX_NAMES_SHOWN = 3

@Composable
fun PartyCard(
    party: PartyWithPlayers,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val players = party.players
    val gameModeNameRes = party.party.lastGameModeNameRes
    val gradientColors = gameModeGradient(gameModeNameRes)
    val decorativeImageRes = gameModeImageRes(gameModeNameRes)
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val displayDate = party.party.lastUsedAt ?: party.party.dateCreation

    val displayedNames = players.take(MAX_NAMES_SHOWN).map { player ->
        val first = player.nickName.trim().split("\\s+".toRegex()).firstOrNull() ?: player.nickName
        if (first.length > 10) "${first.take(10)}…" else first
    }
    val remaining = players.size - displayedNames.size
    val namesText = if (remaining > 0)
        "${displayedNames.joinToString(", ")} ${stringResource(R.string.and_x_more, remaining)}"
    else
        displayedNames.joinToString(", ")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(brush = Brush.linearGradient(gradientColors))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(decorativeImageRes),
            contentDescription = null,
            alpha = 0.12f,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-8).dp)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (gameModeNameRes != null)
                            stringResource(gameModeNameRes).uppercase()
                        else "",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = dateFormat.format(Date(displayDate)),
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = party.party.name,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                    players.take(3).forEach { player ->
                        PlayerAvatarSmall(player = player)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = namesText,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${party.photos.size}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerAvatarSmall(player: PlayerEntity) {
    val context = LocalContext.current
    when {
        player.photoPath != null -> AsyncImage(
            model = ImageRequest.Builder(context).data(File(player.photoPath)).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(36.dp).clip(CircleShape)
        )
        player.avatarName != null -> {
            val resId = context.resources.getIdentifier(
                player.avatarName, "drawable", context.packageName
            )
            Image(
                painter = painterResource(if (resId != 0) resId else R.drawable.img_dummy_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape)
            )
        }
        else -> Image(
            painter = painterResource(R.drawable.img_dummy_avatar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(36.dp).clip(CircleShape)
        )
    }
}

private fun gameModeGradient(gameModeNameRes: Int?): List<Color> = when (gameModeNameRes) {
    R.string.standard_game_mode -> listOf(Color(0xFF1B7B7B), Color(0xFF0E5252))
    R.string.bar_game_mode -> listOf(Color(0xFFE87060), Color(0xFFCA4535))
    R.string.couples_game_mode -> listOf(Color(0xFFCC50A8), Color(0xFF8A35C0))
    R.string.party_puzz_game_mode -> listOf(Color(0xFF6848C0), Color(0xFF472898))
    else -> listOf(Color(0xFF2A4060), Color(0xFF162840))
}

private fun gameModeImageRes(gameModeNameRes: Int?): Int = when (gameModeNameRes) {
    R.string.standard_game_mode -> R.drawable.img_standard_illustration
    R.string.bar_game_mode -> R.drawable.img_bar_mode_illustration
    R.string.couples_game_mode -> R.drawable.img_couples_mode_illustration
    R.string.party_puzz_game_mode -> R.drawable.img_partypuzz_mode_illustration
    else -> R.drawable.img_standard_illustration
}
