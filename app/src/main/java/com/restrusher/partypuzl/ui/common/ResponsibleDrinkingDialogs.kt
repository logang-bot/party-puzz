package com.restrusher.partypuzl.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appColors

private const val DIALOG_HEIGHT_FRACTION = 0.7f

/** First-launch 18+ confirmation. Can't be dismissed: the player either confirms or leaves. */
@Composable
fun AgeConfirmationDialog(onAccept: () -> Unit, onExit: () -> Unit) {
    ResponsibleDrinkingDialogFrame(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        title = stringResource(R.string.drinking_notice_title),
        body = stringResource(R.string.drinking_notice_body)
    ) {
        PrimaryDialogButton(stringResource(R.string.drinking_notice_accept), onAccept)
        TextButton(onClick = onExit) {
            Text(text = stringResource(R.string.drinking_notice_exit))
        }
    }
}

/** Per-launch reminder for players who already confirmed their age. One tap, back or outside closes it. */
@Composable
fun DrinkingReminderDialog(onDismiss: () -> Unit) {
    ResponsibleDrinkingDialogFrame(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = stringResource(R.string.drinking_reminder_title),
        body = stringResource(R.string.drinking_reminder_body)
    ) {
        PrimaryDialogButton(stringResource(R.string.drinking_reminder_ok), onDismiss)
    }
}

@Composable
private fun ResponsibleDrinkingDialogFrame(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    title: String,
    body: String,
    actions: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .fillMaxHeight(DIALOG_HEIGHT_FRACTION)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                DialogMessage(title = title, body = body, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(16.dp))
                actions()
            }
        }
    }
}

@Composable
private fun DialogMessage(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.drinking_notice_badge),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.appColors.brandAccent
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PrimaryDialogButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onTertiary,
            containerColor = MaterialTheme.colorScheme.tertiary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
private fun AgeConfirmationDialogPreview() {
    PartyPuzlTheme { AgeConfirmationDialog(onAccept = {}, onExit = {}) }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AgeConfirmationDialogDarkPreview() {
    PartyPuzlTheme { AgeConfirmationDialog(onAccept = {}, onExit = {}) }
}

@Preview(showBackground = true)
@Composable
private fun DrinkingReminderDialogPreview() {
    PartyPuzlTheme { DrinkingReminderDialog(onDismiss = {}) }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrinkingReminderDialogDarkPreview() {
    PartyPuzlTheme { DrinkingReminderDialog(onDismiss = {}) }
}
