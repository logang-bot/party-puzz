# Outcome Presentation

A **mode event** is the reward or punishment a game mode hands out at the end of a deal. This document covers how one is presented; for what triggers each event and which mode produces it, see [game-mode-handler.md](game-mode-handler.md).

---

## Two stages

An outcome never appears instantly. It rolls, then it lands.

```
handler produces an event
          │
          ▼
  outcomeStage = SPINNING ──1.8 s──▶ outcomeStage = REVEALED
          │                                   │
   OutcomeSpinContent                 OutcomeRevealContent
   (slot reel cycling                 (pop-in badge, kicker,
    every outcome the                  message, tap to dismiss)
    mode can produce)
```

| Stage | Composable | Interaction |
|---|---|---|
| `SPINNING` | `OutcomeSpinContent` | None — taps are ignored |
| `REVEALED` | `OutcomeRevealContent` | Tap anywhere to dismiss, except while picking a drinks target |

`OUTCOME_SPIN_DURATION_MS` (1800 ms) lives in `GameScreenState.kt` so the UI does not have to depend on the ViewModel. The reel animates for that duration minus a 250 ms hold, so it visibly settles before the reveal replaces it.

The reel lands on **what actually happened**, not a random slot: `BarEvent.reelIndex` and `CouplesEvent.reelIndex` map the active event to its position in the mode's label array.

---

## Reward vs punishment

`EventCategory` (`REWARD` / `PUNISHMENT`) was declared long before anything read it. It now drives the entire look of the reveal, so the two read differently across a noisy room before anybody parses the text.

| Mode | Category | Gradient | Tone | Icon |
|---|---|---|---|---|
| Couples | Reward | `#FF5B8A` → `#8B6CFF` | `#FF5B8A` | `ic_couples` |
| Couples | Punishment | `#7A2140` → `#3A1030` | `#C23368` | `ic_couples` |
| Bar | Reward | `#FFD25A` → `#FF5B8A` | `#FFD25A` | `ic_sports_bar` |
| Bar | Punishment | `#FF2E63` → `#1A0B2E` | `#FF2E63` | `ic_whatshot` |

Rewards run bright and warm; punishments run dark and saturated. The tone colour is used for the reel highlight, the rolling label and the `REWARD` / `PUNISHMENT` kicker.

When Party Puzl leaves both sub-modes active, `activeOutcomeMode` resolves Couples first — its artwork is the more specific of the two.

---

## The reveal

```
        [ badge ]        ← 92 dp gradient circle + icon (Bar)
                           or the event's illustration (Couples)
                           pops in with a bouncy spring

        PUNISHMENT       ← kicker, tone-coloured, letter-spaced

    Take 3 drink(s)!     ← message, italic headline

     Tap to dismiss      ← or the target-picker buttons
```

Couples events keep their existing illustrations (`img_kiss`, `img_choose_kissers`, `img_love_declaration`, `img_love_act`, `img_lovers`) rather than the generic badge.

`BarEvent.GiveDrinksPickTarget` is the one outcome that is not tap-to-dismiss: it renders one `DealOptionButton` per candidate. Choosing one swaps the event for `BarEvent.GiveDrinks` in place, keeping `outcomeStage` at `REVEALED` so it does not re-spin.

> The animated beer-glass `DrinksFillIndicator` was removed with this redesign. Drink counts are carried by the message copy.

---

## Layering

The outcome renders as a full-screen overlay above the current phase, gated on `hasActiveModeEvent` — not on `dealPhase`. That matters because cancelling a sticky dare from the bottom sheet can punish at any moment, including during `DEAL_CHOICE`. Previously such an event set state that nothing rendered.

Dismissal is phase-aware:

| Dismissed during | Behaviour |
|---|---|
| `CHALLENGE_SHOWN` | Ends the turn — advances to the next player's `DEAL_CHOICE` (or the camera card first) |
| Any other phase | Clears the event only; the current player keeps their turn |

---

## String resources

Reel labels are ordered arrays. **Their order must stay in sync with `reelIndex`.**

| Array | Order |
|---|---|
| `R.array.outcome_reel_bar` | `NoAction`, `GiveDrinks`, `GiveDrinksPickTarget`, `TakeDrinks` |
| `R.array.outcome_reel_couples` | `GiveAKiss`, `ChooseKissers`, `MakeALoveDeclaration`, `ActOfLove`, `ChooseLovers` |

Other strings: `outcome_reward`, `outcome_punishment`, `outcome_rolling_reward`, `outcome_rolling_punishment`, plus the existing `bar_event_*` and `couples_event_*` message strings.

---

## Key Files

| File | Role |
|---|---|
| `outcome/OutcomeTheme.kt` | `OutcomeMode`, per mode + category theming, `activeOutcomeMode`, `reelIndex` mappings |
| `outcome/OutcomeSpinContent.kt` | The roll |
| `outcome/OutcomeRevealContent.kt` | The landed outcome and its message |
| `SlotReel.kt` | Shared reel motion, also used by "Surprise me" |
| `GameDealSection.kt` | `OutcomeOverlay` — layering and dismissal gating |
| `GameScreenViewModel.kt` | `applyOutcome()` / `startOutcomeSpin()` staging |
| `BarEvent.kt`, `CouplesEvent.kt` | Event types and their `category` extensions |

---

## Related

- [game-mode-handler.md](game-mode-handler.md) — What triggers a reward or punishment
- [bar-mode.md](bar-mode.md) — Bar events
- [couples-mode.md](couples-mode.md) — Couples events
- [party-puzz-mode.md](party-puzz-mode.md) — Random delegation between the two
- [game-deal-flow.md](game-deal-flow.md) — The turn the outcome ends
