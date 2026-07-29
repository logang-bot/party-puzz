package com.restrusher.partypuzl.ui.common

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.AccentCoral
import com.restrusher.partypuzl.ui.theme.appColors

class RewardedAdState(private val context: Context) {
    var rewardedAd by mutableStateOf<RewardedAd?>(null)
        private set

    val isReady get() = rewardedAd != null

    fun load(adUnitId: String) {
        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { rewardedAd = null }
            }
        )
    }

    fun show(activity: Activity, onRewarded: () -> Unit = {}) {
        rewardedAd?.let { ad ->
            ad.show(activity) { onRewarded() }
            rewardedAd = null
        }
    }
}

@Composable
fun rememberRewardedAd(adUnitId: String): RewardedAdState {
    val context = LocalContext.current
    val state = remember { RewardedAdState(context) }
    LaunchedEffect(adUnitId) { state.load(adUnitId) }
    return state
}

@Composable
fun RewardedAdCard(
    rewardedAdState: RewardedAdState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(AccentCoral, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_whatshot),
                    contentDescription = null,
                    tint = MaterialTheme.appColors.onAccentSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.rewarded_ad_title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.rewarded_ad_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = { (context as? Activity)?.let { rewardedAdState.show(it) } },
                enabled = rewardedAdState.isReady,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.watch).uppercase(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
