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
  PLAYER_PICK                                        │
       │                                             │
  ~2.4 s turn intro                                  │
       │                                             │
       ▼                                             │
  DEAL_CHOICE ─promote, confirm─▶ CHALLENGE_SHOWN    │
       │                              │              │
  "Surprise me"                  user dismisses      │
       │                              │              │
       ▼                  ┌───────────┴───────────┐  │
SURPRISE_SPOTLIGHT    mode produced          nothing │
       │              an event               happened│
 ~2.0 s spotlight          │                     │   │
       │             outcome overlay             │   │
       ▼                   │                     │   │
  DEAL_CHOICE        spin ──▶ reveal             │   │
   (promoted)              │                     │   │
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
| `PLAYER_PICK` | A blank deal area, player names flicking through its centre, then the picked player's photo | ~2.4 s, or a tap |
| `DEAL_CHOICE` | Current player, hero card(s), compact tiles, "Surprise me" | Until a deal is confirmed |
| `SURPRISE_SPOTLIGHT` | Deals gathered into a grid, a highlight ring travelling across them | ~2.0 s, then back to `DEAL_CHOICE` promoted |
| `CHALLENGE_SHOWN` | The chosen challenge, full-bleed | Until dismissed |

There is no idle or hand-off phase. Every turn — including the first, since `GameScreenViewModel.init` calls `advanceToNextTurn()` too — opens on `PLAYER_PICK`, which announces whose turn it is and then hands over to the picker. See [The turn intro](#the-turn-intro).

> When `pendingCameraRequest` is true, `dealPhase` stays at `CHALLENGE_SHOWN` after the challenge or event is dismissed, and the camera request card slides in on top. The turn only advances once the camera interaction resolves. See [photo-album.md](photo-album.md).

---

## Whose turn it is

The picker announces the player itself: avatar, "IT'S YOUR TURN", and their nickname sit above the cards. The active player is also ringed in the player rail along the bottom — but it rings `revealedPlayer`, not `selectedPlayer`: the latter is already the next player throughout `PLAYER_PICK`, and ringing them there would give the intro's answer away.

### The turn intro

`PLAYER_PICK` is the same beat for every turn: the deal area is blank, and `PickedNameCycle` flicks player names through its centre — each rising into place as the last rises out — decelerating onto the player whose turn it is. The landed name springs into focus, the photo drops in on the same spring, the pair holds long enough to be read, and then `onPlayerPickFinished()` moves the turn to `DEAL_CHOICE`. A tap anywhere skips to the landing: the sequence is keyed on the skip flag, so it cancels the cycle in flight and re-enters it landed. `onPlayerPickFinished()` is idempotent either way — the ViewModel ignores it outside `PLAYER_PICK`.

| Beat | Duration |
|---|---|
| 11 name swaps, decelerating from 90 ms apart to 420 ms | 1733 ms |
| Landed name and photo hold | 650 ms |
| Phase change: cards enter, photo and name travel into the header | 420 ms |

That is ~2.4 s in `PLAYER_PICK` and ~2.8 s until the picker has settled, **the same on every turn** — `NameCyclePath` is a fixed 12 steps however many players there are and wherever the pick falls among them. An earlier version derived the length from the party, and the intro ran anywhere from 3.0 s to 3.8 s depending on the shuffle.

The deceleration is `DeceleratingRun`, the same cube-of-progress ramp the "Surprise me" ring travels on: early swaps stay near the fast bound and only the last two or three stretch towards the slow one, so the run reads as slowing *onto* the name.

> **Why this is not a letter scramble.** It was, and the scramble felt broken for two structural reasons. It re-randomised the name's letters every frame, and since the font is proportional an `M` and an `I` are different widths — so the text reflowed on every frame and jittered. And it was driven by a `delay()` loop, which is not frame-aligned, so an 80 ms step landed at 83 ms or 100 ms depending on where the frame boundary fell. Swapping whole names on a tween has neither problem: the motion is interpolated rather than sampled, so a step arriving a frame late changes nothing, and nothing re-lays-out mid-animation.

The name holds the exact centre of the screen for the whole intro, including before the photo exists. `NameCentringSpacer` is what buys that: it mirrors the photo and its gap on the far side of the name, so the photo's slot is reserved symmetrically and its arrival shifts nothing.

The photo and name are **shared elements**, not two copies that cross-fade. `GameDealSection` wraps the phase `AnimatedContent` in a `SharedTransitionLayout`, and `PlayerRevealTransition.kt` holds the two keys plus the `PlayerRevealScopes` that carry them; `CurrentPlayerHeader` claims the same keys, so the pair travels from the centre of the screen into the header while the deal cards fade and scale in beneath. The header takes those scopes as **nullable** — `SurpriseSpotlightContent` draws the same header outside any phase transition, and passes nothing.

> The name is only keyed once it has landed, and the landing is the run's **final step** rather than a composable that replaces the cycle — otherwise the name it lands over would vanish instead of sliding out like every other swap. Cycling names span the full width; only the landed one hugs its text, which is what the shared bounds need.

Because the intro holds the screen for well over two seconds, it also covers the pack load with room to spare. `onPlayerPickFinished()` awaits `packContentResolved` before opening the picker, so the picker's first frame already has the right hero card — previously it composed against the default and the real hero cross-faded in a frame later, which read as an unexplained flick of the hero card on entering the screen.

> An earlier iteration opened each turn on a split-screen "pass the phone" hand-off. That design was pulled — it is reserved for the Follow The Spot mini-game redesign. `PassThePhoneContent.kt` is kept in the package, unused, as the starting point for that work.

---

## Deal Choice

The player picks their own deal in **two taps**. Whichever category was played **last — by anyone, not just this player** — opens in the hero slot; the rest sit in compact tiles. Tapping a compact tile only *promotes* it into the hero slot — the outgoing hero drops into the row in its place — and the prompt comes on the tap after that, on the hero card itself. So the player always sees the category they picked before committing to it, and Truth or Dare always gets its two sides shown rather than rolling one for them.

The promoted category lives in `promotedDealType`, which only covers the current turn; `pickerHeroDealType` is what the picker actually draws (`promotedDealType`, else `resolvedHeroDealType`), and a promoted card carries a "tap to reveal" hint.

| Hero category | Rendered as |
|---|---|
| Truth or Dare | **Two** hero cards, Truth and Dare, so the player commits to a side up front |
| General Knowledge / Sticky Dares / Mini-games | One hero card |

`heroDealType` is stored in `GameScreenState` and updated in `startChallenge()`, so it survives across turns. On the very first turn there is no previous pick, so `GameScreenViewModel` seeds it at **random** from `availableDealTypes` — done once the pack content has loaded, since that is what decides which deals exist. `resolvedHeroDealType` falls back to the first available deal if the stored hero later becomes unavailable.

| User action | Resulting `truthOrDareChoice` |
|---|---|
| Hero **Truth** card | `TRUTH` |
| Hero **Dare** card | `DARE` |
| Compact **Truth or Dare** tile | None — promotes to the two hero cards |
| Spotlight lands on Truth or Dare | None — the hero shows both sides, the player picks one |

**Availability:** a deal is offered only when both hold (`availableDealTypes`):

1. At least one **enabled question pack** feeds its category. Packs are chosen on the setup screen and pooled by `QuestionPackContentLoader` into `GameScreenState.enabledCategories`; a deal whose packs are all switched off never appears on the choice screen or in the spotlight grid. See [question-packs.md](question-packs.md).
2. For `MINI_GAME` only, there are at least 2 players.

The compact row therefore renders between 0 and 3 tiles. `enabledCategories` defaults to all four so the first frame renders normally, then narrows when the load returns. The picker never sees that intermediate state anyway: the turn intro waits on the same load before opening.

The setup screen refuses to start a game with no packs enabled, so `availableDealTypes` is never empty in practice.

### Surprise me

**Surprise me** picks the target first, in `onSurpriseRequested()`, and then animates. Everything on screen after that tap is cosmetic — the deal is already decided.

The phase runs four stages, all driven from a single `LaunchedEffect` inside `SurpriseSpotlightContent`:

| Stage | What happens | Duration |
|---|---|---|
| `GATHER` | The compact cards of every available deal fade and scale into a centred grid, staggered | 350 ms |
| `TRAVEL` | A highlight ring hops card to card, ramping from 45 ms to 260 ms a step, and stops on the target | ~1.2 s |
| `FLICKER` | The landed ring cycles through `dealTones` — every deal's own colour — four times | 400 ms |
| `LANDED` | The flicker resolves and the ring holds steady on the target | 200 ms |

The ring's step duration comes from `DeceleratingRun`: the **cube** of the progress fraction, so nearly every step stays near 45 ms and only the last two or three stretch out. That is what makes the ring read as slowing *onto* a card rather than easing uniformly across all of them. The turn intro's name cycle rides the same ramp.

The grid is adaptive. `spotlightRows()` puts two per row once there are three or more deals, so four make a 2x2, three put two on top and one spanning the width below, two share a row, and one sits alone.

**The hand-off.** `LANDED` ends by calling `onSurpriseSettled()`, which returns to `DEAL_CHOICE` with `promotedDealType` set to the target. The **picker** then draws the hero card, so the growth into the hero state is the phase `AnimatedContent`'s own `fadeIn + scaleIn(0.94f)` rather than a second hero rendering inside the spotlight. The player confirms with the same tap a hand-picked category needs, and Truth or Dare still gets both its sides rather than having one rolled for it.

The spotlight reuses `DealChoiceContent`'s frame — same padding, same `CurrentPlayerHeader`, same 20 dp spacer — so the header barely shifts across the cross-fade and the grid sits where the hero and compact row will be.

> A shared-element transition would carry the landed card continuously into the hero slot, but it is not available here: the phase change and the picker's own promotion are two nested `AnimatedContent`s, and a `deal_<type>` bounds key can only be claimed once per layout. The cross-fade plus scale is the honest substitute.
>
> `onSurpriseSettled()` deliberately leaves `surpriseDealType` set. The outgoing spotlight is still composed for the duration of the cross-fade, and clearing the field there would blank that frame mid-transition. `startChallenge()` clears it on the confirming tap.
>
> Because the turn returns to an ordinary `DEAL_CHOICE`, the player **can** decline the surprise and tap a different compact tile instead. That follows from landing on a promotion rather than a committed challenge — "Surprise me" suggests, it does not bind.

A surprise result counts as a pick, so it becomes the next turn's hero.

> The phase was called `SURPRISE_SHUFFLE` while it drove a slot reel. Nothing shuffles now, so it is `SURPRISE_SPOTLIGHT`.
>
> The hand-off used to be a ViewModel timer: `delay(SURPRISE_SHUFFLE_DURATION_MS)` in a `dealJob`, with the reel separately subtracting its own hold constant from the same number. The animation now reports back when it is finished, so there is one source of truth for the timing, and `dealJob` is gone — a `LaunchedEffect` is torn down by the phase `AnimatedContent` if the turn advances or the screen is left, which is what the job's three cancel sites were for.

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
- **Surprise spotlight:** the grid, the travelling ring and the hero landing, described under [Surprise me](#surprise-me). The ring is an outer border on a `Box` wrapping each compact card, inset by `spotlightRingInset`, so it never collides with the card's own hairline
- **Promotion:** the picker is a `SharedTransitionLayout` over an `AnimatedContent` keyed on `pickerHeroDealType`; hero and compact cards share `deal_<type>` bounds keys, so the tapped tile rises into the hero slot while the old hero drops into the row (320 ms, `FastOutSlowInEasing`). The Dare card of the Truth/Dare pair has no key of its own — one key can only be claimed once per layout — so it fades and scales in
- **Deal identity:** each deal's tone, gradient, strings and glyph live in one `DealAccent` in `GameScreenTheme.kt`, read by the picker cards and the spotlight grid alike. The glyphs are the `ic_deal_*` set — brain (Truth), flame (Dare and the combined tile), trophy (General Knowledge), sparkle (Sticky Dares), dice (Mini-games) — shared with the custom-pack entry types so a category looks the same wherever it is drawn. "Surprise me" keeps `ic_random`, which means shuffle rather than mini-game
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
| `heroDealType` | `GameDealType` | Last category played, opens the next turn's hero slot |
| `promotedDealType` | `GameDealType?` | Category this player tapped into the hero slot; cleared when the turn advances |
| `pickerHeroDealType` | `GameDealType` (computed) | `promotedDealType`, else `resolvedHeroDealType` — what the picker draws |
| `surpriseDealType` | `GameDealType?` | The deal the spotlight ring has to stop on. `onSurpriseSettled()` copies it to `promotedDealType` but deliberately leaves it set |
| `surpriseRequestId` | `Int` | Bumped on every "Surprise me" tap. The spotlight is `key()`-ed on it, so a re-tap restarts the timeline instead of reusing a composition the phase transition is still animating out |
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
| `compactDealTypes` | `List<GameDealType>` (computed) | `availableDealTypes` minus `pickerHeroDealType` |
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
| `GameScreenState.kt` | State, enums (`GameDealPhase`, `GameDealType`, `TruthOrDareChoice`, `OutcomeStage`), `GeneralKnowledgeQuestion`, `OUTCOME_SPIN_DURATION_MS` |
| `GameScreenViewModel.kt` | Turn machine, challenge content loading, sticky dare countdown jobs, outcome staging |
| `GameDealSection.kt` | Phase router; `SharedTransitionLayout` for the reveal; challenge, outcome and camera layering |
| `GameDealOverlays.kt` | `OutcomeOverlay` and `CameraRequestCard` — the two things layered above the phase |
| `PlayerPickContent.kt` | The turn intro: the blank state, the sequence, the photo reveal, the skip |
| `PickedNameCycle.kt` | `NameCyclePath` and the cycling name — the swaps and the landing spring |
| `DeceleratingRun.kt` | The cube-of-progress step ramp, shared with the "Surprise me" ring |
| `PlayerRevealTransition.kt` | `PlayerRevealScopes` and the two shared keys that carry the photo and name |
| `GameScreen.kt` | Root screen composable; background, top bar, bottom sheet visibility |
| `PassThePhoneContent.kt` | Split-screen hand-off — **not in the flow**; parked for the Follow The Spot redesign |
| `DealChoiceContent.kt` | The picker's frame: `CurrentPlayerHeader`, `DealPicker`, "Surprise me" |
| `DealPicker.kt` | Hero slot and compact row, and the shared-bounds promotion between them |
| `DealCategoryCards.kt` | `DealHeroCard` and `DealCompactCard` |
| `SurpriseSpotlightContent.kt` | "Surprise me": the four-stage timeline, and the ring's travel order and step ramp |
| `SpotlightGrid.kt` | The adaptive grid of compact cards the ring travels across |
| `SpotlightBorder.kt` | `Modifier.spotlightBorder` — the ring itself, and its flicker through `dealTones` |
| `CurrentPlayerHeader.kt` | Whose turn it is — shared by the picker and the spotlight, and the reveal's landing point |
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
