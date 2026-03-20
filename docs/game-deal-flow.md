# Game Deal Flow

A **game deal** is the full sequence from a player tapping the card to a challenge being shown and dismissed. Each deal selects one player randomly and one of three deal types randomly.

---

## Phase Sequence

```
IDLE ──tap──▶ ANIMATING ──5 s──▶ PLAYER_NAME_REVEAL ──1 s──▶ PLAYER_PHOTO_REVEAL ──1 s──▶ CHALLENGE_SHOWN
                                                                                                    │
                                                                                            user dismisses
                                                                                                    │
                                                                                                  IDLE
```

| Phase | What the main card shows | Duration |
|---|---|---|
| `IDLE` | "Tap to play" | Until user taps |
| `ANIMATING` | Player names cycling rapidly | 5 s |
| `PLAYER_NAME_REVEAL` | Selected player's nickname | 1 s |
| `PLAYER_PHOTO_REVEAL` | Selected player's photo | 1 s |
| `CHALLENGE_SHOWN` | Challenge overlay (deal-type-specific) | Until dismissed |

> The selected player and deal type are both decided **before** the animation starts; the cycling is purely cosmetic.

---

## Deal Types

### 1. Truth or Dare (`TRUTH_OR_DARE`)

**Challenge card — initial state:**
- Title: "Truth or Dare?"
- Two buttons: **Truth** and **Dare**
- Player name anchored to the bottom

**After the player picks an option (flip card animation):**
- Card flips on the Y axis (600 ms, `FastOutSlowInEasing`)
- Back face shows the label (TRUTH / DARE) and a randomly selected question from `strings.xml`
- Player name remains at the bottom
- "Tap to dismiss" hint appears

**Dismissal:** Only available after a choice is made.

**String resources used:**
- `R.array.truth_texts` — truth questions
- `R.array.dare_texts` — dare challenges

---

### 2. Sticky Dare (`STICKY_DARE`)

A dare with a fixed duration. Unlike the other types, dismissing the card does **not** end the challenge — it starts a countdown timer that runs in the background while the game continues.

**Challenge card:**
- Title: "Sticky Dare!"
- Dare text shown immediately (no extra interaction required)
- Player name anchored to the bottom
- "Tap to dismiss" hint

**Dismissal:** Always available (tap anywhere on the card). On dismissal an `ActiveStickyDare` is created and the countdown starts.

#### Post-dismissal: Sticky Dare Pill

A floating pill appears in the top bar (between the exit and info buttons) showing the most recently added active dare:

```
[Name] is [present continuous text] for [original duration label]
```

- Animated in/out with `fadeIn` / `fadeOut` (400 ms)
- Always shows the **latest** active dare (last in list)
- Disappears automatically once that dare's timer reaches zero

#### Post-dismissal: Active Dares Bottom Sheet

There are two entry points to the bottom sheet, each showing a different scope:

| Entry point | Title | Rows shown | Player name shown per row |
|---|---|---|---|
| Tap the **sticky dare pill** | "Active Dares" | All active dares across all players | Yes |
| Tap a **player avatar card** | Player's nickname | Only that player's active dares | No |

Each row shows:

```
● ● ●   [Present continuous text (capitalised)]   [remaining time]
        [Player name]                              ← hidden when filtered to one player
```

- The three bouncing dots are a manual Compose `InfiniteTransition` animation (no GIF)
- Remaining time is formatted as `"X minutes"` / `"1 minute"` / `"X seconds"` / `"1 second"`
- When a dare completes it exits with `shrinkVertically + fadeOut` (350 ms / 300 ms) before being removed from state
- Empty state text differs: `"No active dares right now"` (all-dares view) vs `"No active dares for this player"` (filtered view)

#### Timer lifecycle

- One coroutine per active dare, keyed by `dare.id` in `stickyDareJobs`
- Ticks every second; when `remainingSeconds` reaches 0 it sets `isCompleted = true`, waits 400 ms for the exit animation, then removes the dare from state
- All timers are cancelled in `ViewModel.onCleared()` — firing when the user exits the game screen

**String resources used (3 parallel arrays — indices must stay in sync with `sticky_dares`):**
- `R.array.sticky_dares` — full dare text shown on the challenge card
- `R.array.sticky_dares_present_continuous` — present-continuous form used in the pill and bottom sheet
- `R.array.sticky_dares_duration_labels` — human-readable duration (e.g. `"2 minutes"`)
- `R.array.sticky_dares_duration_seconds` (`integer-array`) — duration in seconds for the countdown

---

### 3. General Knowledge (`GENERAL_KNOWLEDGE`)

A trivia question with exactly two answer options.

