package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.restrusher.partypuzz.ui.views.gameConfig.GameConfigViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.pm.ActivityInfo
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameConfigScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    gameModeName: Int,
    gameModeImage: Int,
    gameModeDescription: Int,
    onCreatePlayerClick: () -> Unit,
    onEditPlayerClick: (Int) -> Unit,
    onStartGameClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameConfigViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = stringResource(id = R.string.prepare_your_party)
    LaunchedEffect(key1 = title) {
        delay(100)
        setAppBarTitle(title)
    }
    Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = gameModeName),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .sharedElement(state = rememberSharedContentState(key = "game/${gameModeName}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 400)
                            })
                )
                Spacer(modifier = Modifier
                    .width(10.dp)
                    .weight(1f))
                Image(
                    painter = painterResource(id = gameModeImage),
                    contentDescription = stringResource(id = R.string.game_mode_image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(85.dp)
                        .padding(vertical = 20.dp)
                        .sharedElement(state = rememberSharedContentState(key = "game/${gameModeImage}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 400)
                            })
                )
            }
            Text(
                text = stringResource(id = gameModeDescription),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraLight,
                modifier = Modifier.fillMaxWidth()
            )
            OptionsContainer()
            Spacer(modifier = Modifier.height(10.dp))
            PlayersContainer(
                animatedVisibilityScope = animatedVisibilityScope,
                onAddPlayerClick = onCreatePlayerClick,
                onDeletePlayer = viewModel::deletePlayer,
                onEditPlayer = { player -> onEditPlayerClick(player.id) },
                modifier = Modifier.fillMaxWidth())
        }
        StartGameButton(
            onClick = { viewModel.onStartGame(onStartGameClick) },
            enabled = GamePlayersList.PlayersList.isNotEmpty() && GameOptionsSource.options.any { it.enabled },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .navigationBarsPadding()
                .padding(10.dp))
    }
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    }
}

@Composable
fun StartGameButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val disabledBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val disabledText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) onPrimary else primary,
        animationSpec = tween(durationMillis = 300),
        label = "bg color"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isPressed) primary else onPrimary,
        animationSpec = tween(durationMillis = 300),
        label = "text color"
    )

    val bgColor = if (enabled) animatedBgColor else disabledBg
    val textColor = if (enabled) animatedTextColor else disabledText

    val shape = RoundedCornerShape(50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 50.dp)
            .background(color = bgColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.start_game).uppercase(),
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun GameConfigScreenPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GameConfigScreen(
                    setAppBarTitle = { },
                    animatedVisibilityScope = this,
                    gameModeName = R.string.party_puzz_game_mode,
                    gameModeImage = R.drawable.img_partypuzz_mode_illustration,
                    gameModeDescription = R.string.party_puzz_description,
                    onCreatePlayerClick = {},
                    onEditPlayerClick = {},
                    onStartGameClick = {}
                )
            }
        }
    }
}
