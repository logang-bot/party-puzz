package com.restrusher.partypuzz.ui.views.parties

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val cardShape = RoundedCornerShape(15.dp)
private const val MAX_NAMES_SHOWN = 3

@Composable
fun PartyCard(
    party: PartyWithPlayers,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val players = party.players
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

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
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = cardShape)
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.03f))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 12.dp)
        ) {
            Text(
                text = party.party.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy((-15).dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                players.take(3).forEach { player ->
                    when {
                        player.photoPath != null -> AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(player.photoPath))
                                .build(),
                            contentDescription = stringResource(id = R.string.player_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(46.dp)
                                .height(46.dp)
                                .clip(CircleShape)
                        )
                        player.avatarName != null -> {
                            val resId = context.resources.getIdentifier(
                                player.avatarName, "drawable", context.packageName
                            )
                            Image(
                                painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                                contentDescription = stringResource(id = R.string.player_avatar),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(46.dp)
                                    .height(46.dp)
                                    .clip(CircleShape)
                            )
                        }
                        else -> Image(
                            painter = painterResource(id = R.drawable.img_dummy_avatar),
                            contentDescription = stringResource(id = R.string.player_avatar),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(46.dp)
                                .height(46.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            Text(
                text = namesText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            val lastUsed = party.party.lastUsedAt
            Text(
                text = if (lastUsed != null)
                    stringResource(R.string.last_used, dateFormat.format(Date(lastUsed)))
                else
                    stringResource(R.string.created_on, dateFormat.format(Date(party.party.dateCreation))),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
