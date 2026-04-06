# Game Mode Handler

`GameModeHandler` is a composition interface that encapsulates all mode-specific event logic, keeping `GameScreenViewModel` free of per-mode branching.

---

## Why it exists

Without the handler, every deal-type trigger in the ViewModel needed a `when` block to dispatch to the right mode:

```kotlin
when {
    state.barMode.isActive     -> { /* bar logic */ }
    state.couplesMode.isActive -> { /* couples logic */ }
}
```

This block appeared at five different trigger points. Each new game mode would have added a branch to all five. The handler moves that logic into separate classes — the ViewModel just calls `modeHandler.applyPunishment(state)` and the correct behaviour follows automatically.

---

## Interface

```kotlin
internal interface GameModeHandler {
    fun applyPunishment(state: GameScreenState, currentPlayer: Player?): GameScreenState
    fun applyReward(state: GameScreenState): GameScreenState
    fun applyMiniGameResult(state: GameScreenState): GameScreenState
    fun clearEvent(state: GameScreenState): GameScreenState
}
```

All methods are pure state transformers — they receive the current `GameScreenState` and return a new one. The ViewModel applies the result with `_uiState.update { modeHandler.methodName(it) }`.

| Method | When the ViewModel calls it | What it does |
|---|---|---|
| `applyPunishment(state, currentPlayer)` | Player skips or fails a challenge | Sets the active mode event to a punishment; `currentPlayer` identifies who to exclude from target selection |
| `applyReward(state)` | Player succeeds at a challenge | Sets the active mode event to a reward |
| `applyMiniGameResult(state)` | Finish button tapped on mini-game results | Applies the appropriate event based on `state.miniGameResult` |
| `clearEvent(state)` | User taps OK in the mode event dialog | Clears the active mode event field; the ViewModel then resets the deal to IDLE |

---

## Implementations

| Class | Active for | Behaviour |
|---|---|---|
| `NoOpModeHandler` | Standard, Party Puzz | All methods return `state` unchanged — no events fire |
| `BarModeHandler` | Bar Time | Produces `TakeDrinks` / `GiveDrinks` / `GiveDrinksPickTarget` / `NoAction` |
| `CouplesModeHandler` | Couples | Produces `MakeALoveDeclaration` / `ActOfLove` / `GiveAKiss` / `ChoseKissers` / `ChoseLovers` |

---

## Handler selection

The handler is chosen once at ViewModel initialisation and never changes during the session:

```kotlin
private val modeHandler: GameModeHandler = when (GameOptionsSource.currentGameModeNameRes) {
    R.string.bar_game_mode     -> BarModeHandler()
    R.string.couples_game_mode -> CouplesModeHandler()
    else                       -> NoOpModeHandler()
}
```

---

## Adding a new game mode

1. Create `XxxEvent.kt` — sealed class defining the mode's event types
2. Create `XxxModeState.kt` — state data class (`isActive`, `activeEvent: XxxEvent?`) with factory methods in `companion object`
3. Create `XxxModeHandler` in `GameModeHandler.kt` implementing `GameModeHandler`
4. Add `R.string.xxx_game_mode -> XxxModeHandler()` to the `when` in `GameScreenViewModel`
5. Add `val xxxMode: XxxModeState = XxxModeState()` to `GameScreenState`
6. Create `XxxEventDialog.kt` and wire it into `GameScreen` via `AnimatedVisibility`

---

## Key files

| File | Role |
|---|---|
| `GameModeHandler.kt` | Interface + all three handler implementations |
| `GameScreenViewModel.kt` | Holds `modeHandler`; calls it at each trigger point |
| `BarModeState.kt` / `CouplesModeState.kt` | State + event factory methods per mode |
| `BarEvent.kt` / `CouplesEvent.kt` | Sealed event hierarchies per mode |
