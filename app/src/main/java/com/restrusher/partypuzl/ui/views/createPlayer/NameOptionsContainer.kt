package com.restrusher.partypuzl.ui.views.createPlayer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R

private val FieldShape = RoundedCornerShape(16.dp)

@Composable
fun NameOptionsContainer(
    value: String,
    onValueChanged: (String) -> Unit,
    onGenerateRandomName: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    // The field used to be fully transparent with a border that only appeared on focus, so at
    // rest it read as a floating hairline. It now always sits on a filled surface with a soft
    // outline — the design's `rgba(255,255,255,0.04)` + 1 px line — and the outline simply
    // brightens to the brand colour on focus.
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = 180),
        label = "name field border"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(FieldShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
            .border(width = 1.dp, color = borderColor, shape = FieldShape)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChanged,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.players_name),
                    modifier = Modifier.alpha(0.4f)
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused }
        )

        // Hairline divider so the shuffle button reads as part of the field, not floating in it.
        Box(
            modifier = Modifier
                .height(28.dp)
                .width(1.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .clickable(onClick = onGenerateRandomName)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_random),
                contentDescription = stringResource(R.string.generate_random_name),
                colorFilter = ColorFilter.tint(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
