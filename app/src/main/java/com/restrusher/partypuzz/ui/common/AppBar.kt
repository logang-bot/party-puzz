package com.restrusher.partypuzz.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.dp
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
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowLogo = currentDestination?.hasRoute(HomeScreen::class) ?: false
    CenterAlignedTopAppBar(
        title = {
            if (!shouldShowLogo)
                Text(text = title, color = MaterialTheme.colorScheme.onPrimaryContainer)
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationIcon = {
            if (shouldShowLogo)
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(id = R.string.menu)
                    )
                }
            else if (canNavigateBack)
                IconButton(onClick = navigateUp) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                        id = R.string.back_button
                    ))
                }
        },
        actions = {
            if (shouldShowLogo)
                Image(
                    painter = painterResource(id = R.drawable.img_partypuzz_logo),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .width(80.dp)
                        .padding(end = 8.dp)
                )
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
        navigateUp = { },
        onMenuClick = { }
    )
}