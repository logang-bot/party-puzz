# Outcome Presentation

A **mode event** is the reward or punishment a game mode hands out at the end of a deal. This document covers how one is presented; for what triggers each event and which mode produces it, see [game-mode-handler.md](game-mode-handler.md).

---

## Two stages

An outcome never appears instantly. It rolls, then it lands.

```
handler produces an event
          │
          ▼
  outcomeStage = SPINNING ──2.2 s──▶ outcomeStage = REVEALED
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

`OUTCOME_SPIN_DURATION_MS` (2200 ms) lives in `GameScreenState.kt` so the UI does not have to depend on the ViewModel. The reel animates for that duration minus a 250 ms hold, so it visibly settles before the reveal replaces it.

The reel lands on **what actually happened**: it renders this roll's deck and stops at `deck.indexOf(activeEvent)`.

### The deck

The reel used to cycle `R.array.outcome_reel_bar` / `outcome_reel_couples` — four and five *generic category labels* ("Take drinks", "Give a kiss") — while the reveal showed a different string composed at render time from a format string and runtime values. The player watched "Take drinks" scroll past and then read "Take 3 drink(s)!". The spin was decoration over a result it never showed.

Now a roll builds a **deck** of every outcome it could land on, and the fired outcome is one of its entries:

| Roll | Deck |
|---|---|
| Bar reward | `barRewardFlavours` (6) + `giveDrinksPickTargetEvent(...)` |
| Bar punishment | `barPunishmentFlavours` (7) + `takeDrinksEvent()` |
| Couples reward | `couplesRewardFlavours` (6) + `GiveAKiss` + `ChooseKissers` + `ChooseLovers` |
| Couples punishment | `couplesPunishmentFlavours` (7) + `MakeALoveDeclaration(target)` + `ActOfLove(target)` |

The deck lives on the mode state — `BarModeState.deck` / `CouplesModeState.deck` — set by the handler in the same `copy` that sets `activeEvent`, so the two can never disagree. `clearEvent` empties both. Because the deck is `GameScreenState`, it dies with `GameScreenViewModel` when the game screen is popped: there is no cache to go stale and no reset call to forget.

**Flavour outcomes** are the design's flat lines (`FlavourReward` / `FlavourPunishment` on both event classes), carrying a `@StringRes` and no mechanics. Everything else keeps its behaviour — drink counts, the bar target-picker, the couples artwork. A flavour couples outcome has no image, so `CouplesEvent.imageRes` is nullable and the reveal falls back to the gradient medallion.

**Mini-games** roll their own outcome (`GiveDrinks` to the opponent on a win, `NoAction` on a draw, `takeDrinksEvent()` on a loss). `deckLandingOn(deck, fired)` puts it in the deck, *substituting* the generic entry of the same kind rather than joining it — otherwise the reel would offer two different "Take N drinks!" lines.

`OutcomeDeckTest` (`app/src/test/.../gameScreen/OutcomeDeckTest.kt`) holds the invariant: for every handler path, including Party Puzl and all four mini-game results, `activeEvent` appears in `deck` exactly once. An outcome outside its own deck is one the reel cannot land on.

### One text function

`outcome/OutcomeMessage.kt` is the only place an outcome turns into words. The reel and the reveal both read through `outcomeMessage` / `barMessage` / `couplesMessage`, which is what makes the row the reel settles on and the line the reveal shows the same string *by construction* rather than by agreement.

### Hand-over

`OutcomeOverlay` wraps the stage switch in a `SharedTransitionLayout` over an `AnimatedContent` keyed on `outcomeStage == REVEALED`. `Modifier.outcomeTextBounds(...)` claims the `"outcome_text"` key, so the landed line travels from the reel's centre window to the reveal's headline while the reel frame and mask fade out and the badge and kicker fade in.

A shared key may be claimed once per layout, and the reel repeats its deck — several slots can be showing the target index at once. So `SlotReel` hands `itemContent` an `isLanded` flag that is true only for the centre row and only after the spin has finished, and only that row attaches the modifier.

**It scales, it does not remeasure.** The two ends are styled very differently — 16 sp `titleMedium` in the reel, 28 sp bold italic `headlineMedium` in the reveal. With `RemeasureToBounds` the text is laid out afresh on *every frame* of the bounds animation, so a long line re-wraps in flight: two lines become three, then four, then settle back. `ScaleToBounds(ContentScale.Fit, Alignment.Center)` lays it out once and resizes it graphically, so the wrap never changes, and the simultaneous cross-fade covers the softness of the scaled frames.

**Both ends wrap their text.** The key sits on a `Box(contentAlignment = Center)` around each message, and neither box fills its parent. That matters for the scale factor: the reel's row is 88 dp tall but the reveal's two-line headline is about 76 dp, so a `fillMaxSize` reel box would give `Fit` a factor below 1 and the outgoing text would visibly *shrink* on its way to a larger style. Wrapping makes each end's bounds the text's own rect — about 272×48 going to 304×76 — so the factor is above 1 and the motion reads as growth.

**There is no highlight box.** The reel used to draw a bordered rectangle over its centre row to mark the selection. It was removed: the landed text would not sit inside it, and the reel reads fine with just its outer frame and the top/bottom fade mask framing the centre. It could now be reinstated: the reason the landed text would not stay inside it was `RemeasureToBounds` driving the row's measured size from the transition, and `ScaleToBounds` no longer touches measurement. Nobody has asked for it back.

**Row height is sized for the copy.** `outcomeRowHeight` is 88 dp. The flavour lines wrap to two or three lines of 24 dp, so the tallest (72 dp) clears the fade mask with room to spare. The centred slot spans 88-176 dp of the 264 dp window, centring on 132 dp, the window's own centre.

### Reel motion

`outcome/SlotReel.kt` belongs to the outcome package and nothing else uses it. It used to be shared with the deal picker's "Surprise me", which now has its own animation — see [game-deal-flow.md](game-deal-flow.md).

The reel is **deliberately unreadable until it slows**. That is the point of a slot machine: the outcome should arrive, not be previewed.

| Knob | Value | Why |
|---|---|---|
| `FULL_SPINS` | 2 | 14-26 rows of travel, over decks of 7 to 9 entries |
| `reelLanding` | `CubicBezierEasing(0.12f, 0.75f, 0.06f, 1f)` | Front-loads almost all the travel, then decelerates hard. A symmetric ease-in-out spreads its speed evenly and never reads as a slot machine — every row gets the same glance |
| Rows composed | `visibleItems + 1` | A sliding window, not the whole strip. `ReelWindowSpec.itemIndexAt()` maps each slot to its item, and the window is positioned so that at the final whole-numbered offset the centre slot holds `targetIndex` |
| Translation | `graphicsLayer { translationY }` | Draw phase. The previous `Modifier.offset { }` re-laid-out a 20-row, 1500 dp-tall column every frame |

**The shape to hit:** the opening rows must be too quick to read, and the closing three must be readable. Measured over the 1950 ms run, across every deck and every landing position:

| Deck | Entries | Rows travelled | Opening row | Rows under 45 ms | Closing three rows |
|---|---|---|---|---|---|
| Bar reward | 7 | 14-20 | 22-15 ms | 9-14 | 139-149 / 258 / 1039-1171 ms |
| Bar punishment | 8 | 16-23 | 19-13 ms | 10-17 | 144-151 / 255-259 / 990-1121 ms |
| Couples reward / punishment | 9 | 18-26 | 17-12 ms | 12-19 | 147-152 / 252-259 / 947-1078 ms |

Worst case the opening row gets 22 ms — well under a readable glance — and the third-from-last gets 139 ms.

So most of the roll is a genuine blur and only the approach resolves. Frame-step the `SlotReel` preview to check it.

> These figures are a function of `reelLanding`, `FULL_SPINS`, the run length and the deck size. Changing the deck size moves them — recheck if a deck grows past 9 entries.

---

## Reward vs punishment

`EventCategory` (`REWARD` / `PUNISHMENT`) was declared long before anything read it. It now drives the entire look of the reveal, so the two read differently across a noisy room before anybody parses the text.

Colours are named tokens from `ui/theme/Color.kt` — see [theming.md](theming.md).

| Mode | Category | Gradient | Tone | Icon |
|---|---|---|---|---|
| Couples | Reward | `AccentPink` → `AccentViolet` | `AccentPink` | `ic_couples` |
| Couples | Punishment | `OutcomePunishRoseDeep` → `OutcomePunishPlum` | `AccentRose` | `ic_couples` |
| Bar | Reward | `AccentYellow` → `AccentPink` | `AccentYellow` | `ic_sports_bar` |
| Bar | Punishment | `OutcomePunishCrimson` → `OutcomePunishMidnight` | `OutcomePunishCrimson` | `ic_whatshot` |

<details><summary>Hex values</summary>

`AccentPink` `#FF5B8A` · `AccentViolet` `#8B6CFF` · `AccentYellow` `#FFD25A` · `AccentRose` `#C23368` · `OutcomePunishRoseDeep` `#7A2140` · `OutcomePunishPlum` `#3A1030` · `OutcomePunishCrimson` `#FF2E63` · `OutcomePunishMidnight` `#1A0B2E`

