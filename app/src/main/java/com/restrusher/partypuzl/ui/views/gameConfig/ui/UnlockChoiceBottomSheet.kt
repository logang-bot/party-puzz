package com.restrusher.partypuzl.ui.views.gameConfig.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.AccentCoral
import com.restrusher.partypuzl.ui.theme.AccentYellow
import com.restrusher.partypuzl.ui.theme.BrandTeal
import com.restrusher.partypuzl.ui.theme.BrandTealShade
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.views.gameConfig.PackUiModel

/**
 * Shown when a locked premium pack is tapped. Two ways out, per the design: watch a rewarded
 * ad to unlock this pack for the session, or buy the one-time upgrade to unlock everything
 * permanently and drop the ads.
 *
 * [isAdReady] is false while the rewarded ad is still loading — the option stays visible but
 * disabled, which reads better than hiding it and having it pop in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UnlockChoiceBottomSheet(
    pack: PackUiModel,
    isAdReady: Boolean,
    onWatchAd: () -> Unit,
    onPurchase: (Activity) -> Unit,
    onDismiss: () -> Unit,
    activity: Activity?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.unlock_sheet_kicker).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = MaterialTheme.appColors.badgePremium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.unlock_sheet_title, stringResource(pack.nameRes)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.unlock_sheet_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(18.dp))
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

            Spacer(modifier = Modifier.height(10.dp))
            UnlockOption(
                iconRes = R.drawable.ic_partypuzz,
                gradient = listOf(AccentYellow, AccentCoral),
                title = stringResource(R.string.unlock_option_purchase_title),
                subtitle = stringResource(R.string.unlock_option_purchase_subtitle),
                highlighted = true,
                enabled = activity != null,
                onClick = { activity?.let(onPurchase) }
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.unlock_not_now).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UnlockOption(
    iconRes: Int,
    gradient: List<Color>,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val borderColor = if (highlighted) MaterialTheme.appColors.badgePremium.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.16f)
    val containerColor = if (highlighted) MaterialTheme.appColors.badgePremium.copy(alpha = 0.08f)
    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradient))
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.appColors.onAccentSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_keyboard_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}
