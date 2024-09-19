package com.restrusher.partypuzz.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.navigation.toRoute
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.HomeAppBar
import com.restrusher.partypuzz.ui.views.gameConfig.GameConfigScreen
import com.restrusher.partypuzz.ui.views.home.HomeScreen
import kotlinx.serialization.Serializable

//enum class HomeScreens(@StringRes val title: Int) {
//    Home(title = R.string.home_screen),
//    GameConfig(title = R.string.game_config_screen)
//}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeNavigation(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination
    Scaffold(
        topBar = {
            HomeAppBar(
                currentScreen = currentScreen,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = HomeScreen,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<HomeScreen> {
                    HomeScreen(
                        animatedVisibilityScope = this,
                        onGameOptionSelected = { gameName ->
                            navController.navigate(GameConfigScreen(gameModeName = gameName))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable<GameConfigScreen> {
                    val args = it.toRoute<GameConfigScreen>()
                    GameConfigScreen(
                        animatedVisibilityScope = this,
                        gameModeName = args.gameModeName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Serializable
object HomeScreen

@Serializable
data class GameConfigScreen(
    val gameModeName: String
)