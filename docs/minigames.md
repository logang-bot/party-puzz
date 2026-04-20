# Mini-Games

Mini-games are short competitive sub-games that can appear as a deal type inside the main game loop. They are launched as full-screen routes from `GameScreen` and return a result that the active game mode handler can use to trigger rewards or punishments.

---

## System Overview

### `MiniGame` enum

Defined in `MiniGame.kt`. Each entry declares:

| Property | Type | Purpose |
|---|---|---|
| `nameRes` | `@StringRes Int` | Displayed in the challenge card and result UI |
| `minPlayers` | `Int` | Minimum players required for this mini-game to be eligible in a deal |
| `isGlobal` | `Boolean` | `true` = all players participate; `false` = challenger vs selected opponent |

Current entries:

| Entry | `minPlayers` | `isGlobal` |
|---|---|---|
| `FOLLOW_THE_SPOT` | 2 | `false` |
| `HOT_POTATO` | 2 | `true` |

### How a mini-game deal is triggered

1. `GameScreenViewModel.onGameDealTapped()` randomly picks `GameDealType.MINI_GAME`.
2. Any `MiniGame` entry whose `minPlayers ≤ players.size` is eligible.
3. `dealPhase` advances to `CHALLENGE_SHOWN` and `miniGame` is set in `GameScreenState`.
4. `MiniGameChallengeContent` is rendered on the challenge card.

### Two-player vs Global flow

**Two-player (`isGlobal = false`):**
```
MiniGameChallengeContent shows opponent list
         │
  user selects opponent
         │
  "Go" button tapped → onMiniGameOpponentSelected(opponent)
         │
  GameScreen calls onNavigateToMiniGame(miniGame, challenger, opponent)
         │
  Mini-game route launched
         │
  onGameFinished(p1Score, p2Score) → SavedStateHandle
  ["mini_game_p1_score", "mini_game_p2_score"]
         │
  GameScreen reads scores → onMiniGameResultReceived(p1, p2)
         │
  MiniGameResult shown on challenge card → mode handler applies reward/punishment
```

**Global (`isGlobal = true`):**
```
MiniGameChallengeContent shows global description + Start button
         │
  "Start" tapped → onGlobalMiniGameStarted()
         │
  GameScreen calls onNavigateToGlobalMiniGame(miniGame)
         │
  Mini-game route launched (reads all players from GamePlayersList directly)
         │
  onGameFinished(loserName) → SavedStateHandle ["hot_potato_loser"]
         │
  GameScreen reads loserName → onHotPotatoResultReceived(loserName)
         │
  Mode handler applies punishment to the loser; deal resets to IDLE
  (or challenge card flips to show mode event if in Bar / Couples mode)
```

> Global mini-games bypass `MiniGameResult` — there is no winner/loser score card shown on the `GameScreen` challenge card. The result is shown entirely on the mini-game's own screen.

---

## Shared Components

### `MiniGameCountdownOverlay`

**File:** `ui/views/game/common/MiniGameCountdownOverlay.kt`

Reusable full-screen countdown overlay used by all mini-games that have a pre-game countdown. Renders on top of the game content via `AnimatedVisibility`.

| Prop | Type |
|---|---|
| `countdownValue` | `Int` — current value (≥1 shows number, 0 shows "Go!") |

**Visual design:**
- Background: `Brush.verticalGradient` with soft white alpha (frosted-glass look)
- Top specular edge: 1 dp horizontal gradient, transparent → white → transparent
- Bottom specular edge: 1 dp solid white at 30 % opacity
- Text color: `MaterialTheme.colorScheme.onPrimaryContainer` with a white `Shadow`

**Transitions (inside `AnimatedContent`):**

| Tick | Enter | Exit |
|---|---|---|
| Number (≥1 → ≥1) | `slideInVertically { -it } + fadeIn` (220ms) | `slideOutVertically { it } + fadeOut` (220ms) |
| Go (1 → 0) | `scaleIn(initialScale=0.5f) + fadeIn` (350ms) | `scaleOut(targetScale=1.5f) + fadeOut` (250ms) |

---

## Follow The Spot

A 2-player reaction game. Both players hold the phone at opposite ends. A spot appears somewhere on each player's half — the first to tap it scores a point. The player with the most taps after 10 seconds wins.

### Mechanics

- Screen is split horizontally: Player 2 on top (rotated 180°), Player 1 on bottom.
- A coloured circle (the "spot") appears at a random position on each player's half.
- Tapping the spot moves it to a new random position and scores 1 point.
- A 3-second countdown precedes the game; during countdown both halves are blurred.
- A cycling border animation (4 pastel colours, 600 ms per step) runs while the game is active.
- When time runs out `isGameRunning` becomes `false`; the divider shows "Tap to exit".

### State model (`FollowTheSpotState`)

| Field | Purpose |
|---|---|
| `player1` / `player2` | `Player?` built from route args |
| `player1Score` / `player2Score` | Tap counts |
| `timeRemaining` | Seconds left (counts down from 10) |
| `player1SpotNormX/Y` / `player2SpotNormX/Y` | Normalised 0..1 coordinates |
| `isGameRunning` | `true` between Go and time-up |
| `isCountingDown` | `true` during the 3-2-1-Go sequence |
| `countdownValue` | 3 → 0 |

### Result

`onGameFinished(player1Score, player2Score)` writes both scores to `SavedStateHandle`. `GameScreenViewModel.onMiniGameResultReceived` constructs a `MiniGameResult` and the mode handler applies reward or punishment.

