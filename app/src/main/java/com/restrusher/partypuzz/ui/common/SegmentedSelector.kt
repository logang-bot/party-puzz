package com.restrusher.partypuzz.ui.common

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

class SegmentItem(
    @DrawableRes val iconRes: Int,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun SegmentedSelector(
    items: List<SegmentItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Transparent)
            .padding(4.dp)
    ) {
        items.forEach { item ->
            SelectorItem(item = item, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SelectorItem(
    item: SegmentItem,
    modifier: Modifier = Modifier
) {
    val bgColor = if (item.selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (item.selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { item.onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedSelectorPreview() {
    PartyPuzzTheme {
        SegmentedSelector(
            items = listOf(
                SegmentItem(R.drawable.ic_random, "Generated", selected = true, onClick = {}),
                SegmentItem(R.drawable.ic_camera, "Take Photo", selected = false, onClick = {})
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SegmentedSelectorDarkPreview() {
    PartyPuzzTheme {
        SegmentedSelector(
            items = listOf(
                SegmentItem(R.drawable.ic_random, "Generated", selected = true, onClick = {}),
                SegmentItem(R.drawable.ic_camera, "Take Photo", selected = false, onClick = {})
            )
        )
    }
}
