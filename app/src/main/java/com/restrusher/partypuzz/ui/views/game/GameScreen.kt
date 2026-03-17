package com.restrusher.partypuzz.ui.views.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.restrusher.partypuzz.R

@Composable
fun GameScreen(
    setAppBarTitle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(id = R.string.game)
    LaunchedEffect(title) {
        setAppBarTitle(title)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Game screen - Coming soon!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
