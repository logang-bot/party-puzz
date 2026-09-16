package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto

/**
 * Whose turn it is. Shared by the deal picker and the surprise spotlight, which share a frame.
 *
 * Given [scopes], the photo and name are the landing point of the turn intro's reveal: they arrive
 * by travelling up from the centre of the screen rather than fading in where they sit.
 */
@Composable
internal fun CurrentPlayerHeader(
    player: Player?,
    modifier: Modifier = Modifier,
    scopes: PlayerRevealScopes? = null
) {
    if (player == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        PlayerPhoto(
            player = player,
            modifier = Modifier
                .size(48.dp)
                .playerPhotoReveal(scopes)
                .clip(RoundedCornerShape(14.dp))
        )
        Spacer(Modifier.size(12.dp))
        Column {
            TurnLabel(scopes = scopes)
            Text(
                text = player.nickName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.playerNameReveal(scopes)
            )
        }
    }
}

/** The kicker has no counterpart in the reveal, so it fades in once the name has arrived. */
@Composable
private fun TurnLabel(scopes: PlayerRevealScopes?, modifier: Modifier = Modifier) {
    val entry = scopes?.animatedVisibilityScope?.let { scope ->
        with(scope) {
            Modifier.animateEnterExit(
                enter = fadeIn(tween(durationMillis = PLAYER_REVEAL_MS, delayMillis = PLAYER_REVEAL_MS / 2)),
                exit = fadeOut(tween(PLAYER_REVEAL_MS / 2))
            )
        }
    } ?: Modifier

    Text(
        text = stringResource(R.string.its_your_turn).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
        modifier = modifier.then(entry)
    )
}

private val headerPreviewPlayer = Player(1, "Alice", Gender.Female, InterestedIn.Man)

@Preview(name = "CurrentPlayerHeader – Light", showBackground = true, widthDp = 360, heightDp = 120)
@Composable
private fun CurrentPlayerHeaderPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Row(Modifier.appBackground().padding(24.dp)) {
            CurrentPlayerHeader(player = headerPreviewPlayer)
        }
    }
}

@Preview(name = "CurrentPlayerHeader – Dark", showBackground = true, widthDp = 360, heightDp = 120)
@Composable
private fun CurrentPlayerHeaderDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Row(Modifier.appBackground().padding(24.dp)) {
            CurrentPlayerHeader(player = headerPreviewPlayer)
        }
    }
}
