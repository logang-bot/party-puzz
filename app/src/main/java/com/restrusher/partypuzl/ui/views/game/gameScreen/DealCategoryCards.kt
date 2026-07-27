package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme

/** Large promoted card for the deal that was played last, party-wide. */
@Composable
internal fun DealHeroCard(
    accent: DealAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 108.dp)
            .clip(dealCardShape)
            .background(Brush.linearGradient(accent.gradient))
            .clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(accent.iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 18.dp, y = (-18).dp)
                .size(110.dp)
                .alpha(0.18f)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(accent.kickerRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(accent.labelRes),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(accent.blurbRes),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

/** Small tile for a deal that is not currently promoted. */
@Composable
internal fun DealCompactCard(
    accent: DealAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 86.dp)
            .clip(dealCompactShape)
            .background(accent.tone.copy(alpha = 0.14f))
            .border(1.dp, accent.tone.copy(alpha = 0.45f), dealCompactShape)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 14.dp)
    ) {
        Icon(
            painter = painterResource(accent.iconRes),
            contentDescription = null,
            tint = accent.tone,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(accent.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "DealCards – Light", showBackground = true, widthDp = 360, heightDp = 340)
@Composable
private fun DealCardsLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(Color(0xFFFFF5E6))
                .padding(16.dp)
        ) {
            DealHeroCard(accent = truthAccent, onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DealCompactCard(
                    accent = GameDealType.GENERAL_KNOWLEDGE.accent,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                DealCompactCard(
                    accent = GameDealType.STICKY_DARE.accent,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(name = "DealCards – Dark", showBackground = true, widthDp = 360, heightDp = 340)
@Composable
private fun DealCardsDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(Color(0xFF0B1F24))
                .padding(16.dp)
        ) {
            DealHeroCard(accent = dareAccent, onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DealCompactCard(
                    accent = GameDealType.MINI_GAME.accent,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                DealCompactCard(
                    accent = GameDealType.STICKY_DARE.accent,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
