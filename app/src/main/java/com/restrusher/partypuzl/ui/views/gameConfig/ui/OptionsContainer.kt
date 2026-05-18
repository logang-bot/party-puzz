package com.restrusher.partypuzl.ui.views.gameConfig.ui

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.appData.appDataSource.GameOptionsSource
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme

private data class OptionDef(@StringRes val labelRes: Int, val initialEnabled: Boolean)

private val optionDefinitions = listOf(
    OptionDef(R.string.truth_or_dare, true),
    OptionDef(R.string.general_knowledge_title, true),
    OptionDef(R.string.sticky_dares, true),
    OptionDef(R.string.mini_games, false),
)

@Composable
fun OptionsContainer(modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        GameOptionsSource.initialize(
            optionDefinitions.map {
                GameOptionsSource.GameOption(labelRes = it.labelRes, enabled = it.initialEnabled)
            }
        )
    }

    val enabledCount = GameOptionsSource.options.count { it.enabled }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        OptionsHeader(enabledCount = enabledCount, totalCount = optionDefinitions.size)
        Text(
            text = stringResource(R.string.options_toggle_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        OptionsGrid()
    }
}

@Composable
private fun OptionsHeader(enabledCount: Int, totalCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        GameConfigSectionLabel(
            text = stringResource(R.string.pick_what_to_play),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.categories_count_on, enabledCount, totalCount),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun OptionsGrid() {
    val rows = optionDefinitions.chunked(2)
    rows.forEachIndexed { index, pair ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            pair.forEach { def ->
                if (def.labelRes == R.string.mini_games) {
                    MiniGamesOptionCard(
                        optionName = stringResource(def.labelRes),
                        initialEnabled = def.initialEnabled,
                        onToggled = { GameOptionsSource.toggle(def.labelRes) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    OptionCard(
                        optionName = stringResource(def.labelRes),
                        initialEnabled = def.initialEnabled,
                        onToggled = { GameOptionsSource.toggle(def.labelRes) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
        if (index < rows.lastIndex) Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun OptionCard(
    optionName: String,
    modifier: Modifier = Modifier,
    initialEnabled: Boolean = false,
    onToggled: () -> Unit = {}
) {
    var selected by remember { mutableStateOf(initialEnabled) }
    val interactionSource = remember { MutableInteractionSource() }
    val cardShape = RoundedCornerShape(12.dp)

    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(250),
        label = "card border"
    )
    val checkBgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(250),
        label = "check bg"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, borderColor, cardShape)
            .clickable(interactionSource = interactionSource, indication = null) {
                selected = !selected
                onToggled()
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        OptionCardContent(
            optionName = optionName,
            selected = selected,
            checkBgColor = checkBgColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun OptionCardContent(
    optionName: String,
    selected: Boolean,
    checkBgColor: Color,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = optionName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (selected) stringResource(R.string.included).uppercase()
                else stringResource(R.string.tap_to_add).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        OptionToggleCircle(selected = selected, checkBgColor = checkBgColor)
    }
}

@Composable
private fun OptionToggleCircle(selected: Boolean, checkBgColor: Color) {
    val circleColor by animateColorAsState(
        targetValue = if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(250),
        label = "circle border"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(checkBgColor)
            .border(1.5.dp, circleColor, CircleShape)
    ) {
        if (selected) {
            Image(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OptionsContainerPreview() {
    PartyPuzlTheme {
        OptionsContainer()
    }
}

@Preview(showBackground = true)
@Composable
fun OptionCardPreview() {
    PartyPuzlTheme {
        OptionCard(optionName = stringResource(id = R.string.truth_or_dare), initialEnabled = true)
    }
}
