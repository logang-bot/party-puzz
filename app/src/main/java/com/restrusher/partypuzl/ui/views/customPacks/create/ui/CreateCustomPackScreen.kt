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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.views.customPacks.create.CreateCustomPackViewModel
import com.restrusher.partypuzl.ui.views.customPacks.ui.CustomPackCta
import com.restrusher.partypuzl.ui.views.customPacks.ui.NumberedStep

/**
 * The pack shell — name, category, spice, description. Entries come afterwards, one at a time, so
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
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                placeholder = stringResource(R.string.custom_pack_name_hint)
            )

            NumberedStep(
                number = "02",
                label = stringResource(R.string.custom_pack_step_category),
                subtitle = stringResource(R.string.custom_pack_step_category_sub)
            )
            Spacer(modifier = Modifier.height(10.dp))
            CategoryPills(selected = uiState.category, onSelect = viewModel::onCategoryChange)

            NumberedStep(
                number = "03",
                label = stringResource(R.string.custom_pack_step_spice),
                subtitle = stringResource(R.string.custom_pack_step_spice_sub)
            )
            Spacer(modifier = Modifier.height(10.dp))
            SpiceSelector(selected = uiState.spice, onSelect = viewModel::onSpiceChange)

            NumberedStep(
                number = "04",
                label = stringResource(R.string.custom_pack_step_description),
                subtitle = stringResource(R.string.custom_pack_step_description_sub)
            )
            Spacer(modifier = Modifier.height(10.dp))
            PackTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = stringResource(R.string.custom_pack_description_hint),
                singleLine = false,
                minHeight = 96
            )
            CharacterCounter(length = uiState.description.length)

            if (!uiState.isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.custom_pack_create_footnote),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                )
            }
        }

        CustomPackCta(
            label = stringResource(
                if (uiState.isEditing) R.string.custom_pack_save_action
                else R.string.custom_pack_create_action
            ),
            iconRes = R.drawable.ic_check,
            onClick = { viewModel.onSave(onSaved) },
            enabled = uiState.canSave,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
