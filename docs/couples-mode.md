# Couples Mode

Couples is a game mode layered on top of the standard deal flow. When it is active, each deal ends with a **couples event** — a romantic challenge or reward that the player in turn must acknowledge before the next deal starts.

---

## How the mode is detected

The active game mode is a string resource ID (`Int`) set by `GameConfigScreen` into `GameOptionsSource.currentGameModeNameRes` via a `LaunchedEffect` when the screen is composed. `GameScreenViewModel` reads this value at state initialisation and stores the result in `CouplesModeState.isActive`:

```kotlin
couplesMode = CouplesModeState(isActive = GameOptionsSource.currentGameModeNameRes == R.string.couples_game_mode)
```

`isActive` is immutable for the lifetime of the game session; it never changes after the ViewModel is created.

---

## Event types

| Event | What happens |
|---|---|
| `GiveAKiss` | The active player can give a kiss to whoever they choose |
| `ChoseKissers` | The active player chooses who the kissers will be |
| `MakeALoveDeclaration(targetPlayerName)` | The active player must make a love declaration to a player chosen by the CPU |
| `ActOfLove(requesterPlayerName)` | The active player must do whatever a player chosen by the CPU tells them to |
| `ChoseLovers` | The active player chooses who will make a love declaration |

Events fall into two categories:

- **Punishment** (`MakeALoveDeclaration`, `ActOfLove`) — triggered when the player fails or skips a challenge. The target / requester is selected randomly by the CPU from all other players at trigger time.
- **Reward** (`GiveAKiss`, `ChoseKissers`, `ChoseLovers`) — triggered when the player succeeds at a challenge.

The outcome → event mapping:

