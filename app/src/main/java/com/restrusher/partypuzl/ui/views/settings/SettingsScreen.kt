package com.restrusher.partypuzl.ui.views.settings

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.AppLanguage
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.common.AdBannerView
import com.restrusher.partypuzl.ui.common.AdUnitIds
import com.restrusher.partypuzl.ui.theme.appColors

@Composable
fun SettingsScreen(
    setAppBarTitle: (String) -> Unit,
    onManagePacksClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val title = stringResource(id = R.string.settings)
    LaunchedEffect(Unit) { setAppBarTitle(title) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (uiState.isAdFree) 0.dp else 50.dp)
        ) {
            SettingsSectionHeader(title = stringResource(id = R.string.appearance))

            SettingsRow(
                title = stringResource(id = R.string.theme_color),
                subtitle = uiState.themeMode.toDisplayString(),
                iconRes = R.drawable.ic_dark_mode,
                onClick = viewModel::openThemeSheet
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader(title = stringResource(id = R.string.language))

            SettingsRow(
                title = stringResource(id = R.string.language),
                subtitle = uiState.appLanguage.toDisplayString(),
                iconRes = R.drawable.ic_flag_system,
                onClick = viewModel::openLanguageSheet
            )

            SettingsRow(
                title = stringResource(id = R.string.custom_packs_title),
                subtitle = stringResource(
                    id = R.string.custom_packs_active_count,
                    uiState.activeCustomPacks
                ),
                iconRes = R.drawable.ic_chat_bubble,
                onClick = onManagePacksClick
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader(title = stringResource(id = R.string.purchases))

            RemoveAdsRow(
                isAdFree = uiState.isAdFree,
                onClick = { viewModel.purchaseRemoveAds(context as Activity) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader(title = stringResource(id = R.string.support))

            SettingsRow(
                title = stringResource(id = R.string.support_dev),
                subtitle = stringResource(id = R.string.support_dev_subtitle),
                iconRes = R.drawable.ic_sports_bar,
                onClick = { uriHandler.openUri("https://ko-fi.com/restrusher") }
            )

            SettingsRow(
                title = stringResource(id = R.string.support_dev_airtm),
                subtitle = stringResource(id = R.string.support_dev_airtm_subtitle),
                iconRes = R.drawable.ic_whatshot,
                onClick = { uriHandler.openUri("https://airtm.me/lordgatsu") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader(title = stringResource(id = R.string.legal))

            SettingsRow(
                title = stringResource(id = R.string.privacy_policy),
                subtitle = stringResource(id = R.string.privacy_policy_subtitle),
                iconRes = R.drawable.ic_info,
                onClick = {
                    uriHandler.openUri("https://logang-bot.github.io/partypuzz-legal/privacy-policy.html")
                }
            )
        }

        AdBannerView(
            adUnitId = AdUnitIds.SETTINGS_BANNER,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (uiState.isThemeSheetOpen) {
        SettingsOptionsDialog(
            title = stringResource(id = R.string.select_theme),
            onDismiss = viewModel::closeThemeSheet
        ) {
            ThemeMode.entries.forEach { mode ->
                OptionRowWithIcon(
                    label = mode.toDisplayString(),
                    iconRes = mode.toDisplayIconRes(),
                    selected = mode == uiState.themeMode,
                    onClick = { viewModel.selectTheme(mode) }
                )
            }
        }
    }

    if (uiState.isLanguageSheetOpen) {
        val languageOptions = listOf(AppLanguage.SYSTEM) +
                AppLanguage.entries.filter { it != AppLanguage.SYSTEM }
        SettingsOptionsDialog(
            title = stringResource(id = R.string.select_language),
            onDismiss = viewModel::closeLanguageSheet
        ) {
            languageOptions.forEach { language ->
                OptionRowWithIcon(
                    label = language.toDisplayString(),
                    iconRes = language.toDisplayIconRes(),
                    selected = language == uiState.appLanguage,
                    onClick = { viewModel.selectLanguage(language) },
                    tintIcon = language == AppLanguage.SYSTEM
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionsDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@DrawableRes
private fun ThemeMode.toDisplayIconRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.drawable.ic_theme_auto
    ThemeMode.LIGHT -> R.drawable.ic_light_mode
    ThemeMode.DARK -> R.drawable.ic_dark_mode
}

@DrawableRes
private fun AppLanguage.toDisplayIconRes(): Int = when (this) {
    AppLanguage.SYSTEM -> R.drawable.ic_flag_system
    AppLanguage.ENGLISH -> R.drawable.ic_flag_us
    AppLanguage.SPANISH -> R.drawable.ic_flag_es
}

@Composable
private fun ThemeMode.toDisplayString(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(id = R.string.system)
    ThemeMode.LIGHT -> stringResource(id = R.string.light)
    ThemeMode.DARK -> stringResource(id = R.string.dark)
}

@Composable
private fun AppLanguage.toDisplayString(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(id = R.string.system)
    AppLanguage.ENGLISH -> stringResource(id = R.string.english)
    AppLanguage.SPANISH -> stringResource(id = R.string.spanish)
}
