package com.restrusher.partypuzl.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.restrusher.partypuzl.data.preferences.ThemeMode

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * Colors the Material 3 [androidx.compose.material3.ColorScheme] has no slot for.
 *
 * Note what is deliberately *not* here: the mode gradients, deal accents and
 * outcome palettes. Those are the same in both themes, so they live as plain
 * top-level values in Color.kt — routing them through a CompositionLocal would
 * be ceremony for no benefit.
 */
@Immutable
data class AppColors(
    /**
     * Brand color that has to stay legible against the page background, so it
     * flips per theme — unlike `colorScheme.primary`, which stays bright teal in
     * both themes because it is only ever used as a fill or a stroke.
     */
    val brandAccent: Color,
    /** Ink on top of a mode/deal/outcome gradient. White in both themes. */
    val onAccentSurface: Color,
    /** Full-screen dim behind a dialog, bottom sheet or blocking spinner. */
    val scrim: Color,
    /** Translucent chip sitting on a photo or a colored card. */
    val chipScrim: Color,
    /** Card fill: an alpha wash in dark, opaque white in light. */
    val cardSurface: Color,
    val cardBorder: Color,
    /** Drop-shadow color used to lift cards off the cream background in light. */
    val cardShadow: Color,
    /** Frosted panel behind the mini-game countdown. */
    val glassTint: Color,
    val glassEdge: Color,
    val onGlass: Color,
    val badgeOfficial: Color,
    val badgePremium: Color,
    val badgeCustom: Color,
)

val LightAppColors = AppColors(
    brandAccent = BrandTealLight,
    onAccentSurface = Color.White,
    scrim = Color(0xFF14140A).copy(alpha = 0.28f),
    chipScrim = Color(0xFF0E2630).copy(alpha = 0.72f),
    cardSurface = Color.White,
    cardBorder = Color(0xFF0B1F24).copy(alpha = 0.10f),
    cardShadow = Color(0xFF0B1F24),
    glassTint = Color.White.copy(alpha = 0.62f),
    glassEdge = Color.White.copy(alpha = 0.85f),
    onGlass = Color(0xFF0E2630),
    badgeOfficial = BrandTealLight,
    badgePremium = Color(0xFF8A6100),
    badgeCustom = Color(0xFF5B3FBF),
)

val DarkAppColors = AppColors(
    brandAccent = BrandTealSoft,
    onAccentSurface = Color.White,
    scrim = Color(0xFF050F12).copy(alpha = 0.55f),
    chipScrim = Color.Black.copy(alpha = 0.72f),
    cardSurface = Color.White.copy(alpha = 0.04f),
    cardBorder = Color.White.copy(alpha = 0.08f),
    cardShadow = Color.Transparent,
    glassTint = Color.White.copy(alpha = 0.22f),
    glassEdge = Color.White.copy(alpha = 0.30f),
    onGlass = Color.White,
    badgeOfficial = BrandTealSoft,
    badgePremium = AccentYellow,
    badgeCustom = AccentViolet,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** Access the non-Material tokens: `MaterialTheme.appColors.brandAccent`. */
val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

@Composable
fun PartyPuzlTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme else lightScheme,
            typography = AppTypography,
            content = content
        )
    }
}
