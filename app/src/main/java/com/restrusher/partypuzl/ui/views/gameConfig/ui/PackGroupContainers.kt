package com.restrusher.partypuzl.ui.views.gameConfig.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.ui.theme.appCard

/** Chrome the three pack groups share — the tier label and the rounded card the rows sit in. */

/** Tier heading above a group — "OFFICIAL", "PREMIUM", "CUSTOM · 1/2". */
@Composable
internal fun PackGroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
        modifier = modifier.padding(top = 18.dp, bottom = 2.dp)
    )
}

/** Rounded container the rows share, so dividers read as one grouped card. */
@Composable
internal fun PackGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .appCard(RoundedCornerShape(16.dp))
    ) {
        content()
    }
}
