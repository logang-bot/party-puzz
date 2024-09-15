package com.restrusher.partypuzz.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.HomeAppBar
import com.restrusher.partypuzz.ui.views.home.HomeScreen

enum class HomeScreens(@StringRes val title: Int) {
    Home(title = R.string.home_screen)
}

@Composable
fun HomeNavigation(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = HomeScreens.valueOf(
        backStackEntry?.destination?.route ?: HomeScreens.Home.name
    )
    Scaffold(
        topBar = {
            HomeAppBar(
                currentScreen = currentScreen,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeScreens.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = HomeScreens.Home.name) {
                HomeScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}