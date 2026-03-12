package com.restrusher.partypuzz.ui.views.createPlayer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
fun GenderOptionsContainer(
    selectedGender: Gender,
    onGenderSelected: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        GenderButton(
            icon = R.drawable.ic_male,
            text = R.string.male,
            label = stringResource(R.string.male),
            isSelected = selectedGender == Gender.Male,
            onClick = { onGenderSelected(Gender.Male) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        VerticalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 5.dp)
        )
        GenderButton(
            icon = R.drawable.ic_female,
            text = R.string.female,
            label = stringResource(R.string.female),
            isSelected = selectedGender == Gender.Female,
            onClick = { onGenderSelected(Gender.Female) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun GenderButton(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(durationMillis = 300),
        label = "genderButtonBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = tween(durationMillis = 300),
        label = "genderButtonText"
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = stringResource(text),
            colorFilter = ColorFilter.tint(color = textColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenderOptionsContainerPreview() {
    PartyPuzzTheme {
        GenderOptionsContainer(
            selectedGender = Gender.Male,
            onGenderSelected = {}
        )
    }
}
