package com.restrusher.partypuzz.ui.views.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GameModesDatasource
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    uiState: HomeState,
    onGameOptionSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lastParty = uiState.lastParty
    val hasParties = !uiState.isLoading && lastParty != null
    var isPartySelected by remember { mutableStateOf(false) }
    val players = if (isPartySelected) lastParty?.players ?: emptyList() else emptyList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 30.dp, start = 15.dp, end = 15.dp)
            )
            val gamesModes = GameModesDatasource.gameModesList
            val pagerState = rememberPagerState(initialPage = 0) { 4 }
            HorizontalPager(
                state = pagerState,
                key = { gamesModes[it].imageId },
                pageSize = PageSize.Fill,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { index ->
                GameModeCard(
                    animatedVisibilityScope = animatedVisibilityScope,
                    onPlayClick = onGameOptionSelected,
                    gameMode = gamesModes[index],
                    players = players,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                        .padding(horizontal = 20.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (hasParties) {
                Text(
                    text = stringResource(id = R.string.last_party),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                LastPartyCard(
                    party = lastParty!!,
                    isSelected = isPartySelected,
                    onCardClick = { isPartySelected = !isPartySelected },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.choose_a_different_party),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else if (!uiState.isLoading) {
                Text(
                    text = stringResource(id = R.string.no_parties_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun HomeScreenPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                HomeScreen(
                    animatedVisibilityScope = this,
                    uiState = HomeState(),
                    onGameOptionSelected = { _, _ -> }
                )
            }
        }
    }
}
