# Navigation

## Overview

Navigation uses Jetpack Navigation Compose with **type-safe serializable routes**. All routes are defined as `@Serializable` objects or data classes in `HomeScreenRoutes.kt`. The entire graph is wired in a single composable: `HomeNavigation.kt`.

---

## Route Definitions (`HomeScreenRoutes.kt`)

| Route | Type | Parameters |
|---|---|---|
| `HomeScreen` | `data object` | — |
| `GameConfigScreen` | `data class` | `gameModeName: Int`, `gameModeImage: Int`, `gameModeDescription: Int`, `partyId: Int?` |
| `CreatePlayerScreen` | `data class` | `playerId: Int = -1` (default = create mode), `isCouplesMode: Boolean = false` |
| `LoadingScreen` | `data object` | — |
| `GameScreen` | `data object` | — |
| `FollowTheSpotRoute` | `data class` | `player1Name`, `player1PhotoPath?`, `player1AvatarName?`, `player2Name`, `player2PhotoPath?`, `player2AvatarName?` |

---

## Navigation Flow

```
HomeScreen
    └─► GameConfigScreen
            ├─► CreatePlayerScreen  (create new player)
            ├─► CreatePlayerScreen(playerId)  (edit existing player)
            └─► LoadingScreen
                    └─► GameScreen  (LoadingScreen popped from back stack)
                            └─► FollowTheSpotRoute  (mini-game)
                                    └─► back to GameScreen  (with scores via SavedStateHandle)
```

---

## Scaffold & App Bar

`HomeNavigation` wraps the entire graph in a `Scaffold`. The top bar (`HomeAppBar`) is shown or hidden based on the current route:

```kotlin
val isFullScreenRoute =
    currentScreen?.hasRoute(LoadingScreen::class) == true ||
    currentScreen?.hasRoute(GameScreen::class)    == true ||
    currentScreen?.hasRoute(FollowTheSpotRoute::class) == true
```

- **Full-screen routes** (`LoadingScreen`, `GameScreen`, `FollowTheSpotRoute`): app bar is hidden with a slide-up + fade-out exit animation.
- **All other routes**: app bar is visible with a slide-down + fade-in enter animation.

The app bar title is managed via a `var appBarTitle` state in `HomeNavigation`. Screens that need a title call the `setAppBarTitle` lambda passed to them (e.g. `GameConfigScreen`, `CreatePlayerScreen`).

---

## Route-level Transitions

| Route | Enter | Exit |
|---|---|---|
| `LoadingScreen` | `slideInVertically { it } + fadeIn` (400ms) | `slideOutVertically { -it } + fadeOut` (300ms) |
| `GameConfigScreen` | default | `fadeOut` (300ms) |
| All others | default | default |

---

## Back Stack & Result Passing

- **`LoadingScreen → GameScreen`**: `LoadingScreen` is popped inclusively on navigation to `GameScreen`, so the user cannot navigate back to it.
- **`FollowTheSpotRoute → GameScreen`**: mini-game results (`mini_game_p1_score`, `mini_game_p2_score`) are written to the previous back stack entry's `SavedStateHandle` before calling `popBackStack()`.

---

## Screen Titles (`NavigationTitles.kt`)

`NavigationTitles` maps route qualified names to string resource IDs. Currently mapped routes:

| Route | String resource |
|---|---|
| `HomeScreen` | `R.string.home_screen` |
| `GameConfigScreen` | `R.string.prepare_your_party` |

Unmapped routes fall back to `R.string.home_screen`.
