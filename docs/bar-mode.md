# Bar Time Mode

Bar Time is a game mode layered on top of the standard deal flow. When it is active, each turn ends with a **bar event** — an outcome the player in turn must resolve before the phone is passed on.

---

## How the mode is detected

The active game mode is a string resource ID (`Int`) set by `GameConfigScreen` into `GameOptionsSource.currentGameModeNameRes` via a `LaunchedEffect` when the screen is composed. `GameScreenViewModel` reads this value at state initialisation and stores the result in `BarModeState.isActive`:

```kotlin
barMode = BarModeState(isActive = GameOptionsSource.currentGameModeNameRes == R.string.bar_game_mode)
```

`isActive` is immutable for the lifetime of the game session; it never changes after the ViewModel is created.

---

## Event types

| Event | Category | What happens |
|---|---|---|
| `NoAction` | Reward | Nothing — the player taps OK and the next deal begins |
| `GiveDrinks(amount, targetPlayerName)` | Reward | The active player must give the displayed number of drinks to the named player |
| `GiveDrinksPickTarget(amount, candidates)` | Reward | Like `GiveDrinks` but the target has not been decided yet — the dialog shows a list of players to pick from. Tapping a name resolves to a `GiveDrinks` event. |
| `TakeDrinks(amount)` | Punishment | The active player must drink the displayed amount |

Each event carries its category as an extension property (`val BarEvent.category: EventCategory`) — see [game-mode-handler.md](game-mode-handler.md).

Events are **not randomly drawn** — each deal type produces a deterministic event based on how the player did:

| Deal type | Outcome | Event |
|---|---|---|
| Truth or Dare | Skip tapped | `TakeDrinks(1–5)` |
| Sticky Dare | Skip tapped | `TakeDrinks(1–5)` |
| Sticky Dare | Running dare cancelled | `TakeDrinks(1–5)` |
| General Knowledge | Wrong answer | `TakeDrinks(1–5)` |
| General Knowledge | Correct answer | `GiveDrinksPickTarget(1–5, otherPlayers)` |
| Mini-game (two-player) | Current player wins | `GiveDrinks(1–5, opponentName)` |
| Mini-game (two-player) | Tie | `NoAction` |
| Mini-game (two-player) | Current player loses | `TakeDrinks(1–5)` |
| Mini-game (global) | A single loser identified | `TakeDrinks(1–5)` (punishment applied to the loser) |

`amount` is always randomised 1–5 at trigger time. `BarModeState` exposes three factory methods used by the ViewModel: `takeDrinksEvent()`, `giveDrinksEvent(targetName)`, and `giveDrinksPickTargetEvent(players, currentPlayer)`.

---

## Event triggers per deal type

Bar mode modifies how each deal type ends. The table below shows what changes compared to the standard flow.

| Deal type | Standard dismissal | Bar mode change |
|---|---|---|
| **Truth or Dare** | Tap card after choice → deal resets | A **Skip** button appears on the back face of the flip card. Tapping it fires `TakeDrinks`. The normal card tap still resets without an event (challenge completed). |
| **General Knowledge** | Tap card after answering → deal resets | The card tap fires a bar event: `TakeDrinks` for a wrong answer, `GiveDrinksPickTarget` for a correct answer. The tap hint changes to "Tap to continue". |
| **Sticky Dare** | Tap card → creates sticky dare + deal resets | A **Skip** button appears alongside the "Tap to dismiss" hint. Tapping Skip fires `TakeDrinks` and skips the dare (timer **not** started). Cancelling an already-running sticky dare also fires `TakeDrinks`. The normal card tap creates the dare as usual. |
| **Mini-game** | Tap card after results → deal resets | The "Tap to dismiss" hint is replaced by a **Finish** button. Tapping it fires `GiveDrinks(opponent)` if the current player won, `TakeDrinks` if they lost, or `NoAction` on a tie. The card tap is disabled in bar mode once results are shown. |

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
onModeEventDismissed()
      │
activeEvent = null  +  turn returns to the deal picker
```

The challenge card is non-interactive while any mode event is set (`enabled = … && !uiState.hasActiveModeEvent`), preventing double-triggering. The card tap is also disabled during mini-game result display when a mode is active (`!uiState.isModeActive` guard).

---

## Bar event presentation

Bar events are rendered by the shared outcome overlay, not by a bespoke dialog. Every event rolls on a slot reel before it lands, and rewards look different from punishments. The full presentation contract lives in [outcome-presentation.md](outcome-presentation.md); what follows is bar-specific.

**Theme:**

| Category | Gradient | Tone | Icon |
|---|---|---|---|
| Reward (`NoAction`, `GiveDrinks`, `GiveDrinksPickTarget`) | `#FFD25A` → `#FF5B8A` | `#FFD25A` | `ic_sports_bar` |
| Punishment (`TakeDrinks`) | `#FF2E63` → `#1A0B2E` | `#FF2E63` | `ic_whatshot` |

