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
| `GiveDrinks(amount, targetPlayerName)` | Reward | The active player must give the displayed number of sips to the named player |
| `GiveDrinksPickTarget(amount, candidates)` | Reward | Like `GiveDrinks` but the target has not been decided yet — the dialog shows a list of players to pick from. Tapping a name resolves to a `GiveDrinks` event. |
| `TakeDrinks(amount)` | Punishment | The active player must take the displayed number of sips |
| `FlavourReward(textRes)` | Reward | One of `barRewardFlavours` (6 lines) — the text is the whole outcome, no mechanics |
| `FlavourPunishment(textRes)` | Punishment | One of `barPunishmentFlavours` (7 lines) |

Each event carries its category as an extension property (`val BarEvent.category: EventCategory`) — see [game-mode-handler.md](game-mode-handler.md).

Which **category** fires is deterministic — each deal type rewards or punishes based on how the player did. *Which outcome within that category* is drawn from the roll's deck, so the reel has real alternatives to cycle before it lands. See [outcome-presentation.md](outcome-presentation.md).

| Deal type | Outcome | Event |
|---|---|---|
| Truth or Dare | Skip tapped | `TakeDrinks(1–3)` |
| Sticky Dare | Skip tapped | `TakeDrinks(1–3)` |
| Sticky Dare | Running dare cancelled | `TakeDrinks(1–3)` |
| Quiz | Wrong answer | `TakeDrinks(1–3)` |
| Quiz | Correct answer | `GiveDrinksPickTarget(1–3, otherPlayers)` |
| Mini-game (two-player) | Current player wins | `GiveDrinks(1–3, opponentName)` |
| Mini-game (two-player) | Tie | `NoAction` |
| Mini-game (two-player) | Current player loses | `TakeDrinks(1–3)` |
| Mini-game (global) | A single loser identified | `TakeDrinks(1–3)` (punishment applied to the loser) |

`amount` is always randomised 1–3 (kept low on purpose — see the responsible-drinking note below) at trigger time. `BarModeState` exposes three factory methods used by the ViewModel: `takeDrinksEvent()`, `giveDrinksEvent(targetName)`, and `giveDrinksPickTargetEvent(players, currentPlayer)`.

---

## Event triggers per deal type

Bar mode modifies how each deal type ends. The table below shows what changes compared to the standard flow.

| Deal type | Standard dismissal | Bar mode change |
|---|---|---|
| **Truth or Dare** | Tap card after choice → deal resets | A **Skip** button appears on the back face of the flip card. Tapping it fires `TakeDrinks`. The normal card tap still resets without an event (challenge completed). |
| **Quiz** | Tap card after answering → deal resets | The card tap fires a bar event: `TakeDrinks` for a wrong answer, `GiveDrinksPickTarget` for a correct answer. The tap hint changes to "Tap to continue". |
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
user taps Skip / Finish / card (quiz only)
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
| Reward (`NoAction`, `GiveDrinks`, `GiveDrinksPickTarget`, `FlavourReward`) | `AccentYellow` → `AccentPink` | `AccentYellow` | `ic_sports_bar` |
| Punishment (`TakeDrinks`, `FlavourPunishment`) | `OutcomePunishCrimson` → `OutcomePunishMidnight` | `OutcomePunishCrimson` | `ic_whatshot` |

**Reel:** the roll's own deck — `barPunishmentFlavours` plus `takeDrinksEvent()` for a punishment, `barRewardFlavours` plus `giveDrinksPickTargetEvent(...)` for a reward — stopping on the entry that actually fired. The old `outcome_reel_bar` label array is gone.

**Layout after landing:**

```
[gradient badge + icon]   ← pops in with a bouncy spring
REWARD / PUNISHMENT       ← kicker, tone-coloured
"Take 3 sip(s)!"        ← message, italic headline
"Tap to dismiss"          ← or the target buttons
```

### Content per event type

| Event | Message | Interaction |
|---|---|---|
| `FlavourReward` / `FlavourPunishment` | The line itself | Tap to dismiss |
| `NoAction` | "Nothing happens — carry on!" | Tap to dismiss |
| `TakeDrinks` | "Take X sip(s)!" | Tap to dismiss |
| `GiveDrinks` | "Give X sip(s) to PlayerName!" | Tap to dismiss |
| `GiveDrinksPickTarget` | "Give X sip(s) to:" | One button per candidate |

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

