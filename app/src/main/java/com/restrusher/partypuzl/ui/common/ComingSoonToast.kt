package com.restrusher.partypuzl.ui.common

import android.content.Context
import android.widget.Toast
import com.restrusher.partypuzl.R

/**
 * Stands in for every purchase / unlock action while [FeatureFlags.PACK_UNLOCK_COMING_SOON] is
 * on. Delete with the flag.
 */
internal fun Context.showComingSoon() {
    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
}
