# Mini-Games

Mini-games are short competitive sub-games that can appear as a deal type inside the main game loop. They are launched as full-screen routes from `GameScreen` and return a result that the active game mode handler can use to trigger rewards or punishments.

---

## System Overview

### `MiniGame` enum

Defined in `MiniGame.kt`. Each entry declares:

| Property | Type | Purpose |
|---|---|---|
| `nameRes` | `@StringRes Int` | Displayed in the challenge card and result UI |
| `descriptionRes` | `@StringRes Int` | Short one-sentence blurb shown on the deal card (above the opponent selector for two-player games; as the main body for global games) |
| `minPlayers` | `Int` | Minimum players required for this mini-game to be eligible in a deal |
| `isGlobal` | `Boolean` | `true` = all players participate; `false` = challenger vs selected opponent |

Current entries:

| Entry | `minPlayers` | `isGlobal` |
|---|---|---|
| `FOLLOW_THE_SPOT` | 2 | `false` |
| `HOT_POTATO` | 2 | `true` |
| `TAP_WAR` | 2 | `false` |
| `SIMON_SAYS` | 2 | `true` |
| `CIRCLE_MASTER` | 2 | `true` |

### How a mini-game deal is triggered

0. The **Mini-games** question pack has to be enabled on the setup screen. It is the one pack with no question rows — mini-games are code, not prompts — so `QuestionPackContentLoader` checks its enabled flag directly and reports it as `EnabledPackContent.hasMiniGames`. With the pack off, `MINI_GAME` is dropped from `availableDealTypes` and never appears on the picker or the reel. See [question-packs.md](question-packs.md).
1. The player picks **Mini-games** on the deal picker (or the "Surprise me" reel lands on it), and `GameScreenViewModel.onDealChosen()` starts a `GameDealType.MINI_GAME` challenge.
2. Any `MiniGame` entry whose `minPlayers ≤ players.size` is eligible.
3. `dealPhase` advances to `CHALLENGE_SHOWN` and `miniGame` is set in `GameScreenState`.
4. `MiniGameChallengeContent` is rendered on the challenge card.

### Two-player vs Global flow

Both flows funnel through the same `miniGameResult` state on `GameScreenState` and are dismissed by the same `onMiniGameDealFinished` trigger. `MiniGameResult` is a sealed interface with one variant per result shape:

```kotlin
sealed interface MiniGameResult
data class ScoredMiniGameResult(p1Name, p1Score, p2Name, p2Score) : MiniGameResult
data class LoserMiniGameResult(loserName) : MiniGameResult
```

**Two-player (`isGlobal = false`):**
```
MiniGameChallengeContent shows description + opponent list
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
  ScoredMiniGameResult stored in state.miniGameResult
         │
  ScoredResultContent panel shown on challenge card
         │
  user taps Finish (mode active) or the card (standard) → onMiniGameDealFinished()
         │
  modeHandler.applyMiniGameResult → reward / punishment / no-op + deal reset
```

**Global (`isGlobal = true`):**
```
MiniGameChallengeContent shows large title + "Everyone plays!" + description + Start button
         │
  "Start" tapped → onGlobalMiniGameStarted()
         │
  GameScreen calls onNavigateToGlobalMiniGame(miniGame)
         │
  Mini-game route launched (reads all players from GamePlayersList directly)
         │
  onGameFinished(loserName) → SavedStateHandle [game-specific key]
  (e.g. "hot_potato_loser", "simon_says_loser")
         │
  GameScreen LaunchedEffect reads loserName → game-specific ViewModel handler
         │
  LoserMiniGameResult stored in state.miniGameResult
         │
  LoserResultContent panel shown on challenge card (shows "Loser: <name>")
         │
  user taps Finish (mode active) or the card (standard) → onMiniGameDealFinished()
         │
  modeHandler.applyMiniGameResult → punishment for the loser + deal reset
  (in Bar / Couples / Party Puzl the card flips to the mode event first)
```

