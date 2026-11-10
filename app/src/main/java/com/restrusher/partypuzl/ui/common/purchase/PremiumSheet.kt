package com.restrusher.partypuzl.ui.common.purchase

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.AccentCoral
import com.restrusher.partypuzl.ui.theme.AccentYellow
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink

/**
 * The frame every premium prompt shares: a "Premium" kicker, a title and subtitle, the unlock
 * options in [options], and "Not now".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PremiumSheet(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    options: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            PremiumSheetHeader(title = title, subtitle = subtitle)
            Spacer(modifier = Modifier.height(18.dp))
            options()
            Spacer(modifier = Modifier.height(14.dp))
            NotNowButton(onClick = onDismiss)
        }
    }
}

/** The one-time purchase row. Starting the purchase also closes the sheet via [onStarted]. */
@Composable
internal fun PremiumPurchaseOption(
    activity: Activity?,
    onStarted: () -> Unit,
    viewModel: PremiumPurchaseViewModel = hiltViewModel()
) {
    val price by viewModel.formattedPrice.collectAsStateWithLifecycle()
    UnlockOption(
        iconRes = R.drawable.ic_partypuzz,
        gradient = listOf(AccentYellow, AccentCoral),
        title = stringResource(R.string.unlock_option_purchase_title),
        subtitle = premiumPurchaseSubtitle(price),
        highlighted = true,
        enabled = activity != null,
        onClick = {
            activity?.let(viewModel::purchase)
            onStarted()
        }
    )
}

@Composable
private fun PremiumSheetHeader(title: String, subtitle: String) {
    Text(
        text = stringResource(R.string.unlock_sheet_kicker).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 2.sp,
        color = MaterialTheme.appColors.badgePremium
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.ink(Ink.Standard)
    )
}

@Composable
private fun NotNowButton(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.unlock_not_now).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onSurface.ink(Ink.Secondary),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center
    )
}
