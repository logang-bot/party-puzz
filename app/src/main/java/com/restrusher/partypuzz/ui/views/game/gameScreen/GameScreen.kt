package com.restrusher.partypuzz.ui.views.game.gameScreen

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.flow.filterNotNull
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.LockScreenOrientation

private const val KEY_MINI_GAME_P1_SCORE = "mini_game_p1_score"
private const val KEY_MINI_GAME_P2_SCORE = "mini_game_p2_score"

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMiniGame: (miniGame: MiniGame, challenger: Player, opponent: Player) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameScreenViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val backStackEntry = LocalViewModelStoreOwner.current as? NavBackStackEntry
    LaunchedEffect(backStackEntry) {
        backStackEntry?.savedStateHandle
            ?.getStateFlow<Int?>(KEY_MINI_GAME_P1_SCORE, null)
            ?.filterNotNull()
            ?.collect { p1Score ->
                val p2Score = backStackEntry.savedStateHandle
                    .get<Int>(KEY_MINI_GAME_P2_SCORE) ?: return@collect
                viewModel.onMiniGameResultReceived(p1Score, p2Score)
                backStackEntry.savedStateHandle.remove<Int>(KEY_MINI_GAME_P1_SCORE)
                backStackEntry.savedStateHandle.remove<Int>(KEY_MINI_GAME_P2_SCORE)
            }
    }
    LaunchedEffect(backStackEntry) {
        backStackEntry?.savedStateHandle
            ?.getStateFlow<Boolean?>("mini_game_aborted", null)
            ?.filterNotNull()
            ?.collect {
                viewModel.onMiniGameAborted()
                backStackEntry.savedStateHandle.remove<Boolean>("mini_game_aborted")
            }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backgroundGradient = rememberBackgroundGradient()

    var showExitDialog by remember { mutableStateOf(false) }
    var showInfoPanel by remember { mutableStateOf(false) }
    // null = sheet closed; non-null = open for that player; use sentinel ALL_PLAYERS for all-dares
    var daresSheetPlayer: Player? by remember { mutableStateOf(null) }
    var showDaresSheet by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = stringResource(id = R.string.exit_game_title)) },
            text = { Text(text = stringResource(id = R.string.exit_game_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onNavigateBack()
                }) { Text(text = stringResource(id = R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            }
        )
    }

    if (showDaresSheet) {
        StickyDaresBottomSheet(
            activeDares = uiState.activeStickyDares,
            filterPlayer = daresSheetPlayer,
            onDismiss = {
                showDaresSheet = false
                daresSheetPlayer = null
            },
            onCancelDare = viewModel::cancelStickyDare
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar: Box so exit and info are always anchored to their edges
            // regardless of whether the pill is visible
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                IconButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_door_back),
                        contentDescription = stringResource(id = R.string.exit_game_title),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                StickyDarePill(
                    activeDares = uiState.activeStickyDares,
                    onClick = {
                        daresSheetPlayer = null
                        showDaresSheet = true
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        // horizontal padding leaves room for the two icon buttons (48dp each)
                        .padding(horizontal = 52.dp)
                )
                IconButton(
                    onClick = { showInfoPanel = !showInfoPanel },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = stringResource(id = R.string.game_configuration),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            GameDealSection(
                uiState = uiState,
                onGameDealTapped = viewModel::onGameDealTapped,
                onChallengeDismissed = viewModel::onChallengeDismissed,
                onTruthOrDareChosen = viewModel::onTruthOrDareChosen,
                onTruthOrDareSkipped = viewModel::onTruthOrDareSkipped,
                onStickyDareSkipped = viewModel::onStickyDareSkipped,
                onMiniGameDealFinished = viewModel::onMiniGameDealFinished,
                onGeneralKnowledgeAnswered = viewModel::onGeneralKnowledgeAnswered,
                onMiniGameOpponentSelected = { opponent ->
                    viewModel.onMiniGameOpponentSelected(opponent)
                    val miniGame = uiState.miniGame
                    val challenger = uiState.selectedPlayer
                    if (miniGame != null && challenger != null) {
                        onNavigateToMiniGame(miniGame, challenger, opponent)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            PlayersListRow(
                players = uiState.players,
                selectedPlayer = uiState.selectedPlayer,
                dealPhase = uiState.dealPhase,
                activeStickyDares = uiState.activeStickyDares,
                onPlayerTapped = { player ->
                    daresSheetPlayer = player
                    showDaresSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }

        // Bar event dialog overlay
        AnimatedVisibility(
            visible = uiState.barMode.activeEvent != null,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            uiState.barMode.activeEvent?.let { event ->
                BarEventDialog(
                    event = event,
                    onDismiss = viewModel::onBarEventDismissed
                )
            }
        }

        // Info panel overlay — dismissed by tapping the backdrop
        AnimatedVisibility(
            visible = showInfoPanel,
            enter = scaleIn(tween(200), transformOrigin = TransformOrigin(1f, 0f)) + fadeIn(tween(200)),
            exit = scaleOut(tween(150), transformOrigin = TransformOrigin(1f, 0f)) + fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showInfoPanel = false }
                )
                GameInfoPanel(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 56.dp, end = 8.dp)
                )
            }
        }
    }
}