**Reel:** `R.array.outcome_reel_bar`, ordered `NoAction`, `GiveDrinks`, `GiveDrinksPickTarget`, `TakeDrinks` — matching `BarEvent.reelIndex`, so the reel stops on the event that actually fired.

**Layout after landing:**

```
[gradient badge + icon]   ← pops in with a bouncy spring
REWARD / PUNISHMENT       ← kicker, tone-coloured
"Take 3 drink(s)!"        ← message, italic headline
"Tap to dismiss"          ← or the target buttons
```

### Content per event type

| Event | Message | Interaction |
|---|---|---|
| `NoAction` | "Nothing happens — carry on!" | Tap to dismiss |
| `TakeDrinks` | "Take X drink(s)!" | Tap to dismiss |
| `GiveDrinks` | "Give X drink(s) to PlayerName!" | Tap to dismiss |
| `GiveDrinksPickTarget` | "Give X drink(s) to:" | One button per candidate |

Tapping a player button calls `onGiveDrinksTargetSelected(name)`, which transitions the active event to `GiveDrinks(amount, name)` in place. `outcomeStage` stays `REVEALED`, so the reel does not spin again.

> The animated `DrinksFillIndicator` beer glass was removed in the game screen redesign. Drink counts are carried by the message copy.

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

The ViewModel delegates event construction to `BarModeState` factory methods. Its bar-mode-specific surface area is:

| Method | When called | Event produced |
|---|---|---|
| `onTruthOrDareSkipped()` | Skip button on the Truth or Dare prompt | `TakeDrinks` |
| `onStickyDareSkipped()` | Skip button on Sticky Dare card | `TakeDrinks` |
| `cancelStickyDare(id)` (modified) | Cancel button in dares sheet | `TakeDrinks` (bar mode only, after dare removed) |
| `onMiniGameDealFinished()` | Finish button on mini-game results | `GiveDrinks` / `TakeDrinks` / `NoAction` based on winner |
| `onChallengeDismissed()` (modified) | GK card tap after answer | `GiveDrinksPickTarget` (correct) / `TakeDrinks` (wrong) |
| `onGiveDrinksTargetSelected(name)` | Player button in pick-target dialog | transitions `GiveDrinksPickTarget` → `GiveDrinks` |
| `onModeEventDismissed()` | Tap on the landed outcome | clears event via handler, returns the turn to the deal picker |

Mode-specific event logic is fully delegated to `BarModeHandler` — see [game-mode-handler.md](game-mode-handler.md).

---

## Key files

| File | Role |
|---|---|
| `BarEvent.kt` | Sealed class: `NoAction`, `GiveDrinks`, `TakeDrinks(amount)` |
| `BarModeState.kt` | State data class + `triggerRandomEvent()` logic |
| `outcome/OutcomeSpinContent.kt`, `outcome/OutcomeRevealContent.kt` | Shared roll and reveal used by every bar event |
| `outcome/OutcomeTheme.kt` | Bar reward / punishment theming and `BarEvent.reelIndex` |
| `GameScreenState.kt` | Holds `val barMode: BarModeState` |
| `GameScreenViewModel.kt` | Delegates event logic to `BarModeHandler` via `GameModeHandler` |
| `GameDealSection.kt` | Skip / Finish buttons; GK tap hint; challenge card enabled guard |
| `GameDealSection.kt` | `OutcomeOverlay` — layers the roll and reveal above the turn |
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
| `bar_event_give_drinks_choose` | `"Give %1$d drink(s) to:"` — header for the pick-target variant |
| `skip` | `"Skip"` |
| `finish` | `"Finish"` |
| `give` | `"Give"` |
| `tap_to_continue` | `"Tap to continue"` ← replaces `tap_to_dismiss` in GK when bar mode is active |


---

## Related

- [outcome-presentation.md](outcome-presentation.md) — How the roll and reveal are rendered
- [game-mode-handler.md](game-mode-handler.md) — `BarModeHandler` and the trigger points
- [game-deal-flow.md](game-deal-flow.md) — The turn a bar event ends
- [party-puzz-mode.md](party-puzz-mode.md) — Bar events inside the mixed mode