This pattern is intended to scale: when other modes (Couples, Party Puzl) gain their own runtime logic they each get a sibling state class (`CouplesModeState`, etc.) added to `GameScreenState`.

### ViewModel responsibilities

The ViewModel delegates event construction to `BarModeState` factory methods. Its bar-mode-specific surface area is:

| Method | When called | Event produced |
|---|---|---|
| `onTruthOrDareSkipped()` | Skip button on the Truth or Dare prompt | `TakeDrinks` |
| `onStickyDareSkipped()` | Skip button on Sticky Dare card | `TakeDrinks` |
| `cancelStickyDare(id)` (modified) | Cancel button in dares sheet | `TakeDrinks` (bar mode only, after dare removed) |
| `onMiniGameDealFinished()` | Finish button on mini-game results | `GiveDrinks` / `TakeDrinks` / `NoAction` based on winner |
| `onChallengeDismissed()` (modified) | Quiz card tap after answer | `GiveDrinksPickTarget` (correct) / `TakeDrinks` (wrong) |
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
| `outcome/OutcomeTheme.kt` | Bar reward / punishment theming |
| `outcome/OutcomeDecks.kt` | `barRewardDeck` / `barPunishmentDeck`, and `deckLandingOn` |
| `outcome/OutcomeFlavours.kt` | `barRewardFlavours` / `barPunishmentFlavours` |
| `GameScreenState.kt` | Holds `val barMode: BarModeState` |
| `GameScreenViewModel.kt` | Delegates event logic to `BarModeHandler` via `GameModeHandler` |
| `GameDealSection.kt` | Skip / Finish buttons; quiz tap hint; challenge card enabled guard |
| `GameDealOverlays.kt` | `OutcomeOverlay` — layers the roll and reveal above the turn |
| `GameOptionsSource.kt` | `currentGameModeNameRes: Int?` — bridge between `GameConfigScreen` and the ViewModel |
| `GameConfigScreen.kt` | Sets `GameOptionsSource.currentGameModeNameRes` on composition |

---

## String resources

