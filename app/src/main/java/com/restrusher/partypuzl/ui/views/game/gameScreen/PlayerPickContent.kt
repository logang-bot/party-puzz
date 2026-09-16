package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto
import kotlinx.coroutines.delay

private const val LANDED_HOLD_MS = 650L
private const val PICKED_PHOTO_DP = 96
private const val PHOTO_NAME_GAP_DP = 24

/**
 * The turn intro. The deal area goes blank and player names flick through its centre, decelerating
 * onto the player whose turn it is; the landed name springs into focus, their photo drops in on the
 * same spring, and [onFinished] hands the turn to the picker — whose cards enter as the photo and
 * name travel up into the header.
 *
 * A tap skips to the landing. It sets `isSkipped` rather than calling [onFinished] directly,
 * because keying the sequence on that flag cancels the cycle in flight and re-enters it landed.
 */
@Composable
internal fun PlayerPickContent(
    uiState: GameScreenState,
    scopes: PlayerRevealScopes?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = uiState.selectedPlayer
    val path = remember(player) { player?.let { NameCyclePath.of(uiState.players, it) } }
    var cycleStep by remember(path) { mutableIntStateOf(0) }
    var hasLanded by remember(path) { mutableStateOf(false) }
    var isSkipped by remember(path) { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(path, isSkipped) {
        if (path == null) {
            onFinished()
            return@LaunchedEffect
        }
        if (!isSkipped) cycleNames(path) { cycleStep = it }
        cycleStep = path.lastStep
        hasLanded = true
        if (!isSkipped) delay(LANDED_HOLD_MS)
        onFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clickable(interactionSource = interactionSource, indication = null) { isSkipped = true }
            .padding(horizontal = 32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PickedPlayerPhoto(player = player, hasLanded = hasLanded, scopes = scopes)
            Spacer(Modifier.height(PHOTO_NAME_GAP_DP.dp))
            if (path != null) CyclingName(path = path, step = cycleStep, scopes = scopes)
            NameCentringSpacer()
        }
    }
}

/**
 * Balances the photo and its gap on the far side of the name, so the name holds the exact centre
 * of the screen whether the photo has arrived above it yet or not.
 */
@Composable
private fun NameCentringSpacer() {
    Spacer(Modifier.height((PICKED_PHOTO_DP + PHOTO_NAME_GAP_DP).dp))
}

@Composable
private fun PickedPlayerPhoto(
    player: Player?,
    hasLanded: Boolean,
    scopes: PlayerRevealScopes?,
    modifier: Modifier = Modifier
) {
    if (player == null) return
    val entry by animateFloatAsState(
        targetValue = if (hasLanded) 1f else 0f,
        animationSpec = landingSpring,
        label = "picked player photo"
    )

    PlayerPhoto(
        player = player,
        modifier = modifier
            .size(PICKED_PHOTO_DP.dp)
            .playerPhotoReveal(scopes)
            .graphicsLayer {
                alpha = entry.coerceIn(0f, 1f)
                scaleX = LANDING_SCALE_FROM + (1f - LANDING_SCALE_FROM) * entry
                scaleY = scaleX
            }
            .clip(RoundedCornerShape(28.dp))
    )
}

private val pickPreviewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bruno", Gender.Male, InterestedIn.Woman)
)

@Preview(name = "PlayerPickContent – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun PlayerPickContentPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize()) {
            PlayerPickContent(
                uiState = GameScreenState(
                    players = pickPreviewPlayers,
                    selectedPlayer = pickPreviewPlayers.first(),
                    dealPhase = GameDealPhase.PLAYER_PICK
                ),
                scopes = null,
                onFinished = {}
            )
        }
    }
}

@Preview(name = "PlayerPickContent – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun PlayerPickContentDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize()) {
            PlayerPickContent(
                uiState = GameScreenState(
                    players = pickPreviewPlayers,
                    selectedPlayer = pickPreviewPlayers[1],
                    dealPhase = GameDealPhase.PLAYER_PICK
                ),
                scopes = null,
                onFinished = {}
            )
        }
    }
}