**Challenge card — initial state:**
- Title: "General Knowledge"
- Question text
- Two option buttons: **A** and **B**
- Player name anchored to the bottom

**After the player picks an option:**
- Correct option turns **green**
- Wrong option (if selected) turns **red**
- Unselected wrong option dims
- "Tap to dismiss" hint appears

**Dismissal:** Only available after an answer is selected.

**String resources used (4 parallel arrays — indices must stay in sync):**
- `R.array.gk_questions` — question text
- `R.array.gk_options_a` — option A label
- `R.array.gk_options_b` — option B label
- `R.array.gk_correct_options` — `"A"` or `"B"` for each question

---

## Shared Challenge Card Layout

All three deal types share the same card container:

- **Background:** player photo, blurred (`BlurEffect` 25 px)
- **Overlay:** `Color.Black` at 62 % opacity for text legibility
- **Entry animation:** scale from 85 % + fade in (350 ms / 300 ms)
- **Exit animation:** scale to 85 % + fade out (300 ms / 250 ms)
- **Dismissal guard:** `isChallengeDismissible` in `GameScreenState` prevents taps from going through before the deal type allows it

---

## State Model (`GameScreenState`)

| Field | Type | Purpose |
|---|---|---|
| `dealPhase` | `GameDealPhase` | Current phase in the sequence |
| `selectedPlayer` | `Player?` | Player selected for this deal |
| `animatingName` | `String` | Name shown while cycling |
| `dealType` | `GameDealType?` | Which type of challenge was drawn |
| `challengeText` | `String?` | Question / dare text (Truth or Dare + Sticky Dare) |
| `truthOrDareChoice` | `TruthOrDareChoice?` | `TRUTH` / `DARE` once the player has chosen; `null` before |
| `generalKnowledgeQuestion` | `GeneralKnowledgeQuestion?` | Full GK question object |
| `selectedAnswerOption` | `Char?` | `'A'` or `'B'` once the player has answered |
| `stickyDarePresentContinuous` | `String?` | Present-continuous form of the active sticky dare |
| `stickyDareDurationLabel` | `String?` | Human-readable duration (e.g. `"2 minutes"`) |
| `stickyDareDurationSeconds` | `Int?` | Duration in seconds; copied into `ActiveStickyDare` on dismissal |
| `activeStickyDares` | `List<ActiveStickyDare>` | All currently running sticky dare timers |
| `isChallengeDismissible` | `Boolean` (computed) | `true` when tapping the card should return to IDLE |

### `ActiveStickyDare` fields

| Field | Type | Purpose |
|---|---|---|
| `id` | `String` | UUID; used as coroutine job key |
| `playerName` | `String` | Displayed in the pill and bottom sheet |
| `presentContinuousText` | `String` | e.g. `"talking with a Hispanic accent"` |
| `durationLabel` | `String` | Original duration label shown in the pill |
| `totalSeconds` | `Int` | Original duration in seconds |
| `remainingSeconds` | `Int` | Counts down to 0; shown in the bottom sheet |
| `isCompleted` | `Boolean` | Set to `true` 400 ms before removal to trigger exit animation |

---

## Key Files

| File | Role |
|---|---|
| `GameScreenState.kt` | State, enums (`GameDealPhase`, `GameDealType`, `TruthOrDareChoice`), `GeneralKnowledgeQuestion`, `ActiveStickyDare` |
| `GameScreenViewModel.kt` | Orchestrates phase transitions, loads string resources, manages sticky dare countdown jobs |
| `GameDealSection.kt` | All deal-related composables: main card, challenge overlay, flip card, per-type UIs |
| `GameScreen.kt` | Root screen composable; top bar layout, bottom sheet and info panel visibility state |
| `ActiveStickyDare.kt` | `ActiveStickyDare` data class and `Int.toRemainingTimeLabel()` extension |
| `StickyDarePill.kt` | Animated pill shown in the top bar while at least one sticky dare is active |
| `StickyDaresBottomSheet.kt` | `ModalBottomSheet` listing all active dares with countdown and exit animations |
| `GameInfoPanel.kt` | Floating Surface panel showing selected game options (reads from `GameOptionsSource`) |
| `PlayersListRow.kt` | Players row; tapping a card opens the bottom sheet filtered to that player's dares |
| `BouncingDotsAnimation.kt` (`ui/common`) | Reusable 3-dot bouncing animation composable |
| `GameOptionsSource.kt` (`data/local/…`) | In-memory singleton for selected game options; written by `OptionsContainer`, read by `GameInfoPanel` |
| `res/values/strings.xml` | All localizable challenge strings (truth, dare, sticky dares + parallel arrays, GK questions) |
