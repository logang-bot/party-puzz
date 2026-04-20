package com.restrusher.partypuzz.ui.views.game.miniGames.hotPotato

import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.BackPressToExit
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import com.restrusher.partypuzz.ui.views.game.common.MiniGameCountdownOverlay
import kotlinx.coroutines.delay

private const val BG_COLOR_STEP_MS = 600

private val BgColors = listOf(
    Color(0xFFFF80AB),
    Color(0xFF80D8FF),
    Color(0xFFCCFF90),
    Color(0xFFFFFF8D),
)

@Composable
fun HotPotatoScreen(
    onGameFinished: (loserName: String) -> Unit,
    onAbortGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HotPotatoViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackPressToExit(
        warningMessage = stringResource(R.string.press_back_again_to_exit),
        onExit = onAbortGame
    )
    HotPotatoContent(
        uiState = uiState,
        onPassTapped = viewModel::onPassTapped,
        onGameFinished = { onGameFinished(uiState.loser?.nickName.orEmpty()) },
        modifier = modifier
    )
}

@Composable
internal fun HotPotatoContent(
    uiState: HotPotatoState,
    onPassTapped: () -> Unit,
    onGameFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var colorIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.isGameRunning) {
        if (uiState.isGameRunning) {
            while (true) {
                delay(BG_COLOR_STEP_MS.toLong())
                colorIndex = (colorIndex + 1) % BgColors.size
            }
        }
    }
    val bgColor by animateColorAsState(
        targetValue = BgColors[colorIndex],
        animationSpec = tween(durationMillis = BG_COLOR_STEP_MS, easing = LinearEasing),
        label = "bgColor"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (uiState.isGameRunning) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "bgAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor.copy(alpha = bgAlpha))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = uiState.isGameRunning || uiState.loserIndex != null
            ) {
                if (uiState.isGameRunning) onPassTapped() else onGameFinished()
            }
    ) {
        HotPotatoHolderCard(
            uiState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        )
        AnimatedVisibility(
            visible = uiState.isCountingDown,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400))
        ) {
            MiniGameCountdownOverlay(
                countdownValue = uiState.countdownValue,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bob", Gender.Male, InterestedIn.Woman),
    Player(3, "Carol", Gender.Female, InterestedIn.Both)
)

@Preview(name = "Running", showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Composable
private fun HotPotatoRunningPreview() {
    PartyPuzzTheme {
        HotPotatoContent(
            uiState = HotPotatoState(players = previewPlayers, currentHolderIndex = 0, isGameRunning = true),
            onPassTapped = {}, onGameFinished = {}
        )
    }
}

@Preview(name = "Game over", showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait")
@Composable
private fun HotPotatoGameOverPreview() {
    PartyPuzzTheme {
        HotPotatoContent(
            uiState = HotPotatoState(players = previewPlayers, currentHolderIndex = 1, loserIndex = 1),
            onPassTapped = {}, onGameFinished = {}
        )
    }
}