Global and two-player mini-games share the same state field, the same result-panel scaffolding (`MiniGameChallengeContent`), and the same dismiss trigger (`onMiniGameDealFinished`). The two variants of `MiniGameResult` differ only in which payload they carry and in how each mode handler decides who to punish or reward (see [game-mode-handler.md](game-mode-handler.md)).

---

## Challenge-card presentation

`MiniGameChallengeContent` is the single composable that renders everything the mini-game needs on the challenge card — before, during navigation, and after the result comes back. Its layout reacts to two flags:

| Flag | Meaning |
|---|---|
| `result == null` | Pre-game state: show the description body + the Start / Go affordance |
| `miniGame.isGlobal` | Drives header size and the "Everyone plays!" subtitle |

**Header treatment** (`MiniGameHeader`):

| Situation | Treatment |
|---|---|
| Global mini-game, no result yet | `headlineLarge` bold title + `titleMedium` "Everyone plays!" subtitle (resource: `mini_game_everyone_plays`) |
| Two-player mini-game, no result yet | Small `titleMedium` title (same styling the result panel uses) |
| Any mini-game with a result set | Small `titleMedium` title — the result text takes visual priority |

**Body (result == null):**

- Global: `GlobalMiniGameContent` — renders `stringResource(miniGame.descriptionRes)` and a bottom-anchored `Start` button.
- Two-player: `OpponentSelectionContent` — renders the description, the selected (current) player's name, and a list of opponent buttons. The `Go` button appears anchored at the bottom once an opponent is picked.

**Body (result set):**

- `ScoredMiniGameResult` → `ScoredResultContent` (winner / tie headline + both scores)
- `LoserMiniGameResult` → `LoserResultContent` (`"Loser: <name>"` headline; resource: `mini_game_loser`)

Both result panels end with `ResultDismissAction`, which swaps between a `Finish` button (when any game mode is active) and a "Tap to dismiss" hint (Standard mode).

---

## Shared Components

### `MiniGameCountdownOverlay`

**File:** `ui/views/game/common/MiniGameCountdownOverlay.kt`

Reusable full-screen countdown overlay used by all mini-games that have a pre-game countdown. Renders on top of the game content via `AnimatedVisibility`.

| Prop | Type |
|---|---|
| `countdownValue` | `Int` — current value (≥1 shows number, 0 shows "Go!") |

**Visual design** — the frosted panel is theme-aware, via `appColors.glassTint` / `glassEdge` / `onGlass` (see [theming.md](theming.md)). A white wash works over the dark theme but is invisible over the light theme's cream, so light mode uses a heavier white with dark ink instead:

- Background: `Brush.verticalGradient` from `glassTint`, keeping a 1 : 0.45 : 0.73 alpha falloff — white @ 22 % in dark, white @ 62 % in light
- Top specular edge: 1 dp horizontal gradient, transparent → `glassEdge` → transparent
- Bottom specular edge: 1 dp `glassEdge` at 35 % of its alpha
- Text colour: `onGlass` (white in dark, `#0E2630` in light), with a `Shadow` in the same colour for the glow

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

`onGameFinished(player1Score, player2Score)` writes both scores to `SavedStateHandle`. `GameScreenViewModel.onMiniGameResultReceived` constructs a `ScoredMiniGameResult` and stores it in `miniGameResult`; when the user dismisses the result panel, `onMiniGameDealFinished()` calls `modeHandler.applyMiniGameResult` which resolves winner → reward, loser → punishment, tie → no event.

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

## Tap War

A 2-player tug-of-war game. The screen is split horizontally (portrait, same orientation as Follow the Spot). A progress bar starts centred; each tap on your half pushes it toward the opponent's edge. The player whose side the bar reaches first loses.

### Mechanics

