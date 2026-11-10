# Game Configuration Screen

`GameConfigScreen` is the setup step between selecting a game mode and starting the game. Players register who is playing and pick which question packs are in play.

> **The "Pick what to play" categories section was removed.** Deal types are no longer pre-enabled here; each player picks their own deal at the start of their turn. See [game-deal-flow.md](game-deal-flow.md). `OptionsContainer.kt` and `MiniGamesOptionChip.kt` were deleted along with the `GameOptionsSource.options` list, `initialize()` and `toggle()`.
>
> Its replacement is the **Choose your packs** section: because every official pack maps to one deal, switching a pack off is now what removes that deal from the game. See [question-packs.md](question-packs.md).

---

## Layout

The screen is portrait-locked and composed of two regions:

1. **Scrollable content column** — game mode header, ad banner, players section, question packs, and the mini-games hint.
2. **Pinned footer** — the Start the Party button, always visible above the navigation bar.

Players sit **above** the packs section, matching the design's Prepare screen: who is playing is the required step, packs are the optional one.

---

## Game Mode Header

Shows the selected mode's name (bold italic, `displayMedium`) and icon (`72 dp`) side by side. Both elements participate in a shared element transition animated from the mode-selection card on `HomeScreen` (`boundsTransform tween 400 ms`). A description string appears below at `bodyLarge` with reduced opacity.

The screen also writes `GameOptionsSource.currentGameModeNameRes` in a `LaunchedEffect(gameModeName)`. That single value is the entire config → game bridge: `GameScreenViewModel` reads it at init to choose its `GameModeHandler` and to tint the game background.

---

## Players Section

**File:** `ui/views/gameConfig/ui/PlayersContainer.kt`

Shows the list of registered players. Each player card supports edit and delete actions. An "Add player" button navigates to `CreatePlayerScreen`. Player cards use a shared element transition keyed by player ID.

---

## Question Packs Section

**File:** `ui/views/gameConfig/ui/QuestionPacksSection.kt`

Three groups — **Official** (11 packs), **Premium** (3) and **Custom** — sharing one row design (`PackRow.kt`) and differing only in the badge and the trailing control:

| Group | Trailing control | Corner badge | Row when enabled |
|---|---|---|---|
| Official | Check circle (accent fill) | Check | Tinted with the pack's accent at `Wash.Soft` |
| Premium, unlocked | Check circle | — | Tinted |
| Premium, locked | **Unlock** pill | Padlock | Untinted, whole row at 70 % opacity |
| Custom | Check circle (spice accent) | Check | Tinted |

Tapping an unlocked row toggles it. Tapping a locked premium row opens `UnlockChoiceBottomSheet`.

### Premium content download

