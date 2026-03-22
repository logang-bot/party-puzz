package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.restrusher.partypuzz.data.local.appData.appDataSource.GameOptionsSource
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
import androidx.compose.foundation.layout.Row

private data class OptionDef(@androidx.annotation.StringRes val labelRes: Int, val initialEnabled: Boolean)

private val optionDefinitions = listOf(
    OptionDef(R.string.truth_or_dare, true),
    OptionDef(R.string.general_knowledge_title, true),
    OptionDef(R.string.sticky_dares, true),
    OptionDef(R.string.mini_games, true),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionsContainer(modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        GameOptionsSource.initialize(
            optionDefinitions.map {
                GameOptionsSource.GameOption(labelRes = it.labelRes, enabled = it.initialEnabled)
            }
        )
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        optionDefinitions.forEach { def ->
            OptionChip(
                optionName = stringResource(def.labelRes),
                initialEnabled = def.initialEnabled,
                onToggled = { GameOptionsSource.toggle(def.labelRes) }
            )
        }
    }
}

@Composable
fun OptionChip(
    optionName: String, initialEnabled: Boolean = false, onToggled: () -> Unit = {}, modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(initialEnabled) }
    val interactionSource = remember { MutableInteractionSource() }
    val chipShape = RoundedCornerShape(20.dp)

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(250),
        label = "chip bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(250),
        label = "chip text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Color.Transparent else MaterialTheme.colorScheme.primary,
        animationSpec = tween(250),
        label = "chip border"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .clip(chipShape)
            .border(1.dp, borderColor, chipShape)
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null) {
                selected = !selected
                onToggled()
            }
            .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
        if (selected) {
            Image(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = stringResource(id = R.string.option_description),
                colorFilter = ColorFilter.tint(textColor),
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 2.dp)
            )
        }
        Text(text = optionName, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

@Preview(showBackground = true)
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
        OptionChip(optionName = stringResource(id = R.string.truth_or_dare), initialEnabled = true)
    }
}
