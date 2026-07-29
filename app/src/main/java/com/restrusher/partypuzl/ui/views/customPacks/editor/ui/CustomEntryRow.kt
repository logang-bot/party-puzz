package com.restrusher.partypuzl.ui.views.customPacks.editor.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.ui.theme.AccentLime
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appCard
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.AccentIconTile
import com.restrusher.partypuzl.ui.views.customPacks.ui.RowActionButton

/** One authored entry in the pack editor, showing whatever its type actually carries. */
@Composable
internal fun CustomEntryRow(
    entry: CustomEntryEntity,
    position: Int,
    durationLabel: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = entry.type.accent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appCard(RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AccentIconTile(accent = accent, iconRes = entry.type.iconRes, size = 26)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(entry.type.labelRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            if (entry.type == CustomEntryType.STICKY_DARE && durationLabel != null) {
                Text(
                    text = " · $durationLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = position.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
            )
        }

        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = entry.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (entry.type == CustomEntryType.TRIVIA) {
            Spacer(modifier = Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TriviaOption(label = entry.optionA, isCorrect = entry.correctOption == "A")
                TriviaOption(label = entry.optionB, isCorrect = entry.correctOption == "B")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RowActionButton(
                label = stringResource(R.string.custom_pack_edit),
                iconRes = R.drawable.ic_outline_edit,
                onClick = onEdit
            )
            RowActionButton(
                label = stringResource(R.string.custom_pack_delete),
                iconRes = R.drawable.ic_delete,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun TriviaOption(label: String?, isCorrect: Boolean, modifier: Modifier = Modifier) {
    if (label == null) return
    val tone = if (isCorrect) AccentLime else MaterialTheme.colorScheme.onBackground
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(CircleShape)
            .background(tone.copy(alpha = if (isCorrect) 0.16f else 0.05f))
            .border(
                width = 1.dp,
                color = tone.copy(alpha = if (isCorrect) 0.45f else 0.12f),
                shape = CircleShape
            )
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        if (isCorrect) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = AccentLime,
                modifier = Modifier.size(10.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCorrect) AccentLime
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}

private val previewTrivia = CustomEntryEntity(
    id = "preview",
    packId = "pack",
    type = CustomEntryType.TRIVIA,
    text = "Which country drinks the most wine per capita?",
    durationSeconds = null,
    optionA = "Portugal",
    optionB = "France",
    correctOption = "A"
)

@Preview(showBackground = true)
@Composable
private fun CustomEntryRowPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            CustomEntryRow(previewTrivia, 1, null, {}, {})
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomEntryRowDarkPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            CustomEntryRow(previewTrivia, 1, null, {}, {})
        }
    }
}
