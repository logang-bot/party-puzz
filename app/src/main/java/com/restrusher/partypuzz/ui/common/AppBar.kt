package com.restrusher.partypuzz.ui.common

import androidx.compose.foundation.Image
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import com.restrusher.partypuzz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    currentScreen: NavDestination?, navigateUp: () -> Unit, modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
//            Text(text = stringResource(id = R.string.app_name), color = MaterialTheme.colorScheme.onPrimaryContainer)
            Image(
                painter = painterResource(id = R.drawable.img_partypuzz_logo),
                contentDescription = stringResource(
                    id = R.string.app_name
                )
            )
        }, colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ), modifier = modifier
    )
}