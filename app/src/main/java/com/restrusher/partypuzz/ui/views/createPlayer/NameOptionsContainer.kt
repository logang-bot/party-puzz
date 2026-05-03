package com.restrusher.partypuzz.ui.views.createPlayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R

@Composable
fun NameOptionsContainer(
    value: String,
    onValueChanged: (String) -> Unit,
    onGenerateRandomName: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChanged,
            placeholder = { Text(text = stringResource(id = R.string.players_name), modifier = Modifier.alpha(0.4f)) },
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clickable(onClick = onGenerateRandomName)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_random),
                contentDescription = stringResource(R.string.generate_random_name),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
