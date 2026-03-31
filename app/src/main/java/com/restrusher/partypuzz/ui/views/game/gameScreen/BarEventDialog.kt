package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R

@Composable
internal fun BarEventDialog(
    event: BarEvent,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Card spin-in on enter
    var appeared by remember { mutableStateOf(false) }
    val cardRotation by animateFloatAsState(
        targetValue = if (appeared) 0f else 720f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "bar_event_card_rotation"
    )
    LaunchedEffect(Unit) { appeared = true }

    // Illustration continuous slow spin
    val infiniteTransition = rememberInfiniteTransition(label = "bar_event_img")
    val imgRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing)
        ),
        label = "img_spin"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .graphicsLayer { rotationZ = cardRotation }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.bar_event_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = painterResource(R.drawable.img_bar_mode_illustration),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .graphicsLayer { rotationZ = imgRotation }
                )
                Spacer(Modifier.height(20.dp))
                when (event) {
                    is BarEvent.NoAction -> NoActionContent(onDismiss = onDismiss)
                    is BarEvent.GiveDrinks -> GiveDrinksContent(
                        amount = event.amount,
                        targetPlayerName = event.targetPlayerName,
                        onDismiss = onDismiss
                    )
                    is BarEvent.TakeDrinks -> TakeDrinksContent(
                        amount = event.amount,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun NoActionContent(onDismiss: () -> Unit) {
    Text(
        text = stringResource(R.string.bar_event_no_action),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.ok))
    }
}

@Composable
private fun TakeDrinksContent(amount: Int, onDismiss: () -> Unit) {
    Text(
        text = stringResource(R.string.bar_event_take_drinks, amount),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.ok))
    }
}

@Composable
private fun GiveDrinksContent(
    amount: Int,
    targetPlayerName: String,
    onDismiss: () -> Unit
) {
    Text(
        text = stringResource(R.string.bar_event_give_drinks, amount, targetPlayerName),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.ok))
    }
}
