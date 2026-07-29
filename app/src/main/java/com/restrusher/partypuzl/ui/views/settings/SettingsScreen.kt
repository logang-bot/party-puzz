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
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.data.preferences.AppLanguage
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.common.AdBannerView
import com.restrusher.partypuzl.ui.common.AdUnitIds

@Composable
fun SettingsScreen(
    setAppBarTitle: (String) -> Unit,
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
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 16.dp, top = 28.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_keyboard_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
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
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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

@Composable
private fun OptionRowWithIcon(
    label: String,
    @DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    tintIcon: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (!tintIcon) Color.Unspecified
            else if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        if (selected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.appColors.brandAccent
            )
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
private fun RemoveAdsRow(
    isAdFree: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isAdFree) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                painter = if (isAdFree) painterResource(R.drawable.ic_check_circle) else painterResource(R.drawable.ic_block),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(if (isAdFree) R.string.ads_removed else R.string.remove_ads),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(if (isAdFree) R.string.ads_removed_subtitle else R.string.remove_ads_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }
        if (!isAdFree) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
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
