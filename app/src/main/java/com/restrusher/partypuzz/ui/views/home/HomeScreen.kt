package com.restrusher.partypuzz.ui.views.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.appDataSource.GameModesDatasource
import com.restrusher.partypuzz.data.appModels.GameMode

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primary
                )
            )
        )
    ) {
//        Image(
//            painter = painterResource(id = R.drawable.img_light_background),
//            contentDescription = stringResource(
//                id = R.string.app_name
//            ),
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.fillMaxSize()
//        )
        Column {
            Box(
                modifier = Modifier.padding(
                    top = 15.dp,
                    bottom = 30.dp,
                    start = 15.dp,
                    end = 15.dp
                )
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.welcome),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Text(
                        text = stringResource(id = R.string.glad_to_have_you_back, "John"),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

            }
            Row(modifier = Modifier.fillMaxWidth().padding(5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                val gamesModes = GameModesDatasource.gameModesList
                Column(modifier = modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    GameModeCard(gamesModes.elementAt(0), modifier = modifier.weight(1.2f))
                    GameModeCard(gamesModes.elementAt(1), modifier = modifier.weight(0.8f))
                }
                Column(modifier = modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    GameModeCard(gamesModes.elementAt(2), modifier = modifier.weight(0.8f))
                    GameModeCard(gamesModes.elementAt(3), modifier = modifier.weight(1.2f))
                }
            }
        }
    }
}

@Composable
fun GameModeCard(
    gameMode: GameMode, modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .clickable { }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(3.dp)) {
            Image(
                painter = painterResource(id = gameMode.imageId),
                contentDescription = stringResource(id = gameMode.description),
                contentScale = ContentScale.Crop
            )
            Text(
                text = stringResource(id = gameMode.name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(id = gameMode.description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}