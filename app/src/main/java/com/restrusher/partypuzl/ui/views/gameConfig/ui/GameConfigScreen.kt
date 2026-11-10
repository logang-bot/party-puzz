package com.restrusher.partypuzl.ui.views.gameConfig.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzl.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzl.ui.common.ads.AdBanner
import com.restrusher.partypuzl.ui.common.ads.AdPlacement
import com.restrusher.partypuzl.ui.common.ads.LocalAdsProvider
import com.restrusher.partypuzl.ui.common.LoadingScrim
import com.restrusher.partypuzl.ui.common.LockScreenOrientation
import com.restrusher.partypuzl.ui.common.gameModeTheme
import com.restrusher.partypuzl.ui.common.ads.rememberRewardedAd
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.ReportPageTint
import com.restrusher.partypuzl.ui.theme.ctaScrim
import com.restrusher.partypuzl.ui.views.gameConfig.GameConfigViewModel
import com.restrusher.partypuzl.ui.views.gameConfig.text
import kotlinx.coroutines.delay

private const val MIN_PLAYERS_TO_START = 2

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
    onManagePacksClick: () -> Unit,
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
    LaunchedEffect(gameModeName) {
        GameOptionsSource.currentGameModeNameRes = gameModeName
    }
    // Reported rather than read back off GameOptionsSource: that is a plain var, so the
    // scaffold would not recompose when the line above lands and the page would keep the
    // previous session's tint.
    ReportPageTint(gameModeTheme(gameModeName).gradientColors.first())

    val isEnterSettled = rememberEnterTransitionSettled(animatedVisibilityScope)
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Loaded up front so the unlock sheet can offer the ad immediately rather than after a wait.
    val rewardedAd = rememberRewardedAd(AdPlacement.PACK_UNLOCK_REWARDED)
    val showAdOption = LocalAdsProvider.current.supportsRewardedAds

    Box(modifier = modifier.fillMaxSize()) {
        // The page scrolls under the CTA rather than stopping above it, so the bottom inset
        // goes on the scrolling content — after verticalScroll, so it travels with it.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
        ) {
            GameModeHeader(
                animatedVisibilityScope = animatedVisibilityScope,
                gameModeName = gameModeName,
                gameModeImage = gameModeImage,
                gameModeDescription = gameModeDescription
            )

            ResponsibleDrinkingHint(gameModeName, Modifier.padding(top = 12.dp))

            Spacer(modifier = Modifier.height(20.dp))
            AdBanner(placement = AdPlacement.GAME_CONFIG_BANNER)

            Spacer(modifier = Modifier.height(12.dp))
            PlayersContainer(
                animatedVisibilityScope = animatedVisibilityScope,
                showPlayers = isEnterSettled,
                onAddPlayerClick = onCreatePlayerClick,
                onDeletePlayer = viewModel::deletePlayer,
                onEditPlayer = { player -> onEditPlayerClick(player.id) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
            QuestionPacksSection(
                isReady = isEnterSettled && uiState.arePacksLoaded,
                officialPacks = uiState.officialPacks,
                premiumPacks = uiState.premiumPacks,
                customPacks = uiState.customPacks,
                onTogglePack = viewModel::onTogglePack,
                onUnlockPack = viewModel::onUnlockRequested,
                onManagePacks = onManagePacksClick
            )

            Spacer(modifier = Modifier.height(16.dp))
            MiniGamesHintBox()
        }

        StartGameButton(
            onClick = { viewModel.onStartGame(onStartGameClick) },
            enabled = GamePlayersList.PlayersList.size >= MIN_PLAYERS_TO_START &&
                    uiState.hasEnabledPack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .ctaScrim()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp)
        )

        if (uiState.isLoading) {
            LoadingScrim()
        }
    }

    uiState.unlockTarget?.let { target ->
        UnlockChoiceBottomSheet(
            pack = target,
            showAdOption = showAdOption,
            isAdReady = rewardedAd.isReady,
            activity = activity,
            onWatchAd = {
                activity?.let { host ->
                    rewardedAd.show(host) {
                        viewModel.onRewardEarned(target.id)
                        // show() consumes the ad; reload so another pack can be unlocked too.
                        rewardedAd.reload()
                    }
                }
            },
            onDismiss = viewModel::onUnlockDismissed
        )
    }
}

/** Unwraps the Activity the composable is hosted in — needed to show ads and billing flows. */
private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun GameConfigScreenPreview() {
    PartyPuzlTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GameConfigScreen(
                    setAppBarTitle = { },
                    animatedVisibilityScope = this,
                    gameModeName = R.string.party_puzz_game_mode,
                    gameModeImage = R.drawable.ic_partypuzz,
                    gameModeDescription = R.string.party_puzz_description,
                    onCreatePlayerClick = {},
                    onEditPlayerClick = {},
                    onStartGameClick = {},
                    onManagePacksClick = {}
                )
            }
        }
    }
}
