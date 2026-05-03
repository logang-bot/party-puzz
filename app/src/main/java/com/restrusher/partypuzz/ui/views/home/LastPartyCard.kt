package com.restrusher.partypuzz.ui.views.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.ui.common.gameModeTheme
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val cardShape = RoundedCornerShape(16.dp)

@Composable
fun LastPartyCard(
    party: PartyWithPlayers,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSeeButton: Boolean = true
) {
    val gameModeNameRes = party.party.lastGameModeNameRes
    val theme = gameModeTheme(gameModeNameRes)
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val displayDate = party.party.lastUsedAt ?: party.party.dateCreation

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.outline
        else
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 400),
        label = "border"
    )

    val subtitle = "${dateFormat.format(Date(displayDate))} · " +
        stringResource(R.string.x_players, party.players.size) + " · " +
        stringResource(R.string.x_photos, party.photos.size)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(cardShape)
            .border(1.dp, borderColor, cardShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
            .clickable { onCardClick() }
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(theme.gradientColors))
        ) {
            Image(
                painter = painterResource(theme.iconId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = party.party.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun playerNamesSlice(
    players: List<PlayerEntity>,
    maxShown: Int = 3
): Pair<List<String>, Int> {
    val names = players.take(maxShown).map { p ->
        val first = p.nickName.trim().split("\\s+".toRegex()).firstOrNull() ?: p.nickName
        if (first.length > 10) "${first.take(10)}…" else first
    }
    return names to (players.size - names.size)
}

@Composable
@Preview(showBackground = true)
fun LastPartyCardPreview() {
    PartyPuzzTheme {
        val fakeParty = PartyWithPlayers(
            party = PartyEntity(id = 1, name = "Friday Party"),
            players = listOf(
                PlayerEntity(id = 1, nickName = "Laura", gender = Gender.Female, interestedIn = InterestedIn.Man),
                PlayerEntity(id = 2, nickName = "John", gender = Gender.Male, interestedIn = InterestedIn.Woman),
                PlayerEntity(id = 3, nickName = "Clara", gender = Gender.Female, interestedIn = InterestedIn.Man),
                PlayerEntity(id = 4, nickName = "Chris", gender = Gender.Male, interestedIn = InterestedIn.Woman),
            ),
            photos = emptyList()
        )
        LastPartyCard(
            party = fakeParty,
            isSelected = true,
            onCardClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
