package com.restrusher.partypuzz.ui.views.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.BouncingDotsAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StickyDaresBottomSheet(
    activeDares: List<ActiveStickyDare>,
    filterPlayer: Player?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val displayedDares = if (filterPlayer != null)
        activeDares.filter { it.playerName == filterPlayer.nickName }
    else
        activeDares

    // windowInsets = WindowInsets(0) lets the sheet background extend behind the nav bar;
    // navigationBarsPadding() on the content ensures items aren't hidden underneath it.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = filterPlayer?.nickName
                    ?: stringResource(R.string.active_dares_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            if (displayedDares.isEmpty()) {
                Text(
                    text = stringResource(
                        if (filterPlayer != null) R.string.no_active_dares_for_player
                        else R.string.no_active_dares
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            } else {
                displayedDares.forEach { dare ->
                    AnimatedVisibility(
                        visible = !dare.isCompleted,
                        exit = shrinkVertically(tween(350)) + fadeOut(tween(300))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            // padding(top) gives the upward bounce (-5dp) room to render
                            BouncingDotsAnimation(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            // weight(1f) so the text column expands and can wrap freely
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dare.presentContinuousText
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                // Only show player name when listing all players' dares
                                if (filterPlayer == null) {
                                    Text(
                                        text = dare.playerName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = dare.remainingSeconds.toRemainingTimeLabel(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
