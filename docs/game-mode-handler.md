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

## Event categories

All events across all game modes are classified as either a **reward** or a **punishment**:

```kotlin
enum class EventCategory { REWARD, PUNISHMENT }
```

The category is exposed as an extension property on each sealed event class:

```kotlin
val BarEvent.category: EventCategory get() = when (this) { … }
val CouplesEvent.category: EventCategory get() = when (this) { … }
```

This keeps the data class definitions clean (no extra constructor params) while giving the compiler exhaustiveness checking — adding a new event subclass without updating the extension is a compile error.

| Mode | Reward events | Punishment events |
|---|---|---|
| Bar Time | `NoAction`, `GiveDrinks`, `GiveDrinksPickTarget` | `TakeDrinks` |
| Couples | `GiveAKiss`, `ChoseKissers`, `ChoseLovers` | `MakeALoveDeclaration`, `ActOfLove` |

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
| `applyMiniGameResult(state)` | Finish button tapped on mini-game results | Branches on `state.miniGameResult` (see below). Tie / no result → state unchanged, ViewModel then calls `resetDeal()` |
| `clearEvent(state)` | User taps OK in the mode event dialog | Clears the active mode event field; the ViewModel then resets the deal to IDLE |

### `MiniGameResult` sealed interface

Both two-player and global mini-games funnel their outcome through the same state field (`GameScreenState.miniGameResult`) using a sealed interface:

```kotlin
sealed interface MiniGameResult

data class ScoredMiniGameResult(
    val player1Name: String,
    val player1Score: Int,
    val player2Name: String,
    val player2Score: Int
) : MiniGameResult {
    val winner: String? // p1 / p2 name, or null on a tie
}

data class LoserMiniGameResult(val loserName: String) : MiniGameResult
```

`applyMiniGameResult` pattern-matches on the variant:

| Variant | Bar / Couples / Party Puzz behaviour |
|---|---|
| `ScoredMiniGameResult` with `winner == selectedPlayer.nickName` | Reward the current player (Bar: `GiveDrinks(opponent)`; Couples: random reward) |
| `ScoredMiniGameResult` with `winner == null` (tie) | No event — `applyMiniGameResult` returns state unchanged; ViewModel falls through to `resetDeal()` |
| `ScoredMiniGameResult` with `winner == opponent` | Punish the current player (Bar: `TakeDrinks`; Couples: punishment against the current player) |
| `LoserMiniGameResult` | Punish the named loser (Bar: `TakeDrinks`; Couples: punishment against the loser — `state.players.find { it.nickName == loserName }`) |
| `null` | State returned unchanged (defensive no-op) |

Adding a new mini-game usually means reusing one of the two variants; only introduce a new `MiniGameResult` subtype if its payload is genuinely different, since every handler's `applyMiniGameResult` must exhaustively cover the sealed hierarchy.

---

## Implementations

| Class | Active for | Behaviour |
|---|---|---|
| `NoOpModeHandler` | Standard | All methods return `state` unchanged — no events fire |
| `BarModeHandler` | Bar Time | Produces `TakeDrinks` / `GiveDrinks` / `GiveDrinksPickTarget` / `NoAction` |
| `CouplesModeHandler` | Couples | Produces `MakeALoveDeclaration` / `ActOfLove` / `GiveAKiss` / `ChoseKissers` / `ChoseLovers` |
| `PartyPuzzModeHandler` | Party Puzz | Randomly delegates to `BarModeHandler`, `CouplesModeHandler`, or `NoOpModeHandler` on each trigger; `clearEvent` clears both sub-mode events |

---

## Handler selection

The handler is chosen once at ViewModel initialisation and never changes during the session:

```kotlin
private val modeHandler: GameModeHandler = when (GameOptionsSource.currentGameModeNameRes) {
    R.string.bar_game_mode        -> BarModeHandler()
    R.string.couples_game_mode    -> CouplesModeHandler()
    R.string.party_puzz_game_mode -> PartyPuzzModeHandler()
    else                          -> NoOpModeHandler()
}
```

---

## Adding a new game mode

1. Create `XxxEvent.kt` — sealed class defining the mode's event types, plus a `val XxxEvent.category: EventCategory` extension property covering every subclass
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
