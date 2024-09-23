package com.restrusher.partypuzz.ui.common

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.navigation.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    title: String,
    currentDestination: NavDestination?,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowLogo = currentDestination?.hasRoute(HomeScreen::class) ?: false
    CenterAlignedTopAppBar(
        title = {
            if (shouldShowLogo)
                Image(
                    painter = painterResource(id = R.drawable.img_partypuzz_logo),
                    contentDescription = stringResource(
                        id = R.string.app_name
                    )
                )
            else
                Text(text = title, color = MaterialTheme.colorScheme.onPrimaryContainer)
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationIcon = {
            if (canNavigateBack)
                IconButton(onClick = navigateUp) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                        id = R.string.back_button
                    ))
                }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun HomeAppBarPreview() {
    HomeAppBar(
        title = "PartyPuzz",
        currentDestination = null,
        canNavigateBack = true,
        navigateUp = { }
    )
}