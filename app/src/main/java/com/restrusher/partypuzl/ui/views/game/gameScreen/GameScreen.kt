package com.restrusher.partypuzl.ui.views.game.gameScreen

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.common.LockScreenOrientation
import com.restrusher.partypuzl.ui.theme.appBackground
import java.io.File
import kotlinx.coroutines.flow.filterNotNull

private const val KEY_MINI_GAME_P1_SCORE = "mini_game_p1_score"
private const val KEY_MINI_GAME_P2_SCORE = "mini_game_p2_score"
private const val PLAYER_RAIL_HEIGHT_DP = 72

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMiniGame: (miniGame: MiniGame, challenger: Player, opponent: Player) -> Unit,
    onNavigateToGlobalMiniGame: (miniGame: MiniGame) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameScreenViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val context = LocalContext.current
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
    LaunchedEffect(backStackEntry) {
        backStackEntry?.savedStateHandle
            ?.getStateFlow<String?>("hot_potato_loser", null)
            ?.filterNotNull()
            ?.collect { loserName ->
                viewModel.onHotPotatoResultReceived(loserName)
                backStackEntry.savedStateHandle.remove<String>("hot_potato_loser")
            }
    }
    LaunchedEffect(backStackEntry) {
        backStackEntry?.savedStateHandle
            ?.getStateFlow<String?>("simon_says_loser", null)
            ?.filterNotNull()
            ?.collect { loserName ->
                viewModel.onSimonSaysResultReceived(loserName)
                backStackEntry.savedStateHandle.remove<String>("simon_says_loser")
            }
    }
    LaunchedEffect(backStackEntry) {
        backStackEntry?.savedStateHandle
            ?.getStateFlow<String?>("circle_master_loser", null)
            ?.filterNotNull()
            ?.collect { loserName ->
                viewModel.onCircleMasterResultReceived(loserName)
                backStackEntry.savedStateHandle.remove<String>("circle_master_loser")
            }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gameBackground = rememberGameBackground(uiState)

    val cameraUri = remember {
        val tempFile = File.createTempFile("party_photo_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
            .apply { createNewFile(); deleteOnExit() }
        FileProvider.getUriForFile(context.applicationContext, "${context.packageName}.provider", tempFile)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) viewModel.onPhotoCaptured(cameraUri) else viewModel.onCameraRequestDismissed()
    }

    var showExitDialog by remember { mutableStateOf(false) }
    // null = all dares; non-null = only that player's dares
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
            .appBackground(gameBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
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
                        tint = MaterialTheme.colorScheme.onBackground
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
                        // leaves room for the exit button on either side of the centred pill
                        .padding(horizontal = 52.dp)
                )
            }

            GameDealSection(
                uiState = uiState,
                onDealChosen = viewModel::onDealChosen,
                onSurpriseRequested = viewModel::onSurpriseRequested,
                onChallengeDismissed = viewModel::onChallengeDismissed,
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
                onGlobalMiniGameStarted = {
                    val miniGame = uiState.miniGame
                    if (miniGame != null) onNavigateToGlobalMiniGame(miniGame)
                },
                onModeEventDismissed = viewModel::onModeEventDismissed,
                onGiveDrinksTargetSelected = viewModel::onGiveDrinksTargetSelected,
                onCameraRequested = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                    if (granted) cameraLauncher.launch(cameraUri)
                    else viewModel.onCameraRequestDismissed()
                },
                onCameraRequestDismissed = viewModel::onCameraRequestDismissed,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            PlayersListRow(
                players = uiState.players,
                selectedPlayer = uiState.selectedPlayer,
                activeStickyDares = uiState.activeStickyDares,
                onPlayerTapped = { player ->
                    daresSheetPlayer = player
                    showDaresSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PLAYER_RAIL_HEIGHT_DP.dp)
            )
        }
    }
}
