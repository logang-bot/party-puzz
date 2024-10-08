package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
fun PlayerDataCard(
    player: Player, modifier: Modifier = Modifier
) {
    var playerName by remember { mutableStateOf("") }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ), modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp)
        ) {
            Box {
                PlayerAvatar(R.drawable.img_dummy_avatar)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .align(Alignment.BottomEnd)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_male),
                        contentDescription = stringResource(
                            id = R.string.player_avatar
                        ),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiary),
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }
            Text(
                text = "John Doefdasfadsfsa",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlayerAvatar(
    @DrawableRes imageId: Int
) {
    Box(modifier = Modifier.padding(end = 5.dp)) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = stringResource(
                id = R.string.player_avatar
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(68.dp)
                .height(68.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun AddPlayerCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp)
        ) {
            Box {
                PlayerAvatar(R.drawable.ic_plus)
            }
            Text(
                text = stringResource(id = R.string.add_player),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(widthDp = 90)
@Composable
fun PlayerDataCardPreview() {
    PartyPuzzTheme {
        PlayerDataCard(player = Player.getEmptyPlayer())
    }
}

@Preview(widthDp = 90)
@Composable
fun AddPlayerCardPreview() {
    PartyPuzzTheme {
        AddPlayerCard()
    }
}