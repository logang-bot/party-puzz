# Party Puzz Mode

Party Puzz is the "everything goes" game mode. Each deal ends with a mode event drawn randomly from the other three modes — Bar Time, Couples, or nothing at all — so no two rounds feel the same.

---

## How the mode is detected

The active game mode is a string resource ID (`Int`) set by `GameConfigScreen` into `GameOptionsSource.currentGameModeNameRes` via a `LaunchedEffect` when the screen is composed. `GameScreenViewModel` reads this value at state initialisation and activates both sub-mode states so that either can receive events:

```kotlin
barMode    = BarModeState(isActive = … || isPartyPuzzMode),
couplesMode = CouplesModeState(isActive = … || isPartyPuzzMode)
```

Both `isActive` flags are set to `true` for Party Puzz. This ensures `isModeActive = true`, which controls shared UI such as the Skip button on Sticky Dare and the Finish button on mini-game results.

---

## Event randomisation

`PartyPuzzModeHandler` holds one instance of each sub-handler and picks randomly on every trigger:

```kotlin
internal class PartyPuzzModeHandler : GameModeHandler {
    private val handlers = listOf(BarModeHandler(), CouplesModeHandler(), NoOpModeHandler())

    override fun applyPunishment(state, currentPlayer) = handlers.random().applyPunishment(state, currentPlayer)
    override fun applyReward(state)                    = handlers.random().applyReward(state)
    override fun applyMiniGameResult(state)            = handlers.random().applyMiniGameResult(state)

    override fun clearEvent(state) = state.copy(
        barMode     = state.barMode.copy(activeEvent = null),
        couplesMode = state.couplesMode.copy(activeEvent = null)
    )
}
```

The random draw is **per trigger**, not per deal. Each call to `applyPunishment`, `applyReward`, or `applyMiniGameResult` independently samples from the three handlers. This means:

- `NoOpModeHandler` → no event fires; deal resets normally
- `BarModeHandler` → a `BarEvent` is set; the Bar Event dialog appears
- `CouplesModeHandler` → a `CouplesEvent` is set; the Couples Event dialog appears

`clearEvent` always clears both `barMode.activeEvent` and `couplesMode.activeEvent` because either could have been populated.

---

## Event types

Party Puzz produces no event types of its own. It delegates to the full event catalogues of the other modes:

| Source mode | Possible events |
|---|---|
| Bar Time | `NoAction`, `TakeDrinks(amount)`, `GiveDrinks(amount, target)`, `GiveDrinksPickTarget(amount, candidates)` |
| Couples | `GiveAKiss`, `ChooseKissers`, `ChooseLovers`, `MakeALoveDeclaration(target)`, `ActOfLove(requester)` |
| Standard | — (no event; `NoOpModeHandler` returns state unchanged) |

See [bar-mode.md](bar-mode.md) and [couples-mode.md](couples-mode.md) for the full event specifications.

---

## Event triggers per deal type

The triggers are identical to those of Bar Time and Couples mode — because Party Puzz reuses the same handler interface. If the random draw produces a `NoOpModeHandler`, the trigger still fires but produces no visible event and the deal resets as in Standard mode.

| Deal type | Trigger | Possible outcome |
|---|---|---|
| Truth or Dare | Skip tapped (after choosing) | Bar punishment / Couples punishment / nothing |
| Sticky Dare | Skip tapped | Bar punishment / Couples punishment / nothing |
| Sticky Dare | Running dare cancelled | Bar punishment / Couples punishment / nothing |
| General Knowledge | Wrong answer | Bar punishment / Couples punishment / nothing |
| General Knowledge | Correct answer | Bar reward / Couples reward / nothing |
| Mini-game | Win | Bar reward / Couples reward / nothing |
| Mini-game | Loss | Bar punishment / Couples punishment / nothing |
| Mini-game | Tie | nothing (all handlers return state unchanged on tie) |

---

## Event lifecycle

The lifecycle is the same as Bar Time and Couples mode — whichever event fires follows its own dialog:

```
challenge shown
      │
user taps Skip / Finish / card (GK only)
      │
random handler chosen
      │
      ├─ NoOpModeHandler  ──▶  no event; deal resets to IDLE
      │
      ├─ BarModeHandler   ──▶  barMode.activeEvent set  ──▶  BarEventDialog appears
      │                        user resolves bar event (OK / Give)
      │                        onModeEventDismissed()
      │                        both activeEvents cleared  +  deal resets to IDLE
      │
      └─ CouplesModeHandler ▶  couplesMode.activeEvent set  ──▶  CouplesEventDialog appears
                               user taps card
                               onModeEventDismissed()
                               both activeEvents cleared  +  deal resets to IDLE
```

`hasActiveModeEvent` (checks both `barMode.activeEvent` and `couplesMode.activeEvent`) is the single guard used throughout the UI to detect a pending event regardless of which sub-mode produced it.

---

## Architecture

### No new state class

Party Puzz reuses the existing `BarModeState` and `CouplesModeState` inside `GameScreenState`. No `PartyPuzzModeState` class is needed because the mode has no state of its own beyond what the sub-modes already track.

### Handler selection

```kotlin
private val modeHandler: GameModeHandler = when (GameOptionsSource.currentGameModeNameRes) {
    R.string.bar_game_mode        -> BarModeHandler()
    R.string.couples_game_mode    -> CouplesModeHandler()
    R.string.party_puzz_game_mode -> PartyPuzzModeHandler()
    else                          -> NoOpModeHandler()
}
```

### No new dialog

Party Puzz produces no new overlay. `GameScreen` already mounts `BarEventDialog` and `CouplesEventDialog` via `AnimatedVisibility`; whichever dialog's backing event is set will appear automatically.

---

## Key files

| File | Role |
|---|---|
| `GameModeHandler.kt` | `PartyPuzzModeHandler` implementation |
| `GameScreenViewModel.kt` | Routes `party_puzz_game_mode` to `PartyPuzzModeHandler`; activates both sub-modes |
| `BarModeState.kt` / `CouplesModeState.kt` | Sub-mode states that Party Puzz can populate |
| `BarEventDialog.kt` / `CouplesEventDialog.kt` | Dialogs reused as-is by Party Puzz |
| `GameScreenState.kt` | `hasActiveModeEvent` and `isModeActive` already cover both sub-modes |

---

## String resources

| Key | EN value |
|---|---|
| `party_puzz_game_mode` | `"Party Puzz"` |
| `party_puzz_description` | `"The ultimate mix — every round is a surprise, combining all game modes into one unpredictable experience!"` |
