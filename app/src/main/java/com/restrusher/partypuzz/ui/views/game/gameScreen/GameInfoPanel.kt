package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GameOptionsSource

@Composable
internal fun GameInfoPanel(modifier: Modifier = Modifier) {
    val options = GameOptionsSource.options

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .width(220.dp)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.game_configuration),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    if (option.enabled) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Spacer(Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (option.enabled)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}
