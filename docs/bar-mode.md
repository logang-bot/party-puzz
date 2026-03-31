# Bar Time Mode

Bar Time is a game mode layered on top of the standard deal flow. When it is active, each deal ends with a **bar event** — a randomly drawn outcome that the player in turn must resolve before the next deal starts.

---

## How the mode is detected

The active game mode is a string resource ID (`Int`) set by `GameConfigScreen` into `GameOptionsSource.currentGameModeNameRes` via a `LaunchedEffect` when the screen is composed. `GameScreenViewModel` reads this value at state initialisation and stores the result in `BarModeState.isActive`:

```kotlin
barMode = BarModeState(isActive = GameOptionsSource.currentGameModeNameRes == R.string.bar_game_mode)
```

`isActive` is immutable for the lifetime of the game session; it never changes after the ViewModel is created.

---

## Event types

| Event | What happens |
|---|---|
| `NoAction` | Nothing — the player taps OK and the next deal begins |
| `GiveDrinks(amount, targetPlayerName)` | The active player must give the displayed number of drinks to the named player. It's the active player's call to enforce it. |
| `TakeDrinks(amount)` | The active player must drink the displayed amount |

Both `amount` and `targetPlayerName` in `GiveDrinks` are randomised at trigger time inside `BarModeState.triggerRandomEvent()` — the dialog is purely informational, there are no interactive pickers.

Events are drawn by `BarModeState.triggerRandomEvent(players, currentPlayer)`:

```
40 %  →  NoAction
30 %  →  GiveDrinks  (amount = random 1–5, target = random other player)
30 %  →  TakeDrinks  (amount = random 1–5)
```

`triggerRandomEvent` receives the current player list and the active player so it can exclude the active player from the target pool. If no valid target exists (should not happen in practice — the game requires ≥ 2 players), it falls back to `NoAction`.

---

## Event triggers per deal type

Bar mode modifies how each deal type ends. The table below shows what changes compared to the standard flow.

| Deal type | Standard dismissal | Bar mode change |
|---|---|---|
| **Truth or Dare** | Tap card after choice → deal resets | A **Skip** button appears on the back face of the flip card. Tapping it triggers a bar event. The normal card tap still resets without an event (the challenge was completed). |
| **General Knowledge** | Tap card after answering → deal resets | The card tap now triggers a bar event instead of resetting immediately. The tap hint changes to "Tap to continue". |
| **Sticky Dare** | Tap card → creates sticky dare + deal resets | A **Skip** button appears alongside the normal "Tap to dismiss" hint. Tapping Skip triggers a bar event and skips the sticky dare (the timer is **not** started). The normal card tap still creates the sticky dare as usual. |
| **Mini-game** | Tap card after results → deal resets | The "Tap to dismiss" hint is replaced by a **Finish** button. Tapping it triggers a bar event. The card tap is disabled in bar mode once results are shown, forcing use of Finish. |

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
BarModeState.activeEvent set  ──▶  bar event dialog appears on top
      │                             challenge card remains visible behind
      │
user resolves event (OK / Give)
      │
onBarEventDismissed()
      │
activeEvent = null  +  deal resets to IDLE
```

The challenge card is non-interactive while `activeBarEvent` is set (`enabled = … && barMode.activeEvent == null`), preventing double-triggering.

---

## Bar event dialog

The dialog is an overlay composable (`BarEventDialog`) shown inside `GameScreen`'s root `Box` via `AnimatedVisibility` (`fadeIn` / `fadeOut`). It is not a system dialog.

**Layout (top to bottom inside the card):**

```
"Bar Event!"  ← headlineMedium, bold
[img_bar_mode_illustration]  ← 160 dp, continuously rotating (4 s / full turn)
[event-specific content]
```

**Card entry animation:** the card spins from 720° to 0° (`tween` 800 ms, `FastOutSlowInEasing`) driven by `animateFloatAsState` with a `LaunchedEffect(Unit)` trigger.

### Content per event type

**NoAction**
```
"Nothing happens — carry on!"
[OK]
```

**TakeDrinks**
```
"Take X drink(s)!"    ← headlineLarge, bold; X = amount
[OK]
```

**GiveDrinks**
```
"Give X drink(s) to PlayerName!"    ← headlineLarge, bold
[OK]
```

Both the amount and the target player are randomised at trigger time and carried inside the `GiveDrinks` event object. The dialog is read-only — the active player just confirms.

---

## Architecture

### State encapsulation

Bar mode state lives in its own data class, not as flat fields on `GameScreenState`:

```kotlin
// BarModeState.kt
data class BarModeState(
    val isActive: Boolean = false,
    val activeEvent: BarEvent? = null
) {
    companion object {
        fun triggerRandomEvent(): BarEvent { … }
    }
}

// GameScreenState.kt
data class GameScreenState(
    …
    val barMode: BarModeState = BarModeState()
)
```

This pattern is intended to scale: when other modes (Couples, PartyPuzz) gain their own runtime logic they each get a sibling state class (`CouplesModeState`, etc.) added to `GameScreenState`.

### ViewModel responsibilities

The ViewModel does not contain event generation logic — it delegates entirely to `BarModeState.triggerRandomEvent()`. Its bar-mode-specific surface area is:

| Method | When called |
|---|---|
| `onTruthOrDareSkipped()` | Skip button on T/D back face |
| `onStickyDareSkipped()` | Skip button on Sticky Dare card |
| `onMiniGameDealFinished()` | Finish button on mini-game results |
| `onBarEventDismissed()` | Any dismiss action inside `BarEventDialog` |
| `onChallengeDismissed()` (modified) | For GK only: triggers event instead of resetting |

Each of the first four methods guards on `barMode.isActive` and exits early when bar mode is off, making them no-ops in other game modes.

---

## Key files

| File | Role |
|---|---|
| `BarEvent.kt` | Sealed class: `NoAction`, `GiveDrinks`, `TakeDrinks(amount)` |
| `BarModeState.kt` | State data class + `triggerRandomEvent()` logic |
| `BarEventDialog.kt` | Dialog composable: scrim, rotating card entry animation, per-event content |
| `GameScreenState.kt` | Holds `val barMode: BarModeState` |
| `GameScreenViewModel.kt` | Event handler methods; reads `barMode.isActive` as a guard |
| `GameDealSection.kt` | Skip / Finish buttons; GK tap hint; challenge card enabled guard |
| `GameScreen.kt` | Shows `BarEventDialog` overlay; passes new callbacks to `GameDealSection` |
| `GameOptionsSource.kt` | `currentGameModeNameRes: Int?` — bridge between `GameConfigScreen` and the ViewModel |
| `GameConfigScreen.kt` | Sets `GameOptionsSource.currentGameModeNameRes` on composition |

---

## String resources

| Key | EN value |
|---|---|
| `bar_event_title` | `"Bar Event!"` |
| `bar_event_no_action` | `"Nothing happens — carry on!"` |
| `bar_event_take_drinks` | `"Take %1$d drink(s)!"` |
| `bar_event_give_drinks` | `"Give %1$d drink(s) to %2$s!"` (`%1$d` = amount, `%2$s` = target player name) |
| `skip` | `"Skip"` |
| `finish` | `"Finish"` |
| `give` | `"Give"` |
| `tap_to_continue` | `"Tap to continue"` ← replaces `tap_to_dismiss` in GK when bar mode is active |
