package com.restrusher.partypuzl.screenshots

import androidx.test.platform.app.InstrumentationRegistry
import com.restrusher.partypuzl.data.preferences.ThemeMode

/** One store-listing variant: the app language and theme a tour run captures. */
data class TourArgs(val languageTag: String, val themeMode: ThemeMode) {

    /** `en-US__light` — the folder pair `collectStoreScreenshots` files each shot under. */
    val outputPrefix: String
        get() = "${storeLocale()}__${themeMode.name.lowercase()}"

    override fun toString(): String {
        return outputPrefix
    }

    private fun storeLocale(): String {
        return if (languageTag.startsWith("es")) "es-ES" else "en-US"
    }

    companion object {
        private val Languages = listOf("en", "es")
        private val Themes = listOf(ThemeMode.LIGHT, ThemeMode.DARK)

        fun variantsToRun(): List<TourArgs> {
            val themes = requestedThemes()
            return requestedLanguages().flatMap { language -> themes.map { TourArgs(language, it) } }
        }

        private fun requestedLanguages(): List<String> =
            instrumentationArgument("locale")?.let(::listOf) ?: Languages

        private fun requestedThemes(): List<ThemeMode> =
            instrumentationArgument("theme")?.let { listOf(ThemeMode.valueOf(it.uppercase())) } ?: Themes

        private fun instrumentationArgument(key: String): String? =
            InstrumentationRegistry.getArguments().getString(key)
    }
}