- Screen is split: Player 2 on top (rotated 180°), Player 1 on bottom.
- A single `barPosition: Float` in `[0.0, 1.0]` represents the bar position: 0.0 = Player 1 wins, 1.0 = Player 2 wins; starts at 0.5.
- Each tap on Player 1's half applies `+BAR_STEP` (0.04); each tap on Player 2's half applies `−BAR_STEP`. Position is clamped to `[0, 1]`.
- When `barPosition ≥ 1.0` Player 2 wins; when `barPosition ≤ 0.0` Player 1 wins — `winner: Int?` is set accordingly and the game stops.
- A 3-second countdown precedes the game; during countdown taps are ignored.
- A 10-second game timer runs after the countdown. If neither player pushes the bar to an edge within time, the player with the bar on their side at expiry loses (bar > 0.5 → Player 1 wins; bar < 0.5 → Player 2 wins; exactly 0.5 → tie).
- A cycling border animation (4 pastel colours, 600 ms per step) runs while the game is active.
- Player 2's inner content (photo + name) is rotated 180° via `isFlipped = true`.

### Divider strip (`TugOfWarBar`)

The centre strip has two visual layers stacked in a `Box`:

1. **Autoconsumable progress bar** (background layer) — a `Box` with `fillMaxWidth(timerFraction)` and `fillMaxHeight()` filled with `surfaceVariant`. The fraction is driven by an `Animatable` updated via `LaunchedEffect(timeRemaining, isGameRunning)`: it snaps to the current second's fraction and smoothly animates to the next over 1 000 ms with `LinearEasing`, giving a continuous drain effect. When `isGameRunning = false` (bar win or timeout) it snaps to 0.

2. **Indicator row** (foreground layer) — a `Row` containing a `PlayerAvatar` on each side and an `IndicatorTrack` in the centre. `IndicatorTrack` uses two `Spacer`s weighted by `barPosition` and `1 − barPosition` with a 20 dp circle indicator at their boundary; `animateFloatAsState` with a spring drives smooth motion. The row fades to 0 alpha when the game ends; "Tap to exit" fades in over it.

### State model (`TapWarState`)

| Field | Purpose |
|---|---|
| `player1` / `player2` | `Player?` built from route args |
| `barPosition` | `Float` in `[0.0, 1.0]`; starts at 0.5 |
| `isGameRunning` | `true` between Go and game end |
| `isCountingDown` | `true` during the 3-2-1-Go sequence |
| `countdownValue` | 3 → 0 |
| `timeRemaining` | Seconds left (counts down from 10 once game starts) |
| `winner` | `Int?` — `1` or `2` once the bar reaches an edge or time expires; `null` while in play |

### Result

`onGameFinished(p1Score, p2Score)` writes both scores to `SavedStateHandle` using the shared `mini_game_p1_score` / `mini_game_p2_score` keys (same as Follow the Spot). `GameScreenViewModel.onMiniGameResultReceived` constructs a `ScoredMiniGameResult`.

### Key files

| File | Role |
|---|---|
| `TapWarScreen.kt` | Screen + `TapWarContent` composable |
| `TapWarSide.kt` | Single player half: tappable zone, border animation, `TapWarDividerEdge` enum |
| `TugOfWarBar.kt` | Divider strip: autoconsumable timer progress bar + animated indicator track, player avatars, "Tap to exit" |
| `TapWarState.kt` | State data class |
| `TapWarViewModel.kt` | Countdown job, timer job, tap handlers (`onPlayer1Tapped`, `onPlayer2Tapped`), bar update, time-based win resolution |
| `TapWarRoute` (`HomeScreenRoutes.kt`) | `data class` carrying both players' display info |

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
- BOOM screen: 💥 emoji, "BOOM!" headline, "Tap to dismiss" hint. The loser's name and the drink prompt are intentionally **not** shown here — the result panel back on `GameScreen` owns that presentation.
- On dismiss: `onGameFinished(loserName)` → returns to `GameScreen`, which shows the `LoserResultContent` panel (and the mode event, if applicable).

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

Hot Potato uses the same `MiniGameResult` plumbing as Follow The Spot, but packaged as `LoserMiniGameResult(loserName)`. The loser name is handed back via `SavedStateHandle`:

```
onGameFinished(loserName)
    → savedStateHandle["hot_potato_loser"] = loserName
    → GameScreen LaunchedEffect picks up the value
    → viewModel.onHotPotatoResultReceived(loserName)
    → state.miniGameResult = LoserMiniGameResult(loserName)
    → MiniGameChallengeContent renders LoserResultContent panel
    → user taps Finish (mode active) / card (standard) → onMiniGameDealFinished()
    → modeHandler.applyMiniGameResult(state) → punishment event (if any) + deal reset
```

In Bar Time / Couples / Party Puzl modes the challenge card flips to show the punishment event before resetting; in Standard mode the deal resets silently.

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

## Simon Says

A global mini-game (all registered players participate). The phone is passed around. A growing sequence of 4 coloured buttons is shown and highlighted one at a time. The active player must tap the same sequence back. Each round the sequence grows by one button. The first player to tap incorrectly loses and drinks.

### Mechanics

- All players read from `GamePlayersList.PlayersList` at ViewModel init — no route args needed.
- 3-second countdown before the first round (`MiniGameCountdownOverlay`).
- Four colour buttons in a 2×2 grid: green (0), red (1), blue (2), yellow (3).
- Each round: sequence is replayed button by button (SHOWING phase), then the current player taps back in order (INPUT phase).
- On correct completion of the full sequence → PASS phase: the footer shows the next player's photo + "Pass the phone to [name] and tap here when ready!" The entire footer area is tappable (no explicit button). Tapping advances `currentPlayerIndex` and starts the next round with one more button appended.
- On incorrect tap → GAME_OVER phase: `loser` is set to the current player.
- Players cycle in order: `currentPlayerIndex = (current + 1) % players.size`.

### Phases (`SimonSaysPhase`)

| Phase | Description |
|---|---|
| `COUNTDOWN` | Pre-game 3-2-1-Go overlay; footer renders an invisible placeholder to keep layout stable |
| `SHOWING` | Sequence highlighted button by button (600 ms on, 200 ms gap); footer shows player photo + "Hey [name], watch carefully!" |
| `INPUT` | Active player taps back the sequence; header shows `"Round N • X/N"` tally; footer shows player photo + "Your turn, [name]!" |
| `PASS` | Sequence completed; footer shows next player's photo + pass instruction; entire footer is tappable to confirm |
| `GAME_OVER` | Incorrect tap; buttons scatter off-screen; footer shows loser photo + "Oops, [name] missed it!"; "Tap to exit" fades into the grid area after 300 ms |

### Highlight timing

| Constant | Value |
|---|---|
| `BUTTON_HIGHLIGHT_MS` | 600 ms |
| `BUTTON_GAP_MS` | 200 ms |

### UI design

**Layout:**
- Header (`RoundHeader`): round number centered (`titleLarge` bold). During INPUT, a secondary tally `"• X/N"` (`labelLarge`, `onSurfaceVariant`) is appended inline to show progress through the sequence.
- Grid (`SimonGrid`): 2×2 `Box` of `SimonButton` composables filling the remaining weight.
- Footer (`PhaseFooter`): flexible height; always renders a `PlayerInfo` (64 dp circle photo + `headlineSmall` message) or an invisible placeholder during COUNTDOWN to prevent the grid from jumping.

**`SimonButton`:**
- Dim state: `baseColor.ink(Ink.Faint)`; highlighted state: `baseColor` (full opacity). Animates via `animateColorAsState(tween(120))`.
- Tap ripple: expands from tap point to fill the full card using `hypot(width, height)` as max radius; ripple colour = `baseColor` at 50 % alpha fading to 0 over 400 ms.
- Scatter (GAME_OVER): each button translates outward in its corner direction and fades to 0 alpha over 350 ms via `animateFloatAsState` + `graphicsLayer`.

**Game-over overlay:**
- Scoped to the `SimonGrid` area only — header and footer remain fully visible.
- Uses `animateFloatAsState` + `Modifier.alpha()` (300 ms delay, 400 ms fade-in) to avoid `ColumnScope.AnimatedVisibility` receiver conflicts.
- Style: `titleMedium` + `FontWeight.Bold` + `onSurfaceVariant` — matches the "Tap to exit" style used in `GameDivider` (Follow the Spot) and `TugOfWarBar` (Tap War).