Premium prompts are downloaded from the pack bank ([pack-bank.md](pack-bank.md)), so a premium row's
count comes from the downloaded content (`PackBankRepository.content`, in the user's language) rather
than from `questions` rows. A premium pack with nothing downloaded has `PackUiModel.needsDownload`
set, and its meta line replaces the count:

| State | Meta line | Tap |
|---|---|---|
| Download in progress (`isPackBankRefreshing`) | "Downloading…" | As usual |
| Unlocked, nothing downloaded | "Couldn't download · tap to retry" | Retries the download (`refresh(force = true)`) instead of toggling |
| Locked, nothing downloaded | "Downloads when you're online" | Opens the unlock sheet |

A premium pack that `needsDownload` doesn't count toward `hasEnabledPack`, so it can't be the only
thing that enables Start.

When an **unlocked** premium pack needs a download (the user owns it but has no content), the
screen downloads it itself: once automatically per visit, and again on every retry tap. While that
runs, `isDownloadingPacks` shows the same `LoadingScrim` as deleting a player. If it fails, a long
snackbar explains why: no connection, server busy (HTTP 429) or server unavailable. See
[pack-bank.md](pack-bank.md#how-the-app-uses-it). Locked packs never trigger this, so a free user
with the server down sees no overlay or snackbar.

### The Custom group

Written by the user and, once offered, toggled per session exactly like the official ones. Group chrome lives in `PackGroupContainers.kt`; the group itself in `CustomPackGroup.kt`.

- Its label carries a live enabled/total count (`Custom · 1/2`) and a **Manage** link into `CustomPacksRoute`.
- With nothing written yet it shows the design's dashed "Write your first pack" panel — now a live button into the manager rather than a *Coming soon* placeholder.
- A custom row's name, icon and accent come from the user's own `custom_packs` row, not the catalog; its meta count is the pack's entry count.
- **Only packs marked available in the manager are listed**, and the `Custom · 1/2` count is over those. `QuestionPackCatalog` decides which built-in packs exist; authored packs have no catalog, so `custom_packs.isAvailable` plays that role — `observePacks` filters on it before mapping. The row toggle here remains the per-session pick, a genuinely separate flag.

Authoring itself, and the difference between the two flags, is documented in [custom-packs.md](custom-packs.md).

**`PackLabel`** is what makes one row design serve both. `PackUiModel.name` was an `@StringRes Int`, which could not hold a user-typed name; it is now a sealed `PackLabel` — `Resource(@StringRes Int)` for built-in packs, `Literal(String)` for custom ones — resolved by the `@Composable PackLabel.text()` helper at the render site.

`GameConfigViewModel` therefore maps packs in two passes: `toCatalogUiModels()` walks `QuestionPackCatalog.all` (so a row with no catalog entry is skipped, which is exactly right for custom rows), and `CustomPackSummary.toUiModel()` handles the rest. Both arrive through one four-flow `combine` — packs, session unlocks, `isAdFree`, and the custom summaries.

Each row's meta line shows the prompt count, read from the `questions` table once after seeding. The Mini-games pack has no question rows, so it reports `MiniGame.entries.size` instead of zero.

Full behaviour — tiers, unlocking, and how enabled packs reach the game — is documented in [question-packs.md](question-packs.md).

---

## Unlock Bottom Sheet

**File:** `ui/views/gameConfig/ui/UnlockChoiceBottomSheet.kt`

Built on the shared `PremiumSheet` (`ui/common/purchase/`). It offers up to two routes: watch a rewarded ad (unlocks that pack for the session) or buy premium, the one-time `remove_ads` product (every premium pack and unlimited custom packs, permanently; on ad branches it also removes ads). The ad row only appears when `AdsProvider.supportsRewardedAds` is true, so `main` offers the purchase alone.

The rewarded ad is loaded on first composition of the screen, not when the sheet opens, so the option is usually ready the moment the sheet appears. While it is still loading the option stays visible but disabled and its subtitle reads "Loading ad…". After a reward is granted the ad is reloaded so a second pack can be unlocked in the same session.

The purchase row shows Play's localised price once it has loaded. If Play has no details for `remove_ads` (no connection, or a build not installed from Play), `launchPurchaseFlow` reports `PurchaseOutcome.Unavailable` and `MainActivity` shows a toast instead of appearing to do nothing. See [ads.md](ads.md#premium--the-one-time-purchase).

---

## Setup Hints

Both hints use `SetupHint(iconRes, textRes)` (`SetupHints.kt`): an icon in a `primaryContainer` rounded tile, then one line of `bodySmall` text. Add any new setup tip through it so the tips stay visually identical.

### Mini-games hint

`MiniGamesHintBox` is a row below the packs section. It shows `ic_lightbulb`, and the hint text (`mini_games_hint`) explains the phone-passing mechanic. It is shown for every mode.

### Responsible-drinking hint

`ResponsibleDrinkingHint(gameModeName)` sits directly under `GameModeHeader`, 12 dp below it. It shows `ic_info` with `responsible_drinking_hint`. It appears only for modes where `GameMode.dealsSips` is true: **Bar Time**, and **Party Puzl**, because Party Puzl mixes Bar sip events in. Standard and Couples render nothing. It's a permanent line rather than a dialog, so players see the message every time they set up a drinking game without having to dismiss anything. It complements the launch-time responsible-drinking dialogs ([navigation.md](navigation.md#responsible-drinking-dialogs)). See [bar-mode.md](bar-mode.md#responsible-drinking-rules-play-policy).

---

## Start the Party Button

Pinned at the bottom of the screen, above the navigation bar (`navigationBarsPadding`), **overlaying** the scrolling content rather than sitting in a reserved strip below it. The strip behind it is a `Modifier.ctaScrim()` — the design's `linear-gradient(180deg, transparent, var(--bg-0) 50%)` — so the packs list keeps going behind the button and dissolves into the page base. The scroll column carries `bottom = 96.dp` so its last row is still reachable. See [theming.md](theming.md#sticky-bottom-ctas).

Enabled when **both**:

- at least 2 players are registered (`GamePlayersList.PlayersList.size >= 2`), and
- at least one pack is enabled (`GameConfigState.hasEnabledPack`) — official, premium **or** custom. An enabled premium pack only counts once its content has downloaded.

> The second condition replaces the old "at least one category enabled" check that was dropped with the categories section. It is meaningful again now that every pack can be switched off — without it the game would open with nothing to draw.

> Custom packs must be counted here. `QuestionPackContentLoader` pools their entries into the same deck, so a lone enabled custom pack is a playable game; while `hasEnabledPack` ignored them, turning every official pack off left Start disabled on a non-empty deck.

**Disabled styling:** the button keeps its solid `primary` fill and dims the whole composable to 45 % alpha, matching the design's `.pp-btn:disabled { opacity: 0.45 }`. It previously used the Material default — an `onSurface.copy(alpha = 0.12f)` background — which read as a translucent bar with the screen background showing through it.

When enabled, pressing the button animates the background and text colors between `primary` ↔ `onPrimary` via `animateColorAsState(tween 300 ms)`.

Tapping calls `GameConfigViewModel.onStartGame(onStartGameClick)`, which sets `isLoading = true`, prepares game state, then invokes the navigation callback. A `CircularProgressIndicator` overlay is shown while `isLoading` is true.

The callback navigates **straight to `GameScreen`**. The intermediate `LoadingScreen` route was removed; `GameScreen` inherited its slide-up enter transition, so the move into the game is visually unchanged. See [navigation.md](navigation.md).

---

## String Resources

| Key | EN |
|---|---|
| `prepare_your_party` | "Prepare your party" |
| `mode_selected` | "Mode selected" |
| `mini_games_hint` | "The phone passes between players each turn — …" |
| `responsible_drinking_hint` | "Any drink works — water and soft drinks too. Know your limits and skip whatever you like." (ES: "Cualquier bebida sirve — agua y refrescos también. Conoce tus límites y sáltate lo que quieras.") |
| `start_the_party` | "Start the party" |
| `choose_your_packs` | "Choose your packs" |
| `choose_your_packs_subtitle` | "Curated by PartyPuzl or written by you — …" |
| `pack_group_official` / `_premium` / `_custom` | Group and badge labels |
| `pack_prompts_count` | "%1$d prompts" |
| `pack_unlock` | "Unlock" |
| `pack_unlocked_session` | "Unlocked for this session" |
| `pack_downloading` / `pack_download_failed` / `pack_download_pending` | Premium download states (see [above](#premium-content-download)) |
| `pack_download_error_no_connection` / `_rate_limited` / `_unavailable` | Download failure snackbars |
| `pack_write_first` / `_subtitle` | Custom empty state (tappable — opens the manager) |
| `pack_manage` | "Manage" link on the Custom group label |
| `unlock_sheet_*`, `unlock_option_*`, `unlock_not_now` | Unlock bottom sheet |
| `pack_truth_or_dare`, `pack_movie_night`, … | Pack names |

All have Spanish equivalents in `values-es/strings.xml`.

Removed with the categories section: `pick_what_to_play`, `categories_count_on`, `options_toggle_hint`.

---

## Key Files

| File | Role |
|---|---|
| `ui/views/gameConfig/ui/GameConfigScreen.kt` | Root layout, loading overlay, snackbar, unlock sheet host |
| `ui/views/gameConfig/PackDownloadMessages.kt` | Download failure → snackbar message |
| `ui/views/gameConfig/ui/GameConfigComponents.kt` | `GameModeHeader`, `GameConfigSectionLabel`, `MiniGamesHintBox`, `StartGameButton` |
| `ui/views/gameConfig/ui/SetupHints.kt` | `SetupHint` layout shared by both hints, and `ResponsibleDrinkingHint` (Bar Time / Party Puzl only) |
| `ui/views/gameConfig/ui/QuestionPacksSection.kt` | Official / Premium / Custom groups |
| `ui/views/gameConfig/ui/PackGroupContainers.kt` | `PackGroupLabel` + `PackGroup` chrome shared by all three groups |
| `ui/views/gameConfig/ui/CustomPackGroup.kt` | Custom rows, the Manage link, the tappable empty state |
| `ui/views/gameConfig/PackLabel.kt` | Resource-or-literal pack name |
| `ui/views/gameConfig/ui/PackRow.kt` | Shared pack row layout |
| `ui/views/gameConfig/ui/PackRowControls.kt` | Icon tile, tier badge, check control, Unlock pill, badge colours |
| `ui/views/gameConfig/ui/UnlockChoiceBottomSheet.kt` | Rewarded ad vs. purchase sheet |
| `ui/views/gameConfig/ui/PlayersContainer.kt` | Player list + add-player affordance |
| `ui/views/gameConfig/ui/PlayerDataCard.kt` | Individual player card with edit/delete |
| `ui/views/gameConfig/GameConfigViewModel.kt` | Pack state, unlock flows, download retry, `onStartGame`, `deletePlayer` |
| `ui/views/gameConfig/PackUiMappers.kt` | Catalog and custom rows → `PackUiModel`, including premium counts and `needsDownload` |
| `ui/views/gameConfig/GameConfigState.kt` | `GameConfigState`, `PackUiModel` |
| `data/local/appData/appDataSource/GameOptionsSource.kt` | Singleton holding `currentGameModeNameRes` |
| `data/local/appData/appDataSource/GamePlayersList` | Singleton holding the registered player list |

---

## Related

- [question-packs.md](question-packs.md) — pack tiers, catalog, unlocking, and how packs feed the game
- [navigation.md](navigation.md) — `GameConfigScreen` route parameters and transitions
- [game-mode-visual-identity.md](game-mode-visual-identity.md) — shared element animation for the mode header icon and name
- [game-deal-flow.md](game-deal-flow.md) — where deal selection now happens
- [ads.md](ads.md) — rewarded ad and the `remove_ads` purchase
- [minigames.md](minigames.md) — mini-game system
