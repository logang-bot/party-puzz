# Game Deal Flow

A **game deal** is one player's turn, from the deal picker appearing to the challenge being dismissed. Each turn picks one player using a **round-based** system, and the player then picks their own deal type.

> Deal types used to be drawn randomly, and the categories in play were pre-selected on the config screen. Both are gone: the player chooses live, every turn. See [game-config.md](game-config.md).
>
> Which deals are *offered* is still decided up front, but by the enabled question packs rather than by category toggles. See [question-packs.md](question-packs.md).

---

## Round-Based Player Selection

Players take turns in rounds. A round ends only when every player has been selected exactly once; then a new round begins. Within each round the order is random (the round's queue is shuffled at the start), so no two consecutive rounds produce the same sequence.

```
Round 1: [Player B, Player A, Player C]   ← shuffled at start of round
Round 2: [Player C, Player B, Player A]   ← reshuffled once queue is empty
...
```

`roundQueue` is a private mutable list in `GameScreenViewModel`. When it is empty it is refilled with `players.shuffled()` and `roundNumber` is incremented; the front element is taken for the current turn.

> `advanceToNextTurn()` steps the queue **outside** the `_uiState.update { }` lambda. `MutableStateFlow.update` is a compare-and-set retry loop whose lambda may run more than once — stepping the queue inside it would occasionally skip a player or double-count a round.

---

## Phase Sequence

```
       ┌─────────────────────────────────────────────┐
       │                                             │
       ▼                                             │
  DEAL_CHOICE ──picks a deal──▶ CHALLENGE_SHOWN      │
       │                              │              │
  "Surprise me"                  user dismisses      │
       │                              │              │
       ▼                  ┌───────────┴───────────┐  │
SURPRISE_SHUFFLE      mode produced          nothing │
       │              an event               happened│
  1.6 s reel               │                     │   │
       │             outcome overlay             │   │
       └──────▶            │                     │   │
                     spin ──▶ reveal             │   │
                           │                     │   │
                           └─────────┬───────────┘   │
                                     │               │
                         pendingCameraRequest?        │
                              │            │          │
                             yes           no ────────┤
                              │                       │
                      camera request ─────────────────┘
                                        (next player)
```

| Phase | What the screen shows | Duration |
|---|---|---|
| `DEAL_CHOICE` | Current player, hero card(s), compact tiles, "Surprise me" | Until a deal is picked |
| `SURPRISE_SHUFFLE` | Slot reel cycling the four deals | 1.6 s |
| `CHALLENGE_SHOWN` | The chosen challenge, full-bleed | Until dismissed |

There is no idle or hand-off phase. The game opens directly on `DEAL_CHOICE` — `GameScreenViewModel.init` calls `advanceToNextTurn()`, so round 1's player is selected before the first frame, and a finished turn returns straight to the picker for the next player.

> When `pendingCameraRequest` is true, `dealPhase` stays at `CHALLENGE_SHOWN` after the challenge or event is dismissed, and the camera request card slides in on top. The turn only advances once the camera interaction resolves. See [photo-album.md](photo-album.md).

---

## Whose turn it is

The picker announces the player itself: avatar, "IT'S YOUR TURN", and their nickname sit above the cards. The active player is also ringed in the player rail along the bottom.

> An earlier iteration opened each turn on a split-screen "pass the phone" hand-off. That design was pulled — it is reserved for the Follow The Spot mini-game redesign. `PassThePhoneContent.kt` is kept in the package, unused, as the starting point for that work.

---

## Deal Choice

The player picks their own deal. Whichever category was played **last — by anyone, not just this player** — is promoted to a large hero card; the rest collapse into compact tiles.

| Hero category | Rendered as |
|---|---|
| Truth or Dare | **Two** hero cards, Truth and Dare, so the player commits to a side up front |
| General Knowledge / Sticky Dares / Mini-games | One hero card |

`heroDealType` is stored in `GameScreenState` and updated in `startChallenge()`, so it survives across turns. On the very first turn there is no previous pick, so `GameScreenViewModel` seeds it at **random** from `availableDealTypes` — done once the pack content has loaded, since that is what decides which deals exist. `resolvedHeroDealType` falls back to the first available deal if the stored hero later becomes unavailable.

| User action | Resulting `truthOrDareChoice` |
|---|---|
| Hero **Truth** card | `TRUTH` |
| Hero **Dare** card | `DARE` |
| Compact **Truth or Dare** tile | Random |
| Surprise reel lands on Truth or Dare | Random |

**Availability:** a deal is offered only when both hold (`availableDealTypes`):

1. At least one **enabled question pack** feeds its category. Packs are chosen on the setup screen and pooled by `QuestionPackContentLoader` into `GameScreenState.enabledCategories`; a deal whose packs are all switched off never appears on the choice screen or in the surprise reel. See [question-packs.md](question-packs.md).
2. For `MINI_GAME` only, there are at least 2 players.

The compact row therefore renders between 0 and 3 tiles. `enabledCategories` defaults to all four so the first frame renders normally, then narrows when the load returns — a few milliseconds, and the player cannot reach a challenge before then.

The setup screen refuses to start a game with no packs enabled, so `availableDealTypes` is never empty in practice.

**Surprise me** picks the target first, moves to `SURPRISE_SHUFFLE`, spins a `SlotReel` onto that target, and then starts the challenge. A surprise result counts as a pick, so it becomes the next turn's hero.

---

## Deal Types

### 1. Truth or Dare (`TRUTH_OR_DARE`)

The Truth / Dare split now happens in the deal picker, so the challenge renders the committed prompt directly — there is no in-challenge choice step and no card flip.

- Label: TRUTH or DARE, tinted with that side's accent
- Prompt text, drawn at random when the challenge starts
- Player name anchored to the bottom
- **Skip** button in Bar / Couples / Party Puzl modes, "Tap to dismiss" otherwise

**String resources used:**
- `R.array.truth_texts` — truth questions
- `R.array.dare_texts` — dare challenges

---

### 2. Sticky Dare (`STICKY_DARE`)

A dare with a fixed duration. Unlike the other types, dismissing does **not** end the challenge — it starts a countdown timer that runs in the background while the game continues.

**Challenge:**
- Title: "Sticky Dare!"
- Dare text shown immediately (no extra interaction required)
- Player name anchored to the bottom
- "Tap to dismiss" hint

**Dismissal:** Always available. On dismissal an `ActiveStickyDare` is created and the countdown starts.

#### Post-dismissal: Sticky Dare Pill

A floating pill appears in the top bar showing the most recently added active dare:

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
| Tap a **player avatar** | Player's nickname | Only that player's active dares | No |

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

> Cancelling a dare early from the sheet punishes **mid-turn**. The outcome overlay renders over whatever phase is active, and dismissing it clears the event without advancing the turn, so the current player does not silently lose their go.

**String resources used (4 parallel arrays — indices must stay in sync with `sticky_dares`):**
- `R.array.sticky_dares` — full dare text shown on the challenge
- `R.array.sticky_dares_present_continuous` — present-continuous form used in the pill and bottom sheet
- `R.array.sticky_dares_duration_labels` — human-readable duration (e.g. `"2 minutes"`)
- `R.array.sticky_dares_duration_seconds` (`integer-array`) — duration in seconds for the countdown

---

### 3. General Knowledge (`GENERAL_KNOWLEDGE`)

A trivia question with exactly two answer options.

**Initial state:**
- Title: "General Knowledge"
- Question text
- Two option buttons: **A** and **B**
- Player name anchored to the bottom

**After the player picks an option:**
- Correct option turns **green**
- Wrong option (if selected) turns **red**
- Unselected wrong option dims
- "Tap to dismiss" hint appears

**Dismissal:** Only available after an answer is selected. A correct answer rewards, a wrong one punishes — see [outcome-presentation.md](outcome-presentation.md).

**String resources used (4 parallel arrays — indices must stay in sync):**
- `R.array.gk_questions` — question text
- `R.array.gk_options_a` — option A label
- `R.array.gk_options_b` — option B label
- `R.array.gk_correct_options` — `"A"` or `"B"` for each question

---

### 4. Mini-games (`MINI_GAME`)

See [minigames.md](minigames.md).

---

## Presentation

The glass card that used to hold every prompt is gone. Challenge content renders full-bleed on the screen background, which is tinted by the deal or mode in play — `rememberGameBackground(uiState)` in `GameScreenTheme.kt` picks a `PageBackground` per deal phase, reusing the `gameModeTheme()` palette. Because that content sits on the page rather than on a card, its ink is `colorScheme.onBackground`. See [game-mode-visual-identity.md](game-mode-visual-identity.md) and [theming.md](theming.md).

- **Phase transitions:** `AnimatedContent`, fade + scale from 94 % (320 ms in / 220 ms out)
- **Dismissal guard:** `isChallengeDismissible` prevents taps from going through before the deal type allows it
- **Player rail:** 46 dp avatars in a 72 dp row, the active player ringed in the primary colour

---

## State Model (`GameScreenState`)

| Field | Type | Purpose |
|---|---|---|
| `dealPhase` | `GameDealPhase` | Current phase in the sequence |
| `roundNumber` | `Int` | 1-based; incremented when the round queue refills. Tracked but not currently surfaced in the UI |
| `selectedPlayer` | `Player?` | Player whose turn it is |
| `dealType` | `GameDealType?` | Which deal the player chose |
| `heroDealType` | `GameDealType` | Last category played, promoted next turn |
| `surpriseDealType` | `GameDealType?` | Reel landing target during `SURPRISE_SHUFFLE` |
| `challengeText` | `String?` | Question / dare text (Truth or Dare + Sticky Dare) |
| `truthOrDareChoice` | `TruthOrDareChoice?` | `TRUTH` / `DARE`; set at pick time, never null once the challenge shows |
| `generalKnowledgeQuestion` | `GeneralKnowledgeQuestion?` | Full GK question object |
| `selectedAnswerOption` | `Char?` | `'A'` or `'B'` once the player has answered |
| `stickyDarePresentContinuous` | `String?` | Present-continuous form of the active sticky dare |
| `stickyDareDurationLabel` | `String?` | Human-readable duration (e.g. `"2 minutes"`) |
| `stickyDareDurationSeconds` | `Int?` | Duration in seconds; copied into `ActiveStickyDare` on dismissal |
| `activeStickyDares` | `List<ActiveStickyDare>` | All currently running sticky dare timers |
| `outcomeStage` | `OutcomeStage?` | `SPINNING` / `REVEALED` while a reward or punishment is on screen |
| `enabledCategories` | `Set<PackCategory>` | Categories the enabled packs can supply; defaults to all four until loaded |
| `availableDealTypes` | `List<GameDealType>` (computed) | Deals in `enabledCategories`, minus `MINI_GAME` under 2 players |
| `resolvedHeroDealType` | `GameDealType` (computed) | `heroDealType`, or the first available deal if unavailable |
| `compactDealTypes` | `List<GameDealType>` (computed) | `availableDealTypes` minus the hero |
| `activeEventCategory` | `EventCategory?` (computed) | Reward vs punishment of the active event |
| `isChallengeDismissible` | `Boolean` (computed) | `true` when tapping should end the challenge |
| `pendingCameraRequest` | `Boolean` | Rolled at `CHALLENGE_SHOWN`; signals that a camera card should follow this turn's final dismissal |
| `showCameraRequest` | `Boolean` | `true` while the camera request card overlay is visible |

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
| `GameScreenState.kt` | State, enums (`GameDealPhase`, `GameDealType`, `TruthOrDareChoice`, `OutcomeStage`), `GeneralKnowledgeQuestion`, spin duration constants |
| `GameScreenViewModel.kt` | Turn machine, challenge content loading, sticky dare countdown jobs, outcome staging |
| `GameDealSection.kt` | Phase router; challenge, outcome overlay and camera card layering |
| `GameScreen.kt` | Root screen composable; background, top bar, bottom sheet visibility |
| `PassThePhoneContent.kt` | Split-screen hand-off — **not in the flow**; parked for the Follow The Spot redesign |
| `DealChoiceContent.kt` | The picker: hero card(s), compact tiles, "Surprise me" |
| `DealCategoryCards.kt` | `DealHeroCard` and `DealCompactCard` |
| `SurpriseShuffleContent.kt` | "Surprise me" reel |
| `SlotReel.kt` | Shared slot-machine reel, used by the surprise shuffle and the outcome roll |
| `GameScreenTheme.kt` | Mode-tinted background gradient, per-deal accents, shared shapes |
| `ActiveStickyDare.kt` | `ActiveStickyDare` data class and `Int.toRemainingTimeLabel()` extension |
| `StickyDarePill.kt` | Animated pill shown in the top bar while at least one sticky dare is active |
| `StickyDaresBottomSheet.kt` | `ModalBottomSheet` listing all active dares with countdown and exit animations |
| `PlayersListRow.kt` | Player rail; tapping an avatar opens the bottom sheet filtered to that player's dares |
| `BouncingDotsAnimation.kt` (`ui/common`) | Reusable 3-dot bouncing animation composable |
| `GameOptionsSource.kt` (`data/local/…`) | In-memory singleton holding `currentGameModeNameRes`, written by `GameConfigScreen`, read by `GameScreenViewModel` |
| `res/values/strings.xml` | All localizable challenge strings (truth, dare, sticky dares + parallel arrays, GK questions) |

---

## Related

- [game-config.md](game-config.md) — Setup screen that precedes the game
- [outcome-presentation.md](outcome-presentation.md) — Reward and punishment spin and reveal
- [minigames.md](minigames.md) — The mini-game deal type
- [photo-album.md](photo-album.md) — Camera request card, photo storage, and party album
