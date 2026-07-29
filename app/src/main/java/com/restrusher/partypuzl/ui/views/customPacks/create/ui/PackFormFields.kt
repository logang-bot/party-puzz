package com.restrusher.partypuzl.ui.views.customPacks.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.PACK_DESCRIPTION_MAX
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.ui.views.customPacks.model.AuthorableCategories
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes

/** The four inputs the create-pack form is built from. */

/**
 * Bordered text field matching `NameOptionsContainer` on the create-player screen: the box draws
 * the border, so the field's own indicators are switched off.
 */
@Composable
internal fun PackTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Int = 56,
    /** Off when the caller already draws a border around the field, as the trivia options do. */
    bordered: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (bordered) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                )
            },
            singleLine = singleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp)
        )
    }
}

/** Selectable pills for the pack's category. Mini-games are absent — they are code, not prompts. */
@Composable
internal fun CategoryPills(
    selected: PackCategory,
    onSelect: (PackCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        AuthorableCategories.forEach { category ->
            val isOn = category == selected
            val accent = MaterialTheme.colorScheme.primary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (isOn) accent.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isOn) accent
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .clickable { onSelect(category) }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                if (isOn) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = stringResource(category.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = if (isOn) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/** Segmented mild / medium / spicy control. The pick drives the pack's icon and accent. */
@Composable
internal fun SpiceSelector(
    selected: SpiceLevel,
    onSelect: (SpiceLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            .padding(4.dp)
    ) {
        SpiceLevel.entries.forEach { level ->
            val isOn = level == selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isOn) level.accent else Color.Transparent)
                    .clickable { onSelect(level) }
                    .padding(vertical = 10.dp)
            ) {
                Icon(
                    painter = painterResource(level.iconRes),
                    contentDescription = null,
                    tint = if (isOn) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(level.labelRes).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOn) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/** Right-aligned "n/max" counter shown under a length-capped field. */
@Composable
internal fun CharacterCounter(
    length: Int,
    modifier: Modifier = Modifier,
    max: Int = PACK_DESCRIPTION_MAX
) {
    Text(
        text = stringResource(R.string.custom_pack_counter, length, max),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.End,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth().padding(top = 6.dp)
    )
}