| Deal type | Outcome | Event |
|---|---|---|
| Truth or Dare | Skip tapped (after choosing) | `MakeALoveDeclaration` or `ActOfLove` (random) |
| Sticky Dare | Skip tapped | `MakeALoveDeclaration` or `ActOfLove` (random) |
| Sticky Dare | Running dare cancelled | `MakeALoveDeclaration` or `ActOfLove` (random, target = dare's player) |
| General Knowledge | Wrong answer | `MakeALoveDeclaration` or `ActOfLove` (random) |
| General Knowledge | Correct answer | `GiveAKiss`, `ChoseKissers`, or `ChoseLovers` (random) |
| Mini-game | Current player wins | `GiveAKiss`, `ChoseKissers`, or `ChoseLovers` (random) |
| Mini-game | Tie or current player loses | No event |

`CouplesModeState` exposes two factory methods used by `CouplesModeHandler`: `punishmentEvent(players, currentPlayer)` and `rewardEvent()`.

---

## Event triggers per deal type

Couples mode modifies how each deal type ends. The table below shows what changes compared to the standard flow.

| Deal type | Standard dismissal | Couples mode change |
|---|---|---|
| **Truth or Dare** | Tap card after choice → deal resets | A **Skip** button appears on the back face of the flip card. Tapping it fires a punishment event. The normal card tap still resets without an event (challenge completed). |
| **General Knowledge** | Tap card after answering → deal resets | The card tap fires a couples event: punishment for a wrong answer, reward for a correct answer. The tap hint changes to "Tap to continue". |
| **Sticky Dare** | Tap card → creates sticky dare + deal resets | A **Skip** button appears alongside the "Tap to dismiss" hint. Tapping Skip fires a punishment event and skips the dare (timer **not** started). Cancelling an already-running sticky dare also fires a punishment event targeted at the dare's original player. The normal card tap creates the dare as usual. |
| **Mini-game** | Tap card after results → deal resets | The "Tap to dismiss" hint is replaced by a **Finish** button. Tapping it fires a reward event if the current player won; no event on a tie or loss. |

### Skip / Finish button placement

- **Truth or Dare** — Skip replaces the "Tap to dismiss" hint on the back face of the flip card.
- **Sticky Dare** — Skip is added below the "Tap to dismiss" hint.
- **Mini-game** — Finish replaces the "Tap to dismiss" hint in the result summary section.

---

## Event lifecycle

```
challenge shown
      │
user taps Skip / Finish / card (GK only)
      │
CouplesModeState.activeEvent set  ──▶  couples event dialog appears on top
      │                                 challenge card remains visible behind
      │
user taps OK
      │
onModeEventDismissed()
      │
activeEvent = null  +  deal resets to IDLE
```

The challenge card is non-interactive while `couplesMode.activeEvent` is set (`enabled = … && couplesMode.activeEvent == null`), preventing double-triggering.

---

## Couples event dialog

The dialog is an overlay composable (`CouplesEventDialog`) shown inside `GameScreen`'s root `Box` via `AnimatedVisibility` (`fadeIn` / `fadeOut`). It is not a system dialog.

**Layout (top to bottom inside the card):**

```
"Couples Event!"  ← headlineMedium, bold
[img_couples_mode_illustration]  ← 160 dp, static
[event message]  ← headlineMedium, bold
[OK]
```

**Card entry animation:** the card spins from 720° to 0° (`tween` 800 ms, `FastOutSlowInEasing`) driven by `animateFloatAsState` with a `LaunchedEffect(Unit)` trigger.

All five event types show a single informational message and an OK button — there are no interactive pickers. The active player and the rest of the party act on the message themselves.

### Content per event type

**GiveAKiss**
```
"You can give a kiss to whoever you choose!"
[OK]
```

**ChoseKissers**
```
"You choose who the kissers will be!"
[OK]
```

**MakeALoveDeclaration**
```
"Make a love declaration to PlayerName!"    ← PlayerName chosen by CPU
[OK]
```

**ActOfLove**
```
"Do whatever PlayerName tells you to do!"    ← PlayerName chosen by CPU
[OK]
```

**ChoseLovers**
```
"You choose who will make a love declaration!"
[OK]
```

---

## Architecture

### State encapsulation

Couples mode state lives in its own data class, mirroring `BarModeState`:

```kotlin
// CouplesModeState.kt
data class CouplesModeState(
    val isActive: Boolean = false,
    val activeEvent: CouplesEvent? = null
) {
    companion object {
        fun punishmentEvent(players: List<Player>, currentPlayer: Player?): CouplesEvent { … }
        fun rewardEvent(): CouplesEvent { … }
    }
}

// GameScreenState.kt
data class GameScreenState(
    …
    val barMode: BarModeState = BarModeState(),
    val couplesMode: CouplesModeState = CouplesModeState()
)
```

### ViewModel responsibilities

Mode-specific event logic is fully delegated to `CouplesModeHandler` — the ViewModel itself has no knowledge of couples events. See [game-mode-handler.md](game-mode-handler.md) for the handler pattern.

The ViewModel's couples-mode surface area (shared with all modes via the handler):

| Method | When called | Effect |
|---|---|---|
| `onTruthOrDareSkipped()` | Skip button on T/D back face | punishment event |
| `onStickyDareSkipped()` | Skip button on Sticky Dare card | punishment event |
| `cancelStickyDare(id)` (modified) | Cancel button in dares sheet | punishment event (dare's player as target) |
| `onMiniGameDealFinished()` | Finish button on mini-game results | reward event if current player won; nothing otherwise |
| `onChallengeDismissed()` (modified) | GK card tap after answer | reward (correct) or punishment (wrong) |
| `onModeEventDismissed()` | OK button inside `CouplesEventDialog` | clears event, resets deal to IDLE |

---

## Key files

| File | Role |
|---|---|
| `CouplesEvent.kt` | Sealed class: `GiveAKiss`, `ChoseKissers`, `MakeALoveDeclaration`, `ActOfLove`, `ChoseLovers` |
| `CouplesModeState.kt` | State data class + `punishmentEvent()` / `rewardEvent()` factory methods |
| `CouplesEventDialog.kt` | Dialog composable: scrim, rotating card entry animation, per-event message |
| `GameModeHandler.kt` | `CouplesModeHandler` implementation — all event construction logic |
| `GameScreenState.kt` | Holds `val couplesMode: CouplesModeState` |
| `GameScreenViewModel.kt` | Delegates all mode logic to `CouplesModeHandler` via `GameModeHandler` interface |
| `GameDealSection.kt` | Skip / Finish buttons; GK tap hint; challenge card enabled guard |
| `GameScreen.kt` | Shows `CouplesEventDialog` overlay; passes callbacks to `GameDealSection` |

---

## String resources

| Key | EN value |
|---|---|
| `couples_event_title` | `"Couples Event!"` |
| `couples_event_give_a_kiss` | `"You can give a kiss to whoever you choose!"` |
| `couples_event_chose_kissers` | `"You choose who the kissers will be!"` |
| `couples_event_make_love_declaration` | `"Make a love declaration to %1$s!"` (`%1$s` = target player name) |
| `couples_event_act_of_love` | `"Do whatever %1$s tells you to do!"` (`%1$s` = requester player name) |
| `couples_event_chose_lovers` | `"You choose who will make a love declaration!"` |
