package com.restrusher.partypuzl.ui.common.purchase

import android.app.Activity
import androidx.compose.runtime.Composable

/** A premium prompt whose only way forward is the one-time purchase. */
@Composable
fun PremiumUpsellSheet(
    title: String,
    message: String,
    activity: Activity?,
    onDismiss: () -> Unit
) {
    PremiumSheet(title = title, subtitle = message, onDismiss = onDismiss) {
        PremiumPurchaseOption(activity = activity, onStarted = onDismiss)
    }
}
