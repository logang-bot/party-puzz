# Mini-Game Suggestions (Backlog)

Candidate mini-games for future implementation. All are designed for a drunk party context: short rounds, dead-simple controls, and entertaining even with impaired coordination.

Each entry includes a rough implementation difficulty and which architectural pattern it would follow (see [minigames.md](minigames.md)).

---

## Tap War

**Type:** Two-player | **Difficulty:** Low

Two players sit on opposite halves of the phone (portrait, split-screen — same layout as Follow the Spot). A horizontal tug-of-war bar starts centered. Each tap on your side pushes the bar toward the opponent's edge. The player whose side the bar crosses first loses.

**Why it fits:** Nearly identical structure to Follow the Spot (`SpotBoard`-style split). The bar position is a single `Float` in `[0, 1]` updated on each tap. No timer needed — the game ends as soon as one side wins.

**Result:** Standard `p1Score / p2Score` via `SavedStateHandle` (1 / 0 or 0 / 1).

**New UI needed:**
- Animated progress bar replacing the spot circles
- Real-time score/position display in the divider strip

---

## Steady Hands

**Type:** Two-player or global | **Difficulty:** Medium

Players hold the phone as still as possible for a fixed duration (e.g. 10 s). The accelerometer measures cumulative wobble. The player with the most movement loses — or in global mode, the top wobbler takes the punishment.

**Why it fits:** Great for drunk parties — the more drunk you are, the worse you perform. Requires `SensorManager` access via a `DisposableEffect`.

**Implementation notes:**
- Register a `SensorEventListener` on `Sensor.TYPE_ACCELEROMETER`.
- Accumulate `sqrt(x² + y² + z²)` minus baseline gravity (≈ 9.81) per frame.
- In two-player mode: split-screen, each player holds their half, separate accumulators.
- In global mode: pass the phone around at the end, each player holds it for ~3 s; scores compared.

**Result:** Standard scores or global result pattern depending on chosen mode type.

---

## Simon Says

**Type:** Two-player or global | **Difficulty:** Medium

A sequence of 4 coloured buttons is shown and highlighted one at a time. Players must tap the same sequence. Each round extends the sequence by one. First player to fail the sequence loses.

**Why it fits:** A classic drinking game — the longer the game goes, the harder it gets, which pairs well with increasing intoxication.

**Implementation notes:**
- State holds `sequence: List<Int>` (0–3, mapped to colours) and `playerInputIndex`.
- Highlight a button: `animateColorAsState` on each button.
- On each tap: compare against `sequence[playerInputIndex]`; advance or trigger failure.
- In two-player mode: alternate turns; in global mode: players pass the phone each round.

**Result:** Standard scores.

---

## Most Likely To

**Type:** Global | **Difficulty:** Low

A prompt is shown (e.g. "Most likely to fall asleep first"). A countdown runs (e.g. 5 s). At Go, every player simultaneously points at someone. The player who receives the most fingers must drink.

**Why it fits:** No phone interaction needed during voting — just a prompt display and a timer. Social, chaotic, and requires zero reflexes.

**Implementation notes:**
- Prompt drawn from a `string-array` in `strings.xml`.
- Screen shows prompt + countdown; at zero: "Point!" displayed large.
- No automated winner detection — purely physical/social.
- On tap-to-dismiss: returns to `GameScreen` with no scores (no mode handler punishment).

**Result:** None — purely social, no `SavedStateHandle` result needed.

---

## Never Have I Ever

**Type:** Global | **Difficulty:** Very Low

A statement is shown (e.g. "Never have I ever sent a text to the wrong person"). Players who have done it drink. Tap to get the next statement.

**Why it fits:** Instant to implement — one screen composable, a string array, and a "Next" button.

**Implementation notes:**
- Pull from a new `R.array.never_have_i_ever` string array.
- State: `currentIndex: Int`, controlled by the ViewModel.
- "Done" button ends the mini-game; no result scores needed.

**Result:** None.

---

## Would You Rather

**Type:** Global | **Difficulty:** Low

Two options are shown (e.g. "Would you rather… fight 100 duck-sized horses OR 1 horse-sized duck?"). Players tap their choice. After all have chosen, the minority (fewer votes) must drink.

**Why it fits:** Quick, high participation, no skill required.

**Implementation notes:**
- Requires knowing how many players are playing (`GamePlayersList.PlayersList.size`) to detect the minority.
- Each option is a large tap target occupying half the screen.
- State: `votesA: Int`, `votesB: Int`, `totalVotes: Int`. Once `totalVotes == players.size` the result is shown.
- OR simplify to a single-tap reveal (one screen per player passing the phone) — lower fidelity but simpler.

**Result:** None (purely social enforcement).

---

## Draw & Guess

**Type:** Global | **Difficulty:** High

One player draws a word on screen using their finger; the others must guess it. First correct guess wins; the drawer and guesser both avoid punishment; everyone else drinks.

**Why it fits:** High entertainment value as the game gets further along.

**Implementation notes:**
- Requires a `Canvas` composable with `pointerInput` for drag path capture.
- State: `paths: List<Path>`, `currentPath: Path`, `wordToGuess: String`.
- The word is shown only to the current drawer (countdown before reveal, then word hidden after they confirm).
- Guess input: either a text field or a pool of multiple-choice options (simpler to implement).
- Most complex mini-game on this list — Canvas drawing performance on lower-end devices needs testing.

**Result:** None (social enforcement) or standard scores if guess-based scoring is added.

---

## Categories

**Type:** Global | **Difficulty:** Very Low

A category is displayed (e.g. "European countries"). Players take turns verbally naming one item. The phone is passed after each turn. First player to repeat an item or fail to name one within 5 s drinks.

**Why it fits:** Zero UI beyond a timer and a category display. Phone passing is the mechanic.

**Implementation notes:**
- One screen: large category text + 5-second countdown that resets on tap.
- "Bust" button for when the current holder fails — no automated detection.
- State: `category: String`, `timerValue: Int`.

**Result:** None.

---

## Priority Recommendation

For the next mini-game to implement after Hot Potato:

| Priority | Game | Reason |
|---|---|---|
| 1 | **Tap War** | Lowest effort — reuses Follow the Spot's split-screen architecture |
| 2 | **Never Have I Ever** | Single composable + a string array |
| 3 | **Categories** | Same simplicity; adds a pass-the-phone mechanic without any scoring complexity |
| 4 | **Steady Hands** | Unique differentiator; requires accelerometer work but no complex UI |

---

## Related

- [minigames.md](minigames.md) — Mini-game system architecture, implemented games, and the guide for adding new ones