### State model (`SimonSaysState`)

| Field | Type | Purpose |
|---|---|---|
| `players` | `List<Player>` | Full roster, copied from `GamePlayersList` at init |
| `currentPlayerIndex` | `Int` | Index into `players`; advances on each pass |
| `sequence` | `List<Int>` | 0–3 values; grows by one per round |
| `playerInputIndex` | `Int` | Position in `sequence` being validated during INPUT |
| `phase` | `SimonSaysPhase` | Drives UI layout |
| `highlightedButton` | `Int` | Button index lit during SHOWING; `-1` when none |
| `countdownValue` | `Int` | 3 → 0 |
| `loser` | `Player?` | Set on incorrect tap |
| `currentPlayer` *(computed)* | `Player?` | `players.getOrNull(currentPlayerIndex)` |
| `roundNumber` *(computed)* | `Int` | `sequence.size` |

### Result passing

Simon Says uses a dedicated `SavedStateHandle` key (`simon_says_loser`) so it does not collide with the Hot Potato key:

```
onGameFinished(loserName)
    → savedStateHandle["simon_says_loser"] = loserName
    → GameScreen LaunchedEffect picks up the value
    → viewModel.onSimonSaysResultReceived(loserName)
    → state.miniGameResult = LoserMiniGameResult(loserName)
    → MiniGameChallengeContent renders LoserResultContent panel
    → user taps Finish (mode active) / card (standard) → onMiniGameDealFinished()
    → modeHandler.applyMiniGameResult(state) → punishment event (if any) + deal reset
```

### Key files

| File | Role |
|---|---|
| `SimonSaysScreen.kt` | Screen + `SimonSaysContent`, `RoundHeader`, `PlayerInfo`, `SimonGrid`, `PhaseFooter` |
| `SimonButton.kt` | Individual colour button with tap ripple and scatter animation; `SimonColors` list (green, red, blue, yellow); `ScatterDirs` per-corner offsets |
| `SimonSaysState.kt` | State data class + `SimonSaysPhase` enum + computed properties |
| `SimonSaysViewModel.kt` | Countdown job, sequence show job, tap validation, pass/game-over logic |
| `SimonSaysRoute` (`HomeScreenRoutes.kt`) | `data object` — no route args; all player data read from `GamePlayersList` |
| `MiniGameCountdownOverlay.kt` | Shared countdown overlay (see above) |

---

## Circle Master

A global mini-game (all registered players participate). Each player takes a turn drawing a circle around a fixed center dot on screen. When they lift their finger, a score (0–100 %) is computed based on how close the path is to a perfect circle. After every player has drawn, the one with the lowest score loses and drinks.

### Mechanics

- All players read from `GamePlayersList.PlayersList` at ViewModel init — no route args needed.
- 3-second countdown before the first player's turn (`MiniGameCountdownOverlay`).
- Each player's turn proceeds through three sub-phases: **DRAWING → SCORE_REVEAL → PASS**.
- In **DRAWING**: a red center dot is shown; the player draws by dragging a finger around it. Lifting the finger ends the draw and triggers scoring.
- In **SCORE_REVEAL**: the drawn path is frozen on the canvas and the score percentage is shown. The whole screen is tappable to continue.
- In **PASS** (when more players remain): next player's photo + pass instruction are shown. Tapping advances to the next player's DRAWING turn.
- After the last player draws and acknowledges their score, the game transitions to **WINNER**.
- In **WINNER**: all scores are shown sorted highest-to-lowest, with the winner's name and score highlighted. Tapping the screen returns to `GameScreen`.

### Scoring algorithm

1. Collect touch `Offset` points during the DRAWING phase.
2. If fewer than 20 points were collected, score = 0 (no valid circle).
3. Compute the centroid (mean x, mean y) of the path.
4. Compute each point's Euclidean distance from the centroid (`radii` list).
5. If the average radius < 10 px (essentially a dot), score = 0.
6. Compute standard deviation of the radii.
7. `score = round((1 – clamp(stddev / avgRadius, 0, 1)) × 100)`

