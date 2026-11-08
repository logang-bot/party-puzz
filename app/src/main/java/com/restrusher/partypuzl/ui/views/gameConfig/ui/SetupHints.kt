package com.restrusher.partypuzl.ui.views.gameConfig.ui

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.entities.GameMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.ink

/** One-line tip on the setup screen: a tinted icon tile followed by a short sentence. */
@Composable
internal fun SetupHint(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
        )
    }
}

/**
 * Responsible-play reminder, rendered only for modes that deal sips (see [GameMode.dealsSips]) so
 * it sits where the drinking starts. Renders nothing for the other modes.
 */
@Composable
internal fun ResponsibleDrinkingHint(@StringRes gameModeName: Int, modifier: Modifier = Modifier) {
    if (GameMode.fromNameRes(gameModeName)?.dealsSips != true) return
    SetupHint(
        iconRes = R.drawable.ic_info,
        textRes = R.string.responsible_drinking_hint,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun ResponsibleDrinkingHintPreview() {
    PartyPuzlTheme {
        ResponsibleDrinkingHint(gameModeName = R.string.bar_game_mode)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResponsibleDrinkingHintDarkPreview() {
    PartyPuzlTheme {
        ResponsibleDrinkingHint(gameModeName = R.string.bar_game_mode)
    }
}
