package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionsContainer(modifier: Modifier = Modifier) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = modifier) {
        OptionChip(stringResource(id = R.string.bar_mode), true)
        OptionChip(stringResource(id = R.string.can_skip_questions), false)
        OptionChip(stringResource(id = R.string.unlimited_mode), true)
    }
}

@Composable
fun OptionChip(
    optionName: String, isEnabled: Boolean, modifier: Modifier = Modifier
) {
    val iconId = if (isEnabled) R.drawable.ic_check else R.drawable.ic_close
    val backgroundColor = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(backgroundColor)
            .padding(vertical = 2.dp, horizontal = 6.dp)
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = stringResource(id = R.string.option_description),
            colorFilter = ColorFilter.tint(textColor),
            modifier = Modifier.size(16.dp).padding(end = 2.dp)
        )
        Text(text = optionName, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

@Preview
@Composable
fun OptionsContainerPreview() {
    PartyPuzzTheme {
        OptionsContainer()
    }
}

@Preview
@Composable
fun OptionChipPreview() {
    PartyPuzzTheme {
        OptionChip(optionName = stringResource(id = R.string.bar_mode), isEnabled = false)
    }
}