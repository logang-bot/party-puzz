package com.restrusher.partypuzz.ui.views.gameLoading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.pm.ActivityInfo
import androidx.compose.ui.res.stringArrayResource
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.loadingAnimations.BlurredAnimatedText
import com.restrusher.partypuzz.ui.common.loadingAnimations.TripleOrbitLoadingAnimation
import kotlinx.coroutines.delay

private const val LOADING_DELAY_MS = 8000L
private const val TEXT_REFRESH_INTERVAL_MS = 2000L

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val loadingTexts = stringArrayResource(id = R.array.loading_texts)
    var currentTextIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(LOADING_DELAY_MS)
        onLoadingComplete()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(TEXT_REFRESH_INTERVAL_MS)
            currentTextIndex = (currentTextIndex + 1) % loadingTexts.size
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TripleOrbitLoadingAnimation(
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        BlurredAnimatedText(
            text = loadingTexts[currentTextIndex]
        )
    }
}
