package com.restrusher.partypuzl.ui.views.licenses

import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.licenses.OpenSourceLibrary
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme

@Composable
internal fun LicenseTextDialog(
    library: OpenSourceLibrary,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(id = R.string.close)) }
        },
        title = { Text(text = library.name) },
        text = {
            Text(
                text = library.licenseText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        modifier = modifier
    )
}

private val previewLibrary = OpenSourceLibrary(
    id = "androidx.room",
    name = "Room",
    version = "2.6.1",
    licenseNames = "Apache License 2.0",
    licenseText = "Apache License\nVersion 2.0, January 2004\nhttp://www.apache.org/licenses/"
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun LicenseTextDialogPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { LicenseTextDialog(previewLibrary, {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LicenseTextDialogDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { LicenseTextDialog(previewLibrary, {}) }
}
