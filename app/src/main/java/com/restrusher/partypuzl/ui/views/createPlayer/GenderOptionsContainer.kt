package com.restrusher.partypuzl.ui.views.createPlayer

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.ui.common.SegmentItem
import com.restrusher.partypuzl.ui.common.SegmentedSelector
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme

@Composable
fun GenderOptionsContainer(
    selectedGender: Gender?,
    onGenderSelected: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedSelector(
        items = listOf(
            SegmentItem(
                iconRes = R.drawable.ic_male,
                label = stringResource(R.string.male),
                selected = selectedGender == Gender.Male,
                onClick = { onGenderSelected(Gender.Male) }
            ),
            SegmentItem(
                iconRes = R.drawable.ic_female,
                label = stringResource(R.string.female),
                selected = selectedGender == Gender.Female,
                onClick = { onGenderSelected(Gender.Female) }
            )
        ),
        modifier = modifier
    )
}

@Composable
fun InterestedInOptionsContainer(
    selectedInterestedIn: InterestedIn?,
    onInterestedInSelected: (InterestedIn) -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedSelector(
        items = listOf(
            SegmentItem(
                iconRes = R.drawable.ic_man,
                label = stringResource(R.string.man),
                selected = selectedInterestedIn == InterestedIn.Man,
                onClick = { onInterestedInSelected(InterestedIn.Man) }
            ),
            SegmentItem(
                iconRes = R.drawable.ic_woman,
                label = stringResource(R.string.woman),
                selected = selectedInterestedIn == InterestedIn.Woman,
                onClick = { onInterestedInSelected(InterestedIn.Woman) }
            ),
            SegmentItem(
                iconRes = R.drawable.ic_wc,
                label = stringResource(R.string.both),
                selected = selectedInterestedIn == InterestedIn.Both,
                onClick = { onInterestedInSelected(InterestedIn.Both) }
            )
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun GenderOptionsContainerPreview() {
    PartyPuzlTheme {
        GenderOptionsContainer(selectedGender = Gender.Male, onGenderSelected = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GenderOptionsContainerDarkPreview() {
    PartyPuzlTheme {
        GenderOptionsContainer(selectedGender = Gender.Male, onGenderSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InterestedInOptionsContainerPreview() {
    PartyPuzlTheme {
        InterestedInOptionsContainer(selectedInterestedIn = InterestedIn.Man, onInterestedInSelected = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InterestedInOptionsContainerDarkPreview() {
    PartyPuzlTheme {
        InterestedInOptionsContainer(selectedInterestedIn = InterestedIn.Man, onInterestedInSelected = {})
    }
}
