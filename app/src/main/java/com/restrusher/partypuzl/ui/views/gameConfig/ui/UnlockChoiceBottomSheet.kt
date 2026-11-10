package com.restrusher.partypuzl.ui.views.gameConfig.ui

import android.app.Activity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.common.purchase.PremiumPurchaseOption
import com.restrusher.partypuzl.ui.common.purchase.PremiumSheet
import com.restrusher.partypuzl.ui.common.purchase.UnlockOption
import com.restrusher.partypuzl.ui.theme.BrandTeal
import com.restrusher.partypuzl.ui.theme.BrandTealShade
import com.restrusher.partypuzl.ui.views.gameConfig.PackUiModel
import com.restrusher.partypuzl.ui.views.gameConfig.text

/**
 * Shown when a locked premium pack is tapped. Per the design there are two ways out: watch a
 * rewarded ad to unlock this pack for the session, or buy premium to unlock everything for good.
 *
 * The ad option only exists when [showAdOption] — a build with no ad provider offers the purchase
 * alone. [isAdReady] is false while the rewarded ad is still loading; the option then stays
 * visible but disabled, which reads better than having it pop in.
 */
@Composable
internal fun UnlockChoiceBottomSheet(
    pack: PackUiModel,
    showAdOption: Boolean,
    isAdReady: Boolean,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit,
    activity: Activity?
) {
    val subtitle = if (showAdOption) R.string.unlock_sheet_subtitle
    else R.string.unlock_sheet_subtitle_purchase_only

    PremiumSheet(
        title = stringResource(R.string.unlock_sheet_title, pack.name.text()),
        subtitle = stringResource(subtitle),
        onDismiss = onDismiss
    ) {
        if (showAdOption) {
            WatchAdOption(isAdReady = isAdReady, onWatchAd = onWatchAd)
            Spacer(modifier = Modifier.height(10.dp))
        }
        PremiumPurchaseOption(activity = activity, onStarted = onDismiss)
    }
}

@Composable
private fun WatchAdOption(isAdReady: Boolean, onWatchAd: () -> Unit) {
    UnlockOption(
        iconRes = R.drawable.ic_play_arrow,
        gradient = listOf(BrandTeal, BrandTealShade),
        title = stringResource(R.string.unlock_option_ad_title),
        subtitle = stringResource(
            if (isAdReady) R.string.unlock_option_ad_subtitle
            else R.string.unlock_option_ad_loading
        ),
        enabled = isAdReady,
        onClick = onWatchAd
    )
}