Higher variance in the radii → lower score. A perfect circle has zero variance → 100 %.

### Score labels

| Range | Label (EN) |
|---|---|
| 90–100 | `"Flawless!"` |
| 75–89 | `"Pretty round!"` |
| 60–74 | `"Not bad!"` |
| 40–59 | `"Could be rounder…"` |
| 0–39 | `"That's no circle…"` |

### State model (`CircleMasterState`)

| Field | Type | Purpose |
|---|---|---|
| `players` | `List<Player>` | Full roster, copied from `GamePlayersList` at init |
| `currentPlayerIndex` | `Int` | Index into `players`; advances after each pass |
| `scores` | `Map<String, Int>` | `nickName → score` accumulated across turns |
| `phase` | `CircleMasterPhase` | Drives UI layout |
| `countdownValue` | `Int` | 3 → 0 |
| `drawnPath` | `List<Offset>` | Points collected during DRAWING; cleared on each new turn |
| `currentScore` | `Int?` | Score computed at draw-end; `null` during DRAWING |
| `winner` | `Player?` | Highest scorer; set when entering WINNER |
| `loser` | `Player?` | Lowest scorer; passed back as `LoserMiniGameResult` |
| `currentPlayer` *(computed)* | `Player?` | `players[currentPlayerIndex]` |
| `nextPlayer` *(computed)* | `Player?` | `players[currentPlayerIndex + 1]` or `null` |
| `isLastPlayer` *(computed)* | `Boolean` | `currentPlayerIndex >= players.size - 1` |

### Phases (`CircleMasterPhase`)

| Phase | Description |
|---|---|
| `COUNTDOWN` | 3-2-1-Go overlay; canvas is shown below but not interactive |
| `DRAWING` | Active player drags to draw; path collected live |
| `SCORE_REVEAL` | Drawn path frozen; score percentage + label shown; tap anywhere to continue |
| `PASS` | Next player info shown; tap anywhere to start their turn |
| `WINNER` | Sorted scoreboard; tap anywhere to exit and return result |

### Result passing

```
onGameFinished(loserName)
    → savedStateHandle["circle_master_loser"] = loserName
    → GameScreen LaunchedEffect picks up the value
    → viewModel.onCircleMasterResultReceived(loserName)
    → state.miniGameResult = LoserMiniGameResult(loserName)
    → MiniGameChallengeContent renders LoserResultContent panel
    → user taps Finish (mode active) / card (standard) → onMiniGameDealFinished()
    → modeHandler.applyMiniGameResult(state) → punishment event (if any) + deal reset
```

### Key files

| File | Role |
|---|---|
| `CircleMasterScreen.kt` | Screen + `CircleMasterContent`, `DrawingContent`, `PassContent`, `WinnerContent` |
| `CircleCanvas.kt` | Drawing canvas with `detectDragGestures`; renders center dot and live/frozen path |
| `CircleMasterState.kt` | State data class + `CircleMasterPhase` enum + computed properties |
| `CircleMasterViewModel.kt` | Countdown job, `onDrawPoint`, `onDrawEnd`, scoring algorithm, `resolveWinnerLoser` |
| `CircleMasterRoute` (`HomeScreenRoutes.kt`) | `data object` — no route args; all player data read from `GamePlayersList` |
| `MiniGameCountdownOverlay.kt` | Shared countdown overlay |

---

## Adding a New Mini-Game

1. Add an entry to `MiniGame.kt` with `nameRes`, `descriptionRes`, `minPlayers`, and `isGlobal`.
2. Create a `miniGames/<name>/` package with `<Name>State.kt`, `<Name>ViewModel.kt`, `<Name>Screen.kt`.
3. Add a `@Serializable` route to `HomeScreenRoutes.kt`:
   - Two-player: `data class` carrying player display info.
   - Global: `data object` (ViewModel reads `GamePlayersList` directly).
