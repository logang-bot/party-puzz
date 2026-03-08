package com.restrusher.partypuzz.ui.views.createPlayer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzz.R

@Composable
fun ImageOptionsContainer(
    takePictureAction: () -> Unit,
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
        ImageOptionButton(R.drawable.ic_camera, R.string.take_photo, takePictureAction)
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 5.dp))
        ImageOptionButton(R.drawable.ic_random, R.string.generate_random_image, {})
    }
}

@Composable
fun ImageOptionButton(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    action: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier
        .clickable(onClick = action)
        .padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(icon),
            contentDescription = stringResource(text),
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimaryContainer)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(text), lineHeight = 16.sp, style = MaterialTheme.typography.titleMedium)
    }
}