Every key has a matching `values-es/strings.xml` entry. Keep both locales in step, and apply the [responsible-drinking rules](#responsible-drinking-rules-play-policy) to the Spanish copy too.

Several keys still carry their old names (`*_drinks`, `*_tequila_shot`, `*_chug`). The values were rewritten for the Play policy, but the keys were kept so no Kotlin had to change. Read the value, not the key.

### Events

| Key | EN value | ES value |
|---|---|---|
| `bar_event_no_action` | `"Nothing happens — carry on!"` | `"¡No pasa nada — sigan!"` |
| `bar_event_take_drinks` | `"Take %1$d sip(s)!"` | `"¡Toma %1$d sorbo(s)!"` |
| `bar_event_give_drinks` | `"Give %1$d sip(s) to %2$s!"` (`%1$d` = amount, `%2$s` = target player name) | `"¡Da %1$d sorbo(s) a %2$s!"` |
| `bar_event_give_drinks_choose` | `"Give %1$d sip(s) to:"`, the header for the pick-target variant | `"¡Da %1$d sorbo(s) a:"` |
| `skip` | `"Skip"` | |
| `finish` | `"Finish"` | |
| `give` | `"Give"` | |
| `tap_to_continue` | `"Tap to continue"`, which replaces `tap_to_dismiss` in the quiz when bar mode is active | |
| `bar_description` | `"Made for game nights with friends — rounds of wild challenges and sips (any drink works!)"` | `"Hecho para noches de juegos con amigos — rondas de locos desafíos y sorbos (¡cualquier bebida sirve!)"` |

### Flavour decks (`OutcomeFlavours.kt`)

| Key | EN value | ES value |
|---|---|---|
| `bar_reward_skip_next_drink` | Skip your next sip | Sáltate tu próximo sorbo |
| `bar_reward_steal_a_sip` | Pick who takes the next sip | Elige quién toma el próximo sorbo |
| `bar_reward_new_house_rule` | Make a new house rule for the rest of the night | Inventa una regla nueva para el resto de la noche |
| `bar_reward_crown_yourself` | Crown yourself MVP — everyone toasts you | Corónate MVP — todos brindan por ti |
| `bar_reward_send_for_a_round` | Send someone to fetch snacks for the table | Manda a alguien a traer botanas para la mesa |
| `bar_reward_swap_drinks` | Swap seats with whoever you want | Cambia de lugar con quien quieras |
| `bar_punishment_tequila_shot` | Talk in an accent until your next turn | Habla con un acento hasta tu próximo turno |
| `bar_punishment_finish_your_drink` | Take one sip — then sing the chorus of a song the group picks | Toma un sorbo — y canta el coro de una canción que elija el grupo |
| `bar_punishment_group_picks_shot` | The group picks your next dare | El grupo elige tu próximo reto |
| `bar_punishment_table_mixed_shot` | Swap seats with the player the table chooses | Cambia de lugar con el jugador que elija la mesa |
| `bar_punishment_shame_sash` | Wear the "walk of shame" sash for 3 rounds | Lleva la banda del "paseo de la vergüenza" por 3 rondas |
| `bar_punishment_five_second_chug` | Do 10 squats while the group counts | Haz 10 sentadillas mientras el grupo cuenta |
| `bar_punishment_wrong_hand_shot` | Use only your non-dominant hand for the next round | Usa solo tu mano no dominante durante la próxima ronda |

### Responsible-drinking dialogs (`ResponsibleDrinkingDialogs.kt`)

The age confirmation (first launch) and the per-launch reminder share `drinking_notice_badge`. Both present drinking as **one optional part** of the game: most of PartyPuzl needs no drinks, and only some modes add sips. The confirmation asks for **18+** (the app's rating). The legal-drinking-age requirement applies only to players who choose alcohol, so keep that distinction in any rewrite.

| Key | EN value | ES value |
|---|---|---|
| `drinking_notice_badge` | 18+ | 18+ |
| `drinking_notice_title` | Welcome to PartyPuzl | Bienvenido a PartyPuzl |
| `drinking_notice_body` | PartyPuzl is a party game for adults. Truth or Dare, trivia, sticky dares and mini-games all work without a single drink. Some modes, like Bar Time, add sips as a twist — any drink counts… If you choose alcohol, you must be of legal drinking age. Know your limits and never drink and drive. | PartyPuzl es un juego de fiesta para adultos. … Si eliges alcohol, debes tener la edad legal para beber. … |
| `drinking_notice_accept` | I'm 18 or older | Tengo 18 años o más |
| `drinking_notice_exit` | Exit | Salir |
| `drinking_reminder_title` | Before you start | Antes de empezar |
| `drinking_reminder_body` | Sips are optional and only some modes use them — water and soft drinks count just as much. Every challenge can be skipped. If you choose alcohol, pace yourself and never drink and drive. | Los sorbos son opcionales y solo algunos modos los usan — … nunca manejes después de beber. |
| `drinking_reminder_ok` | Got it | Entendido |


---

## Responsible-drinking rules (Play policy)

Google Play rejected the app (Nov 2026) under *Inappropriate Content → Alcohol* for copy that pushed heavy drinking: shots of tequila, chugging, "finish your drink", "loser drinks". The mode now follows these rules. Keep them when writing new copy:

- **Sips only.** No shots, chugging, downing, "finish your drink", or mixing drinks for someone else.
- **Keep the numbers low.** `amount` is 1–3 sips.
- **Any drink counts.** Copy never names a specific alcohol or tells anyone to buy a round.
- **Most flavour punishments don't involve drinking at all.** They are physical or social forfeits.
- **18+ confirmation and per-launch reminder.** `AgeConfirmationDialog` blocks the app on first launch until the player confirms they are 18 or older (the body adds that alcohol requires legal drinking age) (the flag is `UserPreferencesRepository.drinkingNoticeAccepted`). After that, `DrinkingReminderDialog` shows once per launch. See [navigation.md](navigation.md#responsible-drinking-dialogs).
- **Setup reminder.** `ResponsibleDrinkingHint` shows "Any drink works… Know your limits" on the setup screen for every mode with `GameMode.dealsSips` (Bar Time and Party Puzl). See [game-config.md](game-config.md#responsible-drinking-hint).
- **Store listing.** It must follow the same rules, and the IARC questionnaire must declare alcohol references.

## Related

- [outcome-presentation.md](outcome-presentation.md) — How the roll and reveal are rendered
- [game-mode-handler.md](game-mode-handler.md) — `BarModeHandler` and the trigger points
- [game-deal-flow.md](game-deal-flow.md) — The turn a bar event ends
- [party-puzz-mode.md](party-puzz-mode.md) — Bar events inside the mixed mode
