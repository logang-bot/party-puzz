package com.restrusher.partypuzl.ui.views.licenses

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.licenses.OpenSourceLibrary
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ink

@Composable
fun LicensesScreen(
    setAppBarTitle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicensesViewModel = hiltViewModel()
) {
    val title = stringResource(id = R.string.open_source_licenses)
    LaunchedEffect(Unit) { setAppBarTitle(title) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LicensesContent(
        state = uiState,
        onLibraryClick = viewModel::openLicense,
        modifier = modifier
    )

    uiState.selectedLibrary?.let { library ->
        LicenseTextDialog(library = library, onDismiss = viewModel::closeLicense)
    }
}

@Composable
private fun LicensesContent(
    state: LicensesState,
    onLibraryClick: (OpenSourceLibrary) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { LicensesIntro() }
        items(items = state.libraries, key = { it.id }) { library ->
            LibraryRow(library = library, onClick = { onLibraryClick(library) })
        }
    }
}

@Composable
private fun LicensesIntro(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.open_source_licenses_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

@Composable
private fun LibraryRow(
    library: OpenSourceLibrary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = library.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = listOfNotNull(library.version, library.licenseNames.ifBlank { null })
                .joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary)
        )
    }
}

private val previewLicensesState = LicensesState(
    isLoading = false,
    libraries = listOf(
        OpenSourceLibrary("androidx.room", "Room", "2.6.1", "Apache License 2.0", ""),
        OpenSourceLibrary("io.coil-kt", "Coil", "3.0.4", "Apache License 2.0", "")
    )
)

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 360)
@Composable
private fun LicensesContentPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        LicensesContent(previewLicensesState, {}, Modifier.appBackground())
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 360)
@Composable
private fun LicensesContentDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        LicensesContent(previewLicensesState, {}, Modifier.appBackground())
    }
}
