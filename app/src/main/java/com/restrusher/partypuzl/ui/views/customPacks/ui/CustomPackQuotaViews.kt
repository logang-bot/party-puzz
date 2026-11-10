package com.restrusher.partypuzl.ui.views.customPacks.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.domain.customPacks.CustomPackQuota.Companion.MAX_FREE_ENTRIES
import com.restrusher.partypuzl.domain.customPacks.CustomPackQuota.Companion.MAX_FREE_PACKS
import com.restrusher.partypuzl.ui.common.purchase.PremiumUpsellSheet
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.wash

/** The free-version usage line. Tapping it opens the same upsell the cap does. */
@Composable
internal fun FreeQuotaHint(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.badgePremium.wash(Wash.Soft))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = MaterialTheme.appColors.badgePremium,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.appColors.badgePremium
        )
    }
}

@Composable
internal fun CustomPacksUpsellSheet(activity: Activity?, onDismiss: () -> Unit) {
    PremiumUpsellSheet(
        title = stringResource(R.string.custom_packs_upsell_title),
        message = pluralStringResource(
            R.plurals.custom_packs_upsell_message,
            MAX_FREE_PACKS,
            MAX_FREE_PACKS
        ),
        activity = activity,
        onDismiss = onDismiss
    )
}

@Composable
internal fun CustomEntriesUpsellSheet(activity: Activity?, onDismiss: () -> Unit) {
    PremiumUpsellSheet(
        title = stringResource(R.string.custom_pack_entries_upsell_title),
        message = pluralStringResource(
            R.plurals.custom_pack_entries_upsell_message,
            MAX_FREE_ENTRIES,
            MAX_FREE_ENTRIES
        ),
        activity = activity,
        onDismiss = onDismiss
    )
}

@Preview(name = "FreeQuotaHint – Light", showBackground = true, widthDp = 360)
@Composable
private fun FreeQuotaHintPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        FreeQuotaHint(text = "Free version · 1 of 1 packs used", onClick = {})
    }
}

@Preview(name = "FreeQuotaHint – Dark", showBackground = true, widthDp = 360)
@Composable
private fun FreeQuotaHintDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        FreeQuotaHint(text = "Free version · 1 of 1 packs used", onClick = {})
    }
}
