package com.restrusher.partypuzl.screenshots

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.navigation.HomeNavigation
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import com.restrusher.partypuzl.ui.common.ads.LocalAdsProvider
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Debug-only stand-in for `MainActivity` used by the store-screenshot tour. It seeds demo data,
 * then shows the real nav graph without the responsible-drinking dialogs, and hands the test its
 * NavController so each scene can jump straight to a route.
 */
@AndroidEntryPoint
class ScreenshotHostActivity : AppCompatActivity() {

    @Inject
    lateinit var adsProvider: AdsProvider

    @Inject
    lateinit var seeder: ScreenshotSeeder

    val seededDemo: StateFlow<SeededDemo?> get() = _seededDemo
    private val _seededDemo = MutableStateFlow<SeededDemo?>(null)
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            _seededDemo.value = seeder.seed(DemoContent.forLanguage(languageTag()))
            showNavGraph(themeMode())
        }
    }

    fun navigate(route: Any) {
        checkNotNull(navController) { "Nav graph is not shown yet" }.navigate(route)
    }

    private fun showNavGraph(themeMode: ThemeMode) = setContent {
        val controller = rememberNavController().also { navController = it }
        PartyPuzlTheme(themeMode = themeMode) {
            CompositionLocalProvider(LocalAdsProvider provides adsProvider) {
                HomeNavigation(navController = controller)
            }
        }
    }

    private fun languageTag(): String = intent.getStringExtra(EXTRA_LANGUAGE) ?: "en"

    private fun themeMode(): ThemeMode =
        ThemeMode.valueOf(intent.getStringExtra(EXTRA_THEME) ?: ThemeMode.LIGHT.name)

    companion object {
        const val EXTRA_LANGUAGE = "language"
        const val EXTRA_THEME = "theme"
    }
}
