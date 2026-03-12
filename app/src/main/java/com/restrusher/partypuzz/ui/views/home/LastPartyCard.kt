package com.restrusher.partypuzz.ui.views.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.entities.PartyEntity
import com.restrusher.partypuzz.data.local.entities.PartyWithPlayers
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import java.io.File

private val cardShape = RoundedCornerShape(15.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LastPartyCard(
    party: PartyWithPlayers,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSeeButton: Boolean = true
) {
    val context = LocalContext.current
    val players = party.players
    var showPlayersSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val (displayedNames, remaining) = playerNamesSlice(players)
    val namesText = if (remaining > 0)
        "${displayedNames.joinToString(", ")} ${stringResource(R.string.and_x_more, remaining)}"
    else
        displayedNames.joinToString(", ")

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent,
        animationSpec = tween(durationMillis = 400),
        label = "borderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.03f),
        animationSpec = tween(durationMillis = 400),
        label = "backgroundColor"
    )

    Box(
        modifier = modifier
            .border(width = 1.dp, color = borderColor, shape = cardShape)
            .clip(cardShape)
            .background(backgroundColor)
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy((-15).dp)) {
                        players.take(3).forEach { player ->
                            when {
                                player.photoPath != null -> AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(player.photoPath))
                                        .build(),
                                    contentDescription = stringResource(id = R.string.player_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .clip(CircleShape)
                                )
                                player.avatarName != null -> {
                                    val resId = context.resources.getIdentifier(
                                        player.avatarName, "drawable", context.packageName
                                    )
                                    Image(
                                        painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                                        contentDescription = stringResource(id = R.string.player_avatar),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                else -> Image(
                                    painter = painterResource(id = R.drawable.img_dummy_avatar),
                                    contentDescription = stringResource(id = R.string.player_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = namesText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraLight
                    )
                }
                if (showSeeButton) {
                    Button(
                        onClick = { showPlayersSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(text = stringResource(id = R.string.see), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    if (showPlayersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlayersSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.players),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    players.forEach { player ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when {
                                player.photoPath != null -> AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(player.photoPath))
                                        .build(),
                                    contentDescription = stringResource(id = R.string.player_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .clip(CircleShape)
                                )
                                player.avatarName != null -> {
                                    val resId = context.resources.getIdentifier(
                                        player.avatarName, "drawable", context.packageName
                                    )
                                    Image(
                                        painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                                        contentDescription = stringResource(id = R.string.player_avatar),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                else -> Image(
                                    painter = painterResource(id = R.drawable.img_dummy_avatar),
                                    contentDescription = stringResource(id = R.string.player_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (player.nickName.length > 12) player.nickName.take(12) + "…" else player.nickName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Light,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showPlayersSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.close),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LastPartyCardPreview() {
    PartyPuzzTheme {
        val fakeParty = PartyWithPlayers(
            party = PartyEntity(id = 1, name = "Friday Party"),
            players = listOf(
                PlayerEntity(id = 1, nickName = "Laura", gender = Gender.Female),
                PlayerEntity(id = 2, nickName = "John", gender = Gender.Male),
                PlayerEntity(id = 3, nickName = "Clara", gender = Gender.Female),
                PlayerEntity(id = 4, nickName = "Chris", gender = Gender.Male),
            )
        )
        LastPartyCard(
            party = fakeParty,
            isSelected = true,
            onCardClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
