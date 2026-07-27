# Game Configuration Screen

`GameConfigScreen` is the setup step between selecting a game mode and starting the game. Players register who is playing, and that is all — there is nothing else to configure.

> **The "Pick what to play" categories section was removed.** Deal types are no longer pre-enabled here; each player picks their own deal at the start of their turn. See [game-deal-flow.md](game-deal-flow.md). `OptionsContainer.kt` and `MiniGamesOptionChip.kt` were deleted along with the `GameOptionsSource.options` list, `initialize()` and `toggle()`.

---

## Layout

The screen is portrait-locked and composed of two regions:

1. **Scrollable content column** — game mode header, ad banner, players section, and the mini-games hint.
2. **Pinned footer** — the Start the Party button, always visible above the navigation bar.

---

## Game Mode Header

Shows the selected mode's name (bold italic, `displayMedium`) and icon (`72 dp`) side by side. Both elements participate in a shared element transition animated from the mode-selection card on `HomeScreen` (`boundsTransform tween 400 ms`). A description string appears below at `bodyLarge` with reduced opacity.

The screen also writes `GameOptionsSource.currentGameModeNameRes` in a `LaunchedEffect(gameModeName)`. That single value is the entire config → game bridge: `GameScreenViewModel` reads it at init to choose its `GameModeHandler` and to tint the game background.

---

## Players Section

**File:** `ui/views/gameConfig/ui/PlayersContainer.kt`

Shows the list of registered players. Each player card supports edit and delete actions. An "Add player" button navigates to `CreatePlayerScreen`. Player cards use a shared element transition keyed by player ID.

---

## Mini-Games Hint Box

A small informational row below the players list. An icon (`ic_lightbulb`) sits in a `primaryContainer` rounded box; the hint text (`mini_games_hint`) explains the phone-passing mechanic.

---

## Start the Party Button

Pinned at the bottom of the screen, above the navigation bar (`navigationBarsPadding`). Enabled only when at least 2 players are registered (`GamePlayersList.PlayersList.size >= 2`).

> The second condition — "at least one category enabled" — was dropped with the categories section. Leaving it in place would have permanently disabled the button, because `GameOptionsSource.options` would never be populated again.

When disabled, the button uses `onSurface.copy(alpha = 0.12f)` background and `onSurface.copy(alpha = 0.38f)` text — the standard Material3 disabled surface treatment. When enabled, pressing the button animates the background and text colors between `primary` ↔ `onPrimary` via `animateColorAsState(tween 300 ms)`.

Tapping calls `GameConfigViewModel.onStartGame(onStartGameClick)`, which sets `isLoading = true`, prepares game state, then invokes the navigation callback. A `CircularProgressIndicator` overlay is shown while `isLoading` is true.

The callback navigates **straight to `GameScreen`**. The intermediate `LoadingScreen` route was removed; `GameScreen` inherited its slide-up enter transition, so the move into the game is visually unchanged. See [navigation.md](navigation.md).

---

## String Resources

| Key | EN | ES |
|---|---|---|
| `prepare_your_party` | "Prepare your party" | *(ES equivalent)* |
| `mode_selected` | "Mode selected" | *(ES equivalent)* |
| `mini_games_hint` | "The phone passes between players each turn — set it on the table, screen up. Tap to flip the prompt, hand it on." | *(ES equivalent)* |
| `start_the_party` | "Start the party" | *(ES equivalent)* |

Removed with the categories section: `pick_what_to_play`, `categories_count_on`, `options_toggle_hint`.

---

## Key Files

| File | Role |
|---|---|
| `ui/views/gameConfig/ui/GameConfigScreen.kt` | Root layout, loading overlay, `StartGameButton`, `GameConfigSectionLabel` |
| `ui/views/gameConfig/ui/PlayersContainer.kt` | Player list + add-player affordance |
| `ui/views/gameConfig/ui/PlayerDataCard.kt` | Individual player card with edit/delete |
| `ui/views/gameConfig/GameConfigViewModel.kt` | `onStartGame`, `deletePlayer`; exposes `GameConfigState` |
| `data/local/appData/appDataSource/GameOptionsSource.kt` | Singleton holding `currentGameModeNameRes` |
| `data/local/appData/appDataSource/GamePlayersList` | Singleton holding the registered player list |

---

## Related

- [navigation.md](navigation.md) — `GameConfigScreen` route parameters and transitions
- [game-mode-visual-identity.md](game-mode-visual-identity.md) — shared element animation for the mode header icon and name
- [game-deal-flow.md](game-deal-flow.md) — where deal selection now happens
- [minigames.md](minigames.md) — mini-game system
