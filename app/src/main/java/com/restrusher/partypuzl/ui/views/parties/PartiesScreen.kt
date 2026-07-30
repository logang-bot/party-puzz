package com.restrusher.partypuzl.ui.views.parties

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.common.AdBannerView
import com.restrusher.partypuzl.ui.common.AdUnitIds
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink

@Composable
fun PartiesScreen(
    setAppBarTitle: (String) -> Unit,
    onPartyClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PartiesViewModel = hiltViewModel()
) {
    val title = stringResource(id = R.string.parties)
    LaunchedEffect(Unit) { setAppBarTitle(title) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.parties.isEmpty() -> Text(
                text = stringResource(id = R.string.no_parties_yet),
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = MaterialTheme.appColors.brandAccent.ink(Ink.Prominent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center)
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 50.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(
                            R.string.parties_summary,
                            uiState.parties.size,
                            uiState.totalPhotoCount
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(items = uiState.parties, key = { it.party.id }) { partyWithPlayers ->
                    PartyCard(
                        party = partyWithPlayers,
                        onClick = { onPartyClick(partyWithPlayers.party.id) }
                    )
                }
            }
        }

        AdBannerView(
            adUnitId = AdUnitIds.PARTIES_BANNER,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
