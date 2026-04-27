package com.restrusher.partypuzz.ui.views.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.preferences.AppLanguage
import com.restrusher.partypuzz.data.preferences.ThemeMode

@Composable
fun SettingsScreen(
    setAppBarTitle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val title = stringResource(id = R.string.settings)
    LaunchedEffect(Unit) { setAppBarTitle(title) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            )
    ) {
        SettingsSectionHeader(title = stringResource(id = R.string.appearance))

        ListItem(
            headlineContent = { Text(text = stringResource(id = R.string.theme_color)) },
            supportingContent = { Text(text = uiState.themeMode.toDisplayString()) },
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_settings),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { viewModel.openThemeSheet() }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SettingsSectionHeader(title = stringResource(id = R.string.language))

        ListItem(
            headlineContent = { Text(text = stringResource(id = R.string.language)) },
            supportingContent = { Text(text = uiState.appLanguage.toDisplayString()) },
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_mood),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { viewModel.openLanguageSheet() }
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
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
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
                tint = MaterialTheme.colorScheme.primary
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
