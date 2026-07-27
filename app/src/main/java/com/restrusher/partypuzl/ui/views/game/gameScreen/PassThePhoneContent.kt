package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto

/**
 * Split-screen hand-off: the phone lies flat on the table, so the top half is rotated 180° to read
 * upright for whoever is sitting across from the current player.
 *
 * Not part of the game turn flow — the game opens straight on the deal picker. This is parked here
 * as the starting point for the Follow The Spot mini-game redesign; keep it until that lands.
 */
@Composable
internal fun PassThePhoneContent(
    player: Player?,
    roundNumber: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(interactionSource = interactionSource, indication = null) { onContinue() }
    ) {
        PassHalf(player = player, modifier = Modifier.rotate(180f))
        RoundDivider(roundNumber = roundNumber)
        PassHalf(player = player)
    }
}

@Composable
private fun ColumnScope.PassHalf(player: Player?, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.pass_the_phone_to).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        if (player != null) {
            PlayerPhoto(
                player = player,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = player.nickName,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.tap_when_ready).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun RoundDivider(roundNumber: Int, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .drawBehind {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 14f))
                )
            }
    ) {
        Text(
            text = stringResource(R.string.round_number, roundNumber).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 5.dp)
        )
    }
}

private val previewPlayer = Player(1, "Alice", Gender.Female, InterestedIn.Man)

@Preview(name = "PassThePhone – Light", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PassThePhoneLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.background(Color(0xFFFFF5E6)).fillMaxSize()) {
            PassThePhoneContent(player = previewPlayer, roundNumber = 3, onContinue = {})
        }
    }
}

@Preview(name = "PassThePhone – Dark", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PassThePhoneDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF0B1F24)).fillMaxSize()) {
            PassThePhoneContent(player = previewPlayer, roundNumber = 3, onContinue = {})
        }
    }
}
