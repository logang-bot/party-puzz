package com.restrusher.partypuzz.ui.views.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
fun LasPartyCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            .fillMaxWidth()
    ) {
        val names = listOf("Laura", "John", "Clara", "Chris")
        val joinedNames = names.joinToString(", ")
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 10.dp)) {
                Spacer(modifier = Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy((-15).dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.img_dummy_avatar),
                                contentDescription = stringResource(
                                    id = R.string.player_avatar
                                ),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(50.dp)
                                    .clip(CircleShape)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.img_dummy_avatar),
                                contentDescription = stringResource(
                                    id = R.string.player_avatar
                                ),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(50.dp)
                                    .clip(CircleShape)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.img_dummy_avatar),
                                contentDescription = stringResource(
                                    id = R.string.player_avatar
                                ),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(50.dp)
                                    .clip(CircleShape)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.img_dummy_avatar),
                                contentDescription = stringResource(
                                    id = R.string.player_avatar
                                ),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(50.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$joinedNames and 2 more",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraLight
                        )
                    }
                    Button(
                        onClick = { /*TODO*/ },
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

@Composable
@Preview(showBackground = true)
fun LastPartyCardPreview() {
    PartyPuzzTheme {
        LasPartyCard(modifier = Modifier.fillMaxWidth())
    }
}