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
| `LoadingScreen` | `data object` | — (declared but **not registered in the graph** — see below) |
| `GameScreen` | `data object` | — |
| `FollowTheSpotRoute` | `data class` | `player1Name`, `player1PhotoPath?`, `player1AvatarName?`, `player2Name`, `player2PhotoPath?`, `player2AvatarName?` |
| `HotPotatoRoute` | `data object` | — (ViewModel reads all players from `GamePlayersList` directly) |
| `SimonSaysRoute` | `data object` | — (ViewModel reads all players from `GamePlayersList` directly) |
| `TapWarRoute` | `data class` | `player1Name`, `player1PhotoPath?`, `player1AvatarName?`, `player2Name`, `player2PhotoPath?`, `player2AvatarName?` |
| `CircleMasterRoute` | `data object` | — (ViewModel reads all players from `GamePlayersList` directly) |
| `CustomPacksRoute` | `data object` | — |
| `CreateCustomPackRoute` | `data class` | `packId: String? = null` (null = create, otherwise edit the shell) |
| `CustomPackEditorRoute` | `data class` | `packId: String` |
| `CreateCustomEntryRoute` | `data class` | `packId: String`, `entryId: String? = null` (null = new entry) |

The four custom-pack destinations are registered by `customPacksGraph()` in `CustomPacksGraph.kt` rather than inline, so `HomeNavigation.kt` gains one call instead of sixty lines. See [custom-packs.md](custom-packs.md).

---

## Navigation Flow

```
HomeScreen
    └─► GameConfigScreen
            ├─► CreatePlayerScreen  (create new player)
            ├─► CreatePlayerScreen(playerId)  (edit existing player)
            └─► GameScreen
                            ├─► FollowTheSpotRoute  (2-player mini-game)
                            │       └─► back to GameScreen  (p1Score, p2Score via SavedStateHandle)
                            └─► HotPotatoRoute  (global mini-game)
                                    └─► back to GameScreen  (loserName via SavedStateHandle)

SettingsScreen
    └─► CustomPacksRoute
            ├─► CreateCustomPackRoute()          (new pack)
            │       └─► CustomPackEditorRoute(id)  (replaces itself on the back stack)
            ├─► CreateCustomPackRoute(packId)    (edit the pack's name/category/spice/description)
            └─► CustomPackEditorRoute(packId)
                    └─► CreateCustomEntryRoute(packId, entryId?)
```

---

## Scaffold & App Bar

`HomeNavigation` wraps the entire graph in a `Scaffold`. The top bar (`HomeAppBar`) is shown or hidden based on the current route:

```kotlin
val isFullScreenRoute =
    currentScreen?.hasRoute(GameScreen::class)         == true ||
    currentScreen?.hasRoute(FollowTheSpotRoute::class) == true ||
    currentScreen?.hasRoute(HotPotatoRoute::class)     == true ||
    currentScreen?.hasRoute(TapWarRoute::class)        == true ||
    currentScreen?.hasRoute(SimonSaysRoute::class)     == true ||
    currentScreen?.hasRoute(CircleMasterRoute::class)  == true
```

- **Full-screen routes** (`GameScreen`, `FollowTheSpotRoute`, `HotPotatoRoute`, `TapWarRoute`, `SimonSaysRoute`, `CircleMasterRoute`): app bar is hidden with a slide-up + fade-out exit animation.
- **All other routes**: app bar is visible with a slide-down + fade-in enter animation.

The app bar title is managed via a `var appBarTitle` state in `HomeNavigation`. Screens that need a title call the `setAppBarTitle` lambda passed to them (e.g. `GameConfigScreen`, `CreatePlayerScreen`).

---

## Route-level Transitions

| Route | Enter | Exit |
|---|---|---|
| `GameScreen` | `slideInVertically { it } + fadeIn` (400ms) | default |
| `GameConfigScreen` | default | `fadeOut` (300ms) |
| All others | default | default |

---

## Back Stack & Result Passing

- **`GameConfigScreen → GameScreen`**: a direct navigation, no pop. Back from `GameScreen` (via the exit dialog) returns to `GameConfigScreen`.
- **`FollowTheSpotRoute → GameScreen`**: mini-game results (`mini_game_p1_score`, `mini_game_p2_score`) are written to the previous back stack entry's `SavedStateHandle` before calling `popBackStack()`.
- **`HotPotatoRoute → GameScreen`**: the loser's name (`hot_potato_loser`) is written to `SavedStateHandle`. A dedicated `LaunchedEffect` in `GameScreen` reads it and calls `viewModel.onHotPotatoResultReceived(loserName)`. No `MiniGameResult` is shown on the challenge card — punishment is applied directly via the mode handler.

---

## Screen Titles (`NavigationTitles.kt`)

`NavigationTitles` maps route qualified names to string resource IDs. Currently mapped routes:

| Route | String resource |
|---|---|
| `HomeScreen` | `R.string.home_screen` |
| `GameConfigScreen` | `R.string.prepare_your_party` |

Unmapped routes fall back to `R.string.home_screen`.

---

## Removed loading step

`GameConfigScreen` used to navigate to `LoadingScreen`, which showed a 5 s orbit animation with rotating tips before forwarding to `GameScreen` and popping itself. That step was dropped — it delayed the game for no benefit.

`GameScreen` now carries the enter transition that `LoadingScreen` used (`slideInVertically { it } + fadeIn`, 400 ms), so the move from setup into the game looks unchanged.

`LoadingScreen.kt`, its `loading_texts` array, `TripleOrbitLoadingAnimation` and `BlurredAnimatedText` are all **kept** for possible reuse; only the `composable<LoadingScreen>` registration and the route's entry in `isFullScreenRoute` were removed. Re-enabling it means restoring those two blocks.
