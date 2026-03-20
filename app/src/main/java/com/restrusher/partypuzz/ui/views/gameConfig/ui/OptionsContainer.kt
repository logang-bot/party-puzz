package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

private data class OptionDef(@androidx.annotation.StringRes val labelRes: Int, val initialEnabled: Boolean)

private val optionDefinitions = listOf(
    OptionDef(R.string.save_stats, true),
    OptionDef(R.string.bar_mode, false),
    OptionDef(R.string.can_skip_questions, false),
    OptionDef(R.string.unlimited_mode, false),
    OptionDef(R.string.mini_games, false),
    OptionDef(R.string.truth_or_dare, false),
    OptionDef(R.string.general_culture, false),
)

@Composable
fun OptionsContainer(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        GameOptionsSource.initialize(
            optionDefinitions.map {
                GameOptionsSource.GameOption(labelRes = it.labelRes, enabled = it.initialEnabled)
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp)
        ) {
            optionDefinitions.forEach { def ->
                OptionChip(
                    optionName = stringResource(def.labelRes),
                    initialEnabled = def.initialEnabled,
                    onToggled = { GameOptionsSource.toggle(def.labelRes) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val indicatorAlpha by animateFloatAsState(
            targetValue = if (scrollState.isScrollInProgress) 1f else 0f,
            animationSpec = tween(300),
            label = "indicator alpha"
        )
        ScrollIndicator(
            scrollValue = scrollState.value,
            scrollMaxValue = scrollState.maxValue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .graphicsLayer { alpha = indicatorAlpha }
        )
    }
}

@Composable
private fun ScrollIndicator(
    scrollValue: Int,
    scrollMaxValue: Int,
    modifier: Modifier = Modifier
) {
    if (scrollMaxValue == 0) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier.height(3.dp)) {
        val trackWidth = size.width
        val totalContentWidth = trackWidth + scrollMaxValue
        val thumbWidth = trackWidth * (trackWidth / totalContentWidth)
        val thumbOffset = (trackWidth - thumbWidth) * (scrollValue.toFloat() / scrollMaxValue)

        drawRoundRect(color = trackColor, cornerRadius = CornerRadius(4f))
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(thumbOffset, 0f),
            size = Size(thumbWidth, size.height),
            cornerRadius = CornerRadius(4f)
        )
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
        OptionChip(optionName = stringResource(id = R.string.bar_mode), initialEnabled = false)
    }
}