4. In `HomeNavigation.kt`:
   - Add the route to `isFullScreenRoute`.
   - Add a `composable<XxxRoute>` block that handles `onGameFinished` / `onAbortGame`.
   - Wire navigation in `onNavigateToMiniGame` (two-player) or `onNavigateToGlobalMiniGame` (global).
5. In `GameScreen.kt`, handle the new result key in a `LaunchedEffect` if the global pattern is used.
6. In `GameScreenViewModel.kt`, add a result handler that stores an appropriate `MiniGameResult` variant (`ScoredMiniGameResult` or `LoserMiniGameResult`) — reuse existing variants when possible so each mode handler's `applyMiniGameResult` keeps a flat `when` on the sealed interface.
7. Add string resources to `values/strings.xml` and `values-es/strings.xml` — at minimum `<name>_description` for the deal card.
8. Use `MiniGameCountdownOverlay` for the pre-game countdown.

Nothing needs doing on the question-packs side: the Mini-games pack has no rows and the setup screen reports `MiniGame.entries.size`, so a new entry shows up in its prompt count automatically.

---

## String resources

| Key | EN value |
|---|---|
| `follow_the_spot` | `"Follow the spot"` |
| `follow_the_spot_description` | `"Chase the moving spot and tap it — whoever lands the most hits in 10 seconds wins."` |
| `hot_potato` | `"Hot potato"` |
| `hot_potato_description` | `"Pass the phone around — whoever's holding it when it explodes has to drink."` |
| `hot_potato_tap_to_pass` | `"Tap to pass!"` |
| `hot_potato_next` | `"Next"` |
| `hot_potato_boom` | `"BOOM!"` |
| `mini_game_everyone_plays` | `"Everyone plays!"` |
| `mini_game_winner` | `"Winner: %1$s"` |
| `mini_game_tie` | `"It's a tie!"` |
| `mini_game_loser` | `"Loser: %1$s"` |
| `choose_opponent` | `"Choose an opponent"` |
| `start` | `"Start"` |
| `go` | `"Go!"` |
| `finish` | `"Finish"` |
| `tap_to_dismiss` | `"Tap to dismiss"` |
| `tap_war` | `"Tap War"` |
| `tap_war_description` | `"Tap your side as fast as you can — push the bar into your opponent's half to win."` |
| `simon_says` | `"Simon Says"` |
| `simon_says_description` | `"Follow the flashing sequence — whoever breaks it first drinks."` |
| `simon_says_round` | `"Round %1$d"` |
| `simon_says_step` | `"%1$d of %2$d"` (header tally during INPUT) |
| `simon_says_watch_player` | `"Hey %1$s, watch carefully!"` (SHOWING footer) |
| `simon_says_now_your_turn` | `"Your turn, %1$s!"` (INPUT footer) |
| `simon_says_pass_complete` | `"Pass the phone to %1$s and tap here when ready!"` (PASS footer) |
| `simon_says_missed` | `"Oops, %1$s missed it!"` (GAME_OVER footer) |
| `circle_master` | `"Circle Master"` |
| `circle_master_description` | `"Take turns drawing a perfect circle — the shakiest hand drinks."` |
| `circle_master_draw_instruction` | `"Draw a circle around the dot!"` |
| `circle_master_score_perfect` | `"Flawless!"` |
| `circle_master_score_great` | `"Pretty round!"` |
| `circle_master_score_not_bad` | `"Not bad!"` |
| `circle_master_score_try_harder` | `"Could be rounder…"` |
| `circle_master_score_oops` | `"That's no circle…"` |
| `circle_master_pass_to` | `"Pass to %1$s and tap here when ready!"` |
| `circle_master_results` | `"Results"` |

All keys have matching `values-es/strings.xml` entries.

---

## Related

- [game-deal-flow.md](game-deal-flow.md) — Full deal phase sequence and challenge card layout
- [game-mode-handler.md](game-mode-handler.md) — How results trigger rewards and punishments
- [navigation.md](navigation.md) — Route definitions and back-stack result passing
