package com.restrusher.partypuzz.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.ui.common.HomeAppBar
import com.restrusher.partypuzz.ui.views.createPlayer.CreatePlayerScreen
import com.restrusher.partypuzz.ui.views.game.GameScreen
import com.restrusher.partypuzz.ui.views.gameConfig.ui.GameConfigScreen
import com.restrusher.partypuzz.ui.views.gameLoading.LoadingScreen
import com.restrusher.partypuzz.ui.views.home.HomeScreen
import com.restrusher.partypuzz.ui.views.home.HomeViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeNavigation(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination
    var appBarTitle by remember { mutableStateOf("") }

    val isFullScreenRoute = currentScreen?.hasRoute(LoadingScreen::class) == true ||
            currentScreen?.hasRoute(GameScreen::class) == true

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (!isFullScreenRoute) {
                HomeAppBar(
                    title = appBarTitle,
                    currentDestination = currentScreen,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() }
                )
            }
        }
    ) { innerPadding ->
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = HomeScreen,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable<HomeScreen> {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    HomeScreen(
                        animatedVisibilityScope = this,
                        uiState = uiState,
                        onGameOptionSelected = { name, image, partyId ->
                            navController.navigate(GameConfigScreen(gameModeName = name, gameModeImage = image, partyId = partyId))
                        },
                        onTogglePartySelection = viewModel::togglePartySelection,
                        onOpenDialog = viewModel::openDialog,
                        onCloseDialog = viewModel::closeDialog,
                        onSelectDialogParty = viewModel::selectDialogParty,
                        onConfirmPartySelection = viewModel::confirmPartySelection,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable<GameConfigScreen> {
                    val args = it.toRoute<GameConfigScreen>()
                    GameConfigScreen(
                        setAppBarTitle = { title ->
                            appBarTitle = title
                        },
                        animatedVisibilityScope = this,
                        gameModeName = args.gameModeName,
                        gameModeImage = args.gameModeImage,
                        onCreatePlayerClick = {
                            navController.navigate(CreatePlayerScreen)
                        },
                        onStartGameClick = {
                            navController.navigate(LoadingScreen)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable<CreatePlayerScreen> {
                    CreatePlayerScreen(
                        setAppBarTitle = { title ->
                            appBarTitle = title
                        },
                        animatedVisibilityScope = this,
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable<LoadingScreen> {
                    LoadingScreen(
                        onLoadingComplete = {
                            navController.navigate(GameScreen) {
                                popUpTo(LoadingScreen) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable<GameScreen> {
                    GameScreen(
                        onNavigateBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
