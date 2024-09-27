package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R

@Composable
fun OptionsContainer(modifier: Modifier = Modifier) {
    Column(modifier) {
        OptionCard(stringResource(id = R.string.bar_mode))
        OptionCard(stringResource(id = R.string.can_skip_questions))
        OptionCard(stringResource(id = R.string.unlimited_mode))
    }
}

@Composable
fun OptionCard(
    optionName: String,
    modifier: Modifier = Modifier
) {
    var isChecked by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(text = optionName, style = MaterialTheme.typography.labelLarge,  modifier = Modifier.weight(1f))
        Switch(checked = isChecked, onCheckedChange = { isChecked = it })
    }
}