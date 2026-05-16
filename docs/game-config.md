# Game Configuration Screen

`GameConfigScreen` is the setup step between selecting a game mode and starting the game. Players configure which question categories to include and who is playing.

---

## Layout

The screen is portrait-locked and composed of two regions:

1. **Scrollable content column** — game mode header, categories section, players section, and the mini-games hint.
2. **Pinned footer** — the Start the Party button, always visible above the navigation bar.

---

## Game Mode Header

Shows the selected mode's name (bold italic, `displayMedium`) and icon (`72 dp`) side by side. Both elements participate in a shared element transition animated from the mode-selection card on `HomeScreen` (`boundsTransform tween 400 ms`). A description string appears below at `bodyLarge` with reduced opacity.

---

## "Pick What to Play" — Categories Section

**File:** `ui/views/gameConfig/ui/OptionsContainer.kt`

A self-contained section that owns its own header row, subtitle, and interactive grid. No external section label is rendered for this section.

### Header row

| Element | Style | Notes |
|---|---|---|
| Label: "PICK WHAT TO PLAY" | `labelSmall`, `1.5 sp` letter-spacing, 50 % opacity | Uses `GameConfigSectionLabel` composable |
| Counter: `X/Y on` | `labelSmall`, `primary` color | Reads live from `GameOptionsSource.options`; recomposes on each toggle |

Below the header: a subtitle hint ("Tap to toggle. Off categories are skipped for tonight.") at `bodySmall`, 50 % opacity.

### Card grid

Options are rendered as a 2-column grid. Each row is a `Row` with `8 dp` spacing; rows are spaced `8 dp` apart.

#### `OptionCard`

Standard togglable category card.

| State | Border | Background | Status label | Toggle circle |
|---|---|---|---|---|
| Enabled | `primary` (1 dp) | `surfaceContainer` | "INCLUDED" in `primary` | Filled `primary` circle + `onPrimary` check icon |
| Disabled | `outlineVariant` (1 dp) | `surfaceContainer` | "TAP TO ADD" at 40 % `onSurface` | Outlined circle (`outlineVariant`) |

All animated with `animateColorAsState(tween 250 ms)`.

#### `MiniGamesOptionCard`

Same card layout as `OptionCard`, shared via the internal `OptionCardContent` composable. The difference is the border rendering: on first composition a sweep-gradient border (`0xFF00E5FF → 0xFFD500F9 → 0xFFFF6D00`) rotates 360° over 3 s via `Animatable`, then crossfades to a solid `outlineVariant` / `primary` border over 600 ms. This is a one-shot onboarding animation that does not repeat.

The card background fill (`surfaceContainer`) is drawn explicitly inside `drawWithContent` to cover the gradient layer behind the content area.

> The gradient border colors (`0xFF00E5FF`, `0xFFD500F9`, `0xFFFF6D00`) are intentional decorative accents — not Material3 theme tokens. They are not theme-dependent by design.

### Option definitions

Defined as a private `optionDefinitions` list in `OptionsContainer.kt`:

| Label resource | Default state |
|---|---|
| `truth_or_dare` | enabled |
| `general_knowledge_title` | enabled |
| `sticky_dares` | enabled |
| `mini_games` | disabled |

`GameOptionsSource.initialize()` is called once in `LaunchedEffect(Unit)` to sync the global state list with these defaults. Toggling a card calls `GameOptionsSource.toggle(labelRes)` to update the shared state (used by `GameScreenViewModel` at game start).

### State management

`GameOptionsSource` is a singleton `object` holding a `SnapshotStateList<GameOption>`. Reading `.count { it.enabled }` inside the composable is sufficient for the counter to recompose automatically on each toggle — no additional `State` wrapper is needed.

---

## Players Section

**File:** `ui/views/gameConfig/ui/PlayersContainer.kt`

Shows the list of registered players. Each player card supports edit and delete actions. An "Add player" button navigates to `CreatePlayerScreen`. Player cards use a shared element transition keyed by player ID.

---

## Mini-Games Hint Box

A small informational row below the players list. An icon (`ic_lightbulb`) sits in a `primaryContainer` rounded box; the hint text (`mini_games_hint`) explains the phone-passing mechanic. Rendered unconditionally — not tied to whether mini-games are enabled.

---

## Start the Party Button

Pinned at the bottom of the screen, above the navigation bar (`navigationBarsPadding`). Enabled only when:

- At least 2 players are registered (`GamePlayersList.PlayersList.size >= 2`)
- At least 1 category option is enabled (`GameOptionsSource.options.any { it.enabled }`)

When disabled, the button uses `onSurface.copy(alpha = 0.12f)` background and `onSurface.copy(alpha = 0.38f)` text — the standard Material3 disabled surface treatment. When enabled, pressing the button animates the background and text colors between `primary` ↔ `onPrimary` via `animateColorAsState(tween 300 ms)`.

Tapping calls `GameConfigViewModel.onStartGame(onStartGameClick)`, which sets `isLoading = true`, prepares game state, then invokes the navigation callback. A `CircularProgressIndicator` overlay is shown while `isLoading` is true.

---

## String Resources

| Key | EN | ES |
|---|---|---|
| `pick_what_to_play` | "Pick what to play" | "Elige qué jugar" |
| `included` | "Included" | "Incluido" |
| `tap_to_add` | "Tap to add" | "Toca para añadir" |
| `categories_count_on` | `%1$d/%2$d on` | `%1$d/%2$d activas` |
| `options_toggle_hint` | "Tap to toggle. Off categories are skipped for tonight." | "Toca para activar o desactivar. Las categorías desactivadas se omiten esta noche." |
| `mini_games_hint` | "The phone passes between players each turn — set it on the table, screen up. Tap to flip the prompt, hand it on." | *(ES equivalent)* |
| `prepare_your_party` | "Prepare your party" | *(ES equivalent)* |
| `mode_selected` | "Mode selected" | *(ES equivalent)* |
| `start_the_party` | "Start the party" | *(ES equivalent)* |

---

## Key Files

| File | Role |
|---|---|
| `ui/views/gameConfig/ui/GameConfigScreen.kt` | Root layout, loading overlay, `StartGameButton`, `GameConfigSectionLabel` |
| `ui/views/gameConfig/ui/OptionsContainer.kt` | `OptionsContainer`, `OptionCard`, `OptionCardContent`, `OptionToggleCircle` |
| `ui/views/gameConfig/ui/MiniGamesOptionChip.kt` | `MiniGamesOptionCard` — card with one-shot animated gradient border |
| `ui/views/gameConfig/ui/PlayersContainer.kt` | Player list + add-player affordance |
| `ui/views/gameConfig/ui/PlayerDataCard.kt` | Individual player card with edit/delete |
| `ui/views/gameConfig/GameConfigViewModel.kt` | `onStartGame`, `deletePlayer`; exposes `GameConfigState` |
| `data/local/appData/appDataSource/GameOptionsSource.kt` | Singleton holding the enabled/disabled state of each category option |
| `data/local/appData/appDataSource/GamePlayersList` | Singleton holding the registered player list |

---

## Related

- [navigation.md](navigation.md) — `GameConfigScreen` route parameters and transitions
- [game-mode-visual-identity.md](game-mode-visual-identity.md) — shared element animation for the mode header icon and name
- [minigames.md](minigames.md) — mini-game system and how the "Mini-games" category option connects to deal generation
