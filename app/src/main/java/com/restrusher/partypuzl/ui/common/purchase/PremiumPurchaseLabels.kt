package com.restrusher.partypuzl.ui.common.purchase

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.restrusher.partypuzl.R

/** "€2.99 one-time · …" once Play has returned the price, the same line without it until then. */
@Composable
fun premiumPurchaseSubtitle(price: String?): String =
    if (price == null) stringResource(R.string.premium_purchase_subtitle)
    else stringResource(R.string.premium_purchase_subtitle_priced, price)