### Key files

| File | Role |
|---|---|
| `FollowTheSpotScreen.kt` | Screen + `FollowTheSpotContent` composable |
| `SpotBoard.kt` | Single player half: tappable spot, border animation, `DividerEdge` enum |
| `GameDivider.kt` | Centre strip: timer progress bar, player chips, scores, "Tap to exit" |
| `FollowTheSpotState.kt` | State data class |
| `FollowTheSpotViewModel.kt` | Countdown + game timer jobs, spot randomisation |
| `FollowTheSpotRoute` (`HomeScreenRoutes.kt`) | `data class` carrying both players' display info |

---

## Hot Potato

A global mini-game (all registered players participate). The phone represents the "hot potato" — players physically pass it around, each tapping the screen to confirm the pass. A hidden random timer (10–30 s) fires silently; whoever is showing on screen at that moment has the potato and must drink.

### Mechanics

- All players read from `GamePlayersList.PlayersList` at ViewModel init — no route args needed.
- A random starting holder is selected.
- 3-second countdown before the game starts (`MiniGameCountdownOverlay`).
- Full-screen layout: current holder shown large with avatar, name, and 🥔 emoji.
- Below the main holder: a small "Next up" row showing the next player's avatar and name.
- The player physically passes the phone; the new holder taps anywhere to advance to their name.
- A cycling border animation (same 4 pastel colours, 600 ms per step) runs while active.
- When the hidden timer fires: `isGameRunning = false`, `loserIndex` set → BOOM screen shown.
- BOOM screen: 💥 emoji, loser name, drink prompt, "Tap to dismiss".
- On dismiss: `onGameFinished(loserName)` → returns to `GameScreen`.

### Hidden timer

Duration is chosen once at game start with `Random.nextInt(MIN, MAX + 1)`:

| Constant | Value |
|---|---|
| `MIN_GAME_SECONDS` | 10 |
| `MAX_GAME_SECONDS` | 30 |

The timer value is intentionally **never shown** to players.

### State model (`HotPotatoState`)

| Field | Type | Purpose |
|---|---|---|
| `players` | `List<Player>` | Full player roster, copied from `GamePlayersList` at init |
| `currentHolderIndex` | `Int` | Index into `players`; advances on each tap |
| `isGameRunning` | `Boolean` | `true` between Go and explosion |
| `isCountingDown` | `Boolean` | `true` during 3-2-1-Go |
| `countdownValue` | `Int` | 3 → 0 |
| `loserIndex` | `Int?` | Set when timer fires; `null` while game is running |
| `currentHolder` *(computed)* | `Player?` | `players[currentHolderIndex]` |
| `nextHolder` *(computed)* | `Player?` | `players[(currentHolderIndex + 1) % size]`; `null` if only 1 player |
| `loser` *(computed)* | `Player?` | `players[loserIndex]` |

### Result passing

Unlike 2-player mini-games, Hot Potato does not use `MiniGameResult`. The loser name is passed back via `SavedStateHandle`:

```
onGameFinished(loserName)
    → savedStateHandle["hot_potato_loser"] = loserName
    → GameScreen LaunchedEffect picks up the value
    → viewModel.onHotPotatoResultReceived(loserName)
    → modeHandler.applyPunishment(state, loserPlayer)
    → if no mode event pending → resetDeal()
```

In Bar Time / Couples / Party Puzz modes the challenge card flips to show the punishment event before resetting.

### Key files

| File | Role |
|---|---|
| `HotPotatoScreen.kt` | Screen + `HotPotatoContent` composable; border animation logic |
| `HotPotatoPlayerSide.kt` | `HotPotatoHolderCard` composable: active state, next-player row, BOOM state |
| `HotPotatoState.kt` | State data class + computed properties |
| `HotPotatoViewModel.kt` | Countdown job, hidden timer job, `onPassTapped()` |
| `HotPotatoRoute` (`HomeScreenRoutes.kt`) | `data object` — no route args; all player data read from `GamePlayersList` |
| `MiniGameCountdownOverlay.kt` | Shared countdown overlay (see above) |

---

## Adding a New Mini-Game

1. Add an entry to `MiniGame.kt` with `nameRes`, `minPlayers`, and `isGlobal`.
2. Create a `miniGames/<name>/` package with `<Name>State.kt`, `<Name>ViewModel.kt`, `<Name>Screen.kt`.
3. Add a `@Serializable` route to `HomeScreenRoutes.kt`:
   - Two-player: `data class` carrying player display info.
   - Global: `data object` (ViewModel reads `GamePlayersList` directly).
4. In `HomeNavigation.kt`:
   - Add the route to `isFullScreenRoute`.
   - Add a `composable<XxxRoute>` block that handles `onGameFinished` / `onAbortGame`.
   - Wire navigation in `onNavigateToMiniGame` (two-player) or `onNavigateToGlobalMiniGame` (global).
5. In `GameScreen.kt`, handle the new result key in a `LaunchedEffect` if the global pattern is used.
6. In `GameScreenViewModel.kt`, add a result handler function if needed.
7. Add string resources to `values/strings.xml` and `values-es/strings.xml`.
8. Use `MiniGameCountdownOverlay` for the pre-game countdown.

---

## Related

- [game-deal-flow.md](game-deal-flow.md) — Full deal phase sequence and challenge card layout
- [game-mode-handler.md](game-mode-handler.md) — How results trigger rewards and punishments
- [navigation.md](navigation.md) — Route definitions and back-stack result passing
