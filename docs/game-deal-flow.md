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

A dare with an implied duration (stated in the dare text itself).

**Challenge card:**
- Title: "Sticky Dare!"
- Dare text shown immediately (no extra interaction required)
- Player name anchored to the bottom
- "Tap to dismiss" hint

**Dismissal:** Always available (tap anywhere on the card).

**String resources used:**
- `R.array.sticky_dares`

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
| `isChallengeDismissible` | `Boolean` (computed) | `true` when tapping the card should return to IDLE |

---

## Key Files

| File | Role |
|---|---|
| `GameScreenState.kt` | State, enums (`GameDealPhase`, `GameDealType`, `TruthOrDareChoice`), `GeneralKnowledgeQuestion` |
| `GameScreenViewModel.kt` | Orchestrates phase transitions, loads string resources, exposes deal callbacks |
| `GameDealSection.kt` | All deal-related composables: main card, challenge overlay, flip card, per-type UIs |
| `GameScreen.kt` | Root screen composable; wires ViewModel callbacks into `GameDealSection` |
| `res/values/strings.xml` | All localizable challenge strings (truth, dare, sticky dares, GK questions) |
