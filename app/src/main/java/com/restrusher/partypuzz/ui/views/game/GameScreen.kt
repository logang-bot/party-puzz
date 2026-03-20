package com.restrusher.partypuzz.ui.views.game

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import androidx.compose.foundation.layout.Box

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameScreenViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

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
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_door_back),
                    contentDescription = stringResource(id = R.string.exit_game_title),
                    tint = Color.White
                )
            }
            GameDealSection(
                uiState = uiState,
                onGameDealTapped = viewModel::onGameDealTapped,
                onChallengeDismissed = viewModel::onChallengeDismissed,
                onTruthOrDareChosen = viewModel::onTruthOrDareChosen,
                onGeneralKnowledgeAnswered = viewModel::onGeneralKnowledgeAnswered,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            PlayersListRow(
                players = uiState.players,
                selectedPlayer = uiState.selectedPlayer,
                dealPhase = uiState.dealPhase,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
    }
}
