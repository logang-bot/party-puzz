package com.restrusher.partypuzz.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.appBackground
import com.restrusher.partypuzz.ui.common.HomeAppBar
import com.restrusher.partypuzz.ui.views.createPlayer.CreatePlayerScreen as CreatePlayerScreenComposable
import com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot.FollowTheSpotScreen
import com.restrusher.partypuzz.ui.views.game.miniGames.hotPotato.HotPotatoScreen
import com.restrusher.partypuzz.ui.views.game.gameScreen.GameScreen
import com.restrusher.partypuzz.ui.views.game.gameScreen.MiniGame
import com.restrusher.partypuzz.ui.views.gameConfig.ui.GameConfigScreen
import com.restrusher.partypuzz.ui.views.gameLoading.LoadingScreen
import com.restrusher.partypuzz.ui.views.home.HomeScreen
import com.restrusher.partypuzz.ui.views.parties.PartiesScreen
import com.restrusher.partypuzz.ui.views.partyDetail.PartyDetailScreen
import com.restrusher.partypuzz.ui.views.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeNavigation(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination
    var appBarTitle by remember { mutableStateOf("") }

    val isFullScreenRoute = currentScreen?.hasRoute(LoadingScreen::class) == true ||
            currentScreen?.hasRoute(GameScreen::class) == true ||
            currentScreen?.hasRoute(FollowTheSpotRoute::class) == true ||
            currentScreen?.hasRoute(HotPotatoRoute::class) == true

    val isHomeScreen = currentScreen?.hasRoute(HomeScreen::class) == true

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isHomeScreen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                windowInsets = WindowInsets(0)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_partypuzz_logo),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 28.dp, top = 24.dp, bottom = 24.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    },
                    label = { Text(text = stringResource(id = R.string.play)) },
                    selected = isHomeScreen,
                    onClick = { scope.launch { drawerState.close() } },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_outline_mood),
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.parties)) },
                    selected = currentScreen?.hasRoute(PartiesScreen::class) == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(PartiesScreen) {
                            launchSingleTop = true
                        }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_outline_settings),
                            contentDescription = null
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.settings)) },
                    selected = currentScreen?.hasRoute(SettingsScreen::class) == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(SettingsScreen) {
                            launchSingleTop = true
                        }
                    },
                    colors = drawerItemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            modifier = Modifier.fillMaxSize().appBackground(),
            topBar = {
                AnimatedVisibility(
                    visible = !isFullScreenRoute,
                    enter = slideInVertically(tween(300)) { -it } + fadeIn(tween(300)),
                    exit = slideOutVertically(tween(250)) { -it } + fadeOut(tween(200))
                ) {
                    HomeAppBar(
                        title = appBarTitle,
                        currentDestination = currentScreen,
                        canNavigateBack = navController.previousBackStackEntry != null,
                        navigateUp = { navController.navigateUp() },
                        onMenuClick = { scope.launch { drawerState.open() } }
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
                        HomeScreen(
                            animatedVisibilityScope = this,
                            onGameOptionSelected = { name, image, description, partyId ->
                                navController.navigate(GameConfigScreen(gameModeName = name, gameModeImage = image, gameModeDescription = description, partyId = partyId))
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<GameConfigScreen>(
                        exitTransition = { fadeOut(tween(300)) }
                    ) {
                        val args = it.toRoute<GameConfigScreen>()
                        GameConfigScreen(
                            setAppBarTitle = { title ->
                                appBarTitle = title
                            },
                            animatedVisibilityScope = this,
                            gameModeName = args.gameModeName,
                            gameModeImage = args.gameModeImage,
                            gameModeDescription = args.gameModeDescription,
                            onCreatePlayerClick = {
                                navController.navigate(CreatePlayerScreen(isCouplesMode = args.gameModeName == R.string.couples_game_mode))
                            },
                            onEditPlayerClick = { playerId ->
                                navController.navigate(CreatePlayerScreen(playerId = playerId, isCouplesMode = args.gameModeName == R.string.couples_game_mode))
                            },
                            onStartGameClick = {
                                navController.navigate(LoadingScreen)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<CreatePlayerScreen> {
                        val playerId = it.toRoute<CreatePlayerScreen>().playerId
                        CreatePlayerScreenComposable(
                            setAppBarTitle = { title ->
                                appBarTitle = title
                            },
                            animatedVisibilityScope = this,
                            playerId = playerId,
                            navigateBack = { navController.popBackStack() }
                        )
                    }
                    composable<LoadingScreen>(
                        enterTransition = { slideInVertically(tween(400)) { it } + fadeIn(tween(400)) },
                        exitTransition = { slideOutVertically(tween(300)) { -it } + fadeOut(tween(300)) }
                    ) {
                        LoadingScreen(
                            onLoadingComplete = {
                                navController.navigate(GameScreen) {
                                    popUpTo(LoadingScreen) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<GameScreen>(
                        enterTransition = { slideInVertically(tween(400)) { it } + fadeIn(tween(400)) }
                    ) {
                        GameScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToMiniGame = { miniGame, challenger, opponent ->
                                when (miniGame) {
                                    MiniGame.FOLLOW_THE_SPOT -> navController.navigate(
                                        FollowTheSpotRoute(
                                            player1Name = challenger.nickName,
                                            player1PhotoPath = challenger.photoPath,
                                            player1AvatarName = challenger.avatarName,
                                            player2Name = opponent.nickName,
                                            player2PhotoPath = opponent.photoPath,
                                            player2AvatarName = opponent.avatarName
                                        )
                                    )
                                    else -> Unit
                                }
                            },
                            onNavigateToGlobalMiniGame = { miniGame ->
                                when (miniGame) {
                                    MiniGame.HOT_POTATO -> navController.navigate(HotPotatoRoute)
                                    else -> Unit
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<FollowTheSpotRoute> {
                        FollowTheSpotScreen(
                            onGameFinished = { player1Score, player2Score ->
                                navController.previousBackStackEntry?.savedStateHandle?.apply {
                                    set("mini_game_p1_score", player1Score)
                                    set("mini_game_p2_score", player2Score)
                                }
                                navController.popBackStack()
                            },
                            onAbortGame = {
                                navController.previousBackStackEntry?.savedStateHandle
                                    ?.set("mini_game_aborted", true)
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<HotPotatoRoute> {
                        HotPotatoScreen(
                            onGameFinished = { loserName ->
                                navController.previousBackStackEntry?.savedStateHandle
                                    ?.set("hot_potato_loser", loserName)
                                navController.popBackStack()
                            },
                            onAbortGame = {
                                navController.previousBackStackEntry?.savedStateHandle
                                    ?.set("mini_game_aborted", true)
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<PartiesScreen> {
                        PartiesScreen(
                            setAppBarTitle = { title -> appBarTitle = title },
                            onPartyClick = { partyId ->
                                navController.navigate(PartyDetailScreen(partyId = partyId))
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<PartyDetailScreen> {
                        val partyId = it.toRoute<PartyDetailScreen>().partyId
                        PartyDetailScreen(
                            partyId = partyId,
                            setAppBarTitle = { title -> appBarTitle = title },
                            navigateBack = { navController.popBackStack() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<SettingsScreen> {
                        SettingsScreen(
                            setAppBarTitle = { title -> appBarTitle = title },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
