package com.restrusher.partypuzz.ui.views.home

import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onGameOptionSelected: (Int, Int, Int, Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column {
                val pagerState = rememberPagerState(initialPage = 0) { uiState.gameModes.size }
                Column(modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 16.dp)) {
                    Text(
                        text = "${stringResource(R.string.tonight_mode)} · ${pagerState.currentPage + 1}/${uiState.gameModes.size}",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(R.string.whats_the),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.vibe),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                HorizontalPager(
                    state = pagerState,
                    key = { uiState.gameModes[it].imageId },
                    contentPadding = PaddingValues(horizontal = 40.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { index ->
                    GameModeCard(
                        animatedVisibilityScope = animatedVisibilityScope,
                        onPlayClick = { name, image, description ->
                            val partyId = if (uiState.isPartySelected) uiState.activeParty?.party?.id else null
                            onGameOptionSelected(name, image, description, partyId)
                        },
                        gameMode = uiState.gameModes[index],
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                ) {
                    repeat(uiState.gameModes.size) { index ->
                        val isSelected = index == pagerState.currentPage
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.onBackground
                                    else
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (uiState.hasParties) {
                    Text(
                        text = stringResource(id = if (uiState.isPartyCustomSelected) R.string.selected_party else R.string.last_party),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    LastPartyCard(
                        party = uiState.activeParty!!,
                        isSelected = uiState.isPartySelected,
                        onCardClick = viewModel::togglePartySelection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        onClick = viewModel::openDialog,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.choose_a_different_party),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }

        if (uiState.isDialogOpen) {
            PartyPickerDialog(
                allParties = uiState.allParties,
                dialogPendingPartyId = uiState.dialogPendingPartyId,
                onPartySelected = viewModel::selectDialogParty,
                onConfirm = viewModel::confirmPartySelection,
                onDismiss = viewModel::closeDialog
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=420,orientation=portrait"
)
@Composable
fun HomeScreenPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                HomeScreen(
                    animatedVisibilityScope = this,
                    onGameOptionSelected = { _, _, _, _ -> }
                )
            }
        }
    }
}
