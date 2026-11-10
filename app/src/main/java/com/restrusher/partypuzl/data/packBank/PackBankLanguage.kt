package com.restrusher.partypuzl.data.packBank

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * The language premium prompts should be shown in: the app's own language setting when one is
 * picked, the device language otherwise. Matches what `strings.xml` resolves to.
 */
fun currentPackLanguage(): String {
    val locale = AppCompatDelegate.getApplicationLocales()[0]
        ?: LocaleListCompat.getAdjustedDefault()[0]
    return locale?.language ?: PackBankContent.FALLBACK_LANGUAGE
}