</details>

Rewards run bright and warm; punishments run dark and saturated. The tone colour is used for the reel frame, the rolling label and the `REWARD` / `PUNISHMENT` kicker. These four palettes are identical in light and dark — the medallion icon on top is `appColors.onAccentSurface`.

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
| `CHALLENGE_SHOWN` | Ends the turn — advances to the next player's `PLAYER_PICK` (or the camera card first) |
| Any other phase | Clears the event only; the current player keeps their turn |

---

## String resources

The `outcome_reel_bar` / `outcome_reel_couples` arrays are **gone**, and with them the rule that their order had to stay in sync with a hand-written `reelIndex`. The reel derives its target from the deck, so there is no index contract between Kotlin and `strings.xml` left to break.

Flavour copy is 26 individual strings — `bar_reward_*` (6), `bar_punishment_*` (7), `couples_reward_*` (6), `couples_punishment_*` (7) — listed by res id in `outcome/OutcomeFlavours.kt`. That list is the source of truth for deck length, so adding a line means adding it in one place.

Other strings: `outcome_reward`, `outcome_punishment`, `outcome_rolling_reward`, `outcome_rolling_punishment`, plus the `bar_event_*` and `couples_event_*` message strings the mechanical outcomes format.

---

## Key Files

| File | Role |
|---|---|
| `outcome/OutcomeTheme.kt` | `OutcomeMode`, per mode + category theming, `activeOutcomeMode` |
| `outcome/OutcomeDecks.kt` | The four deck builders, `deckLandingOn`, and the orientation-filtered couples target |
| `outcome/OutcomeFlavours.kt` | The four res-id lists of mechanics-free lines |
| `outcome/OutcomeMessage.kt` | The only place an outcome becomes text — shared by reel and reveal |
| `outcome/OutcomeSharedText.kt` | `Modifier.outcomeTextBounds` — the shared key the landed line travels on |
| `outcome/OutcomeSpinContent.kt` | The roll |
| `outcome/OutcomeRevealContent.kt` | The landed outcome and its message |
| `outcome/SlotReel.kt` | Reel motion and its sliding window. Outcome-only — not shared |
| `GameDealOverlays.kt` | `OutcomeOverlay` — the overlay itself and its dismissal gating |
| `GameDealSection.kt` | The `AnimatedVisibility` that mounts it above the phase |
| `GameModeHandler.kt` | Builds a deck per roll and picks the outcome from it |
| `GameScreenViewModel.kt` | `applyOutcome()` / `startOutcomeSpin()` staging |
| `OutcomeDeckTest.kt` (`src/test`) | Holds the "fired outcome is in its own deck" invariant |
| `BarEvent.kt`, `CouplesEvent.kt` | Event types and their `category` extensions |

---

## Related

- [game-mode-handler.md](game-mode-handler.md) — What triggers a reward or punishment
- [bar-mode.md](bar-mode.md) — Bar events
- [couples-mode.md](couples-mode.md) — Couples events
- [party-puzz-mode.md](party-puzz-mode.md) — Random delegation between the two
- [game-deal-flow.md](game-deal-flow.md) — The turn the outcome ends
