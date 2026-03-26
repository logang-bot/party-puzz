package com.restrusher.partypuzz.ui.views.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.preferences.AppLanguage
import com.restrusher.partypuzz.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
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

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

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
        ModalBottomSheet(
            onDismissRequest = viewModel::closeThemeSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            BottomSheetTitle(text = stringResource(id = R.string.select_theme))
            ThemeMode.entries.forEach { mode ->
                OptionRow(
                    label = mode.toDisplayString(),
                    selected = mode == uiState.themeMode,
                    onClick = { viewModel.selectTheme(mode) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.isLanguageSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeLanguageSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            BottomSheetTitle(text = stringResource(id = R.string.select_language))
            // System always first, then remaining options alphabetically
            val languageOptions = listOf(AppLanguage.SYSTEM) +
                    AppLanguage.entries.filter { it != AppLanguage.SYSTEM }
            languageOptions.forEach { language ->
                OptionRow(
                    label = language.toDisplayString(),
                    selected = language == uiState.appLanguage,
                    onClick = { viewModel.selectLanguage(language) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
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
private fun BottomSheetTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
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
