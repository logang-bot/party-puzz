package com.restrusher.partypuzl.ui.views.customPacks.create.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.PackTopic
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ctaScrim
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.customPacks.create.CreateCustomPackState
import com.restrusher.partypuzl.ui.views.customPacks.create.CreateCustomPackViewModel
import com.restrusher.partypuzl.ui.views.customPacks.previewPackFormState
import com.restrusher.partypuzl.ui.views.customPacks.ui.CustomPackCta
import com.restrusher.partypuzl.ui.views.customPacks.ui.NumberedStep

/**
 * The pack shell — name, topic, spice, description. Entries come afterwards, one at a time, so
 * each kind can ask only for what it actually needs.
 *
 * [onSaved] receives the pack id: creating a pack drops the user straight into its (empty) editor.
 */
@Composable
fun CreateCustomPackScreen(
    packId: String?,
    setAppBarTitle: (String) -> Unit,
    onSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateCustomPackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(packId) { viewModel.load(packId) }

    val title = stringResource(
        if (packId != null) R.string.custom_pack_edit_title else R.string.custom_pack_new_title
    )
    LaunchedEffect(title) { setAppBarTitle(title) }

    CreateCustomPackContent(
        state = uiState,
        onNameChange = viewModel::onNameChange,
        onTopicChange = viewModel::onTopicChange,
        onSpiceChange = viewModel::onSpiceChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSave = { viewModel.onSave(onSaved) },
        modifier = modifier
    )
}

@Composable
private fun CreateCustomPackContent(
    state: CreateCustomPackState,
    onNameChange: (String) -> Unit,
    onTopicChange: (PackTopic) -> Unit,
    onSpiceChange: (SpiceLevel) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {
            NumberedStep(number = "01", label = stringResource(R.string.custom_pack_step_name))
            Spacer(modifier = Modifier.height(10.dp))
            PackTextField(
                value = state.name,
                onValueChange = onNameChange,
                placeholder = stringResource(R.string.custom_pack_name_hint)
            )

            NumberedStep(
                number = "02",
                label = stringResource(R.string.custom_pack_step_topic),
                subtitle = stringResource(R.string.custom_pack_step_topic_sub)
            )
            Spacer(modifier = Modifier.height(10.dp))
            TopicPills(selected = state.topic, onSelect = onTopicChange)

            NumberedStep(
                number = "03",
                label = stringResource(R.string.custom_pack_step_spice),
                subtitle = stringResource(R.string.custom_pack_step_spice_sub)
            )
            Spacer(modifier = Modifier.height(10.dp))
            SpiceSelector(selected = state.spice, onSelect = onSpiceChange)

            NumberedStep(
                number = "04",
                label = stringResource(R.string.custom_pack_step_description),
                subtitle = stringResource(R.string.custom_pack_step_description_sub)
            )
            Spacer(modifier = Modifier.height(10.dp))
            PackTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.custom_pack_description_hint),
                singleLine = false,
                minHeight = 96
            )
            CharacterCounter(length = state.description.length)

            if (!state.isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.custom_pack_create_footnote),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary)
                )
            }
        }

        CustomPackCta(
            label = stringResource(
                if (state.isEditing) R.string.custom_pack_save_action
                else R.string.custom_pack_create_action
            ),
            iconRes = R.drawable.ic_check,
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .ctaScrim()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun CreateCustomPackSample(state: CreateCustomPackState) {
    Box(modifier = Modifier.fillMaxSize().appBackground()) {
        CreateCustomPackContent(
            state = state,
            onNameChange = {},
            onTopicChange = {},
            onSpiceChange = {},
            onDescriptionChange = {},
            onSave = {}
        )
    }
}

@Preview(name = "CreateCustomPack – Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateCustomPackLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { CreateCustomPackSample(previewPackFormState) }
}

@Preview(name = "CreateCustomPack – Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateCustomPackDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { CreateCustomPackSample(previewPackFormState) }
}

@Preview(name = "EditCustomPack – Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun EditCustomPackLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        CreateCustomPackSample(previewPackFormState.copy(packId = "custom_preview"))
    }
}
