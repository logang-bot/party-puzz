package com.restrusher.partypuzl.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.restrusher.partypuzl.ui.theme.appColors

/**
 * Blocking spinner over a dimmed screen, for the moments a screen is saving or
 * deleting and nothing else should be touchable.
 *
 * The dim is warmer and lighter in the light theme — a flat 40% black over cream
 * reads as dirty rather than dimmed.
 */
@Composable
fun LoadingScrim(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.scrim),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
