package com.restrusher.partypuzz.ui.views.game

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.LockScreenOrientation

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

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
                }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            }
        )
    }

    // TODO: route to the appropriate mini-game based on selected game mode
    FollowTheSpotScreen(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    )
}
