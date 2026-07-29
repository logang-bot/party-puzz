package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.stickyDurationLabel

/**
 * "How it will play" — the entry rendered roughly as the room will see it, updating as the author
 * types. The point is to make the length and tone of a prompt obvious before it is saved.
 */
@Composable
internal fun EntryPreviewCard(state: CreateCustomEntryState, modifier: Modifier = Modifier) {
    val accent = state.type.accent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(state.type.iconRes),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(state.type.labelRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        val text = state.text.trim()
        Text(
            text = if (text.isNotEmpty()) {
                stringResource(R.string.custom_entry_preview_quoted, text)
            } else {
                stringResource(R.string.custom_entry_preview_placeholder)
            },
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = if (text.isNotEmpty()) 1f else 0.45f
            )
        )

        when (state.type) {
            CustomEntryType.STICKY_DARE -> {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        R.string.custom_entry_preview_clock,
                        stickyDurationLabel(state.durationSeconds)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f))
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }

            CustomEntryType.TRIVIA -> {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PreviewOption(state.optionA, stringResource(R.string.custom_entry_option_a))
                    PreviewOption(state.optionB, stringResource(R.string.custom_entry_option_b))
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun PreviewOption(value: String, placeholder: String, modifier: Modifier = Modifier) {
    val filled = value.isNotBlank()
    Text(
        text = if (filled) value else placeholder,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (filled) 1f else 0.4f),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

private val previewState = CreateCustomEntryState(
    type = CustomEntryType.STICKY_DARE,
    text = "End every sentence with “…allegedly.”",
    durationSeconds = 300
)

@Preview(showBackground = true)
@Composable
private fun EntryPreviewCardPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            EntryPreviewCard(previewState)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EntryPreviewCardDarkPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            EntryPreviewCard(previewState)
        }
    }
}
