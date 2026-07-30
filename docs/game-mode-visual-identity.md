# Game Mode Visual Identity

Each game mode has a fixed visual identity — a gradient palette and a vector icon — that appears consistently across every screen that references a game mode. These values are **not theme-dependent**: the same colors and icon are used regardless of the active Material3 theme or dark/light mode. That is exactly why they live as plain top-level values in `ui/theme/Color.kt` rather than in the per-theme `AppColors` bag — see [theming.md](theming.md).

---

## Central Source of Truth — `GameModeTheme.kt`

**Location:** `ui/common/GameModeTheme.kt`

```kotlin
data class GameModeTheme(
    val gradientColors: List<Color>,   // two-stop linear gradient
    @DrawableRes val iconId: Int
)

fun gameModeTheme(gameModeNameRes: Int?): GameModeTheme
```

Four private constants define each mode. Any component that knows a game mode's **string resource ID** can look up the full visual identity in one call:

```kotlin
val theme = gameModeTheme(gameModeNameRes)
// theme.gradientColors  → gradient stops
// theme.iconId          → drawable resource
```

---

## Mode Definitions

Gradient stops are named tokens from `ui/theme/Color.kt`, not literals — see [theming.md](theming.md).

| Mode | Gradient (start → end) | Hex | Icon |
|---|---|---|---|
| Standard | `BrandTeal` → `BrandTealShade` | `#2EB6C6` → `#1C4F5C` | `ic_standard` |
| Bar | `AccentCoral` → `AccentPink` | `#FF8A5C` → `#FF5B8A` | `ic_bar` |
| Couples | `AccentPink` → `AccentViolet` | `#FF5B8A` → `#8B6CFF` | `ic_couples` |
| Party Puzl | `AccentLime` → `BrandTealDeep` | `#A8E063` → `#1C7A87` | `ic_partypuzz` |
| *(fallback)* | `ModeFallbackStart` → `ModeFallbackEnd` | `#2A4060` → `#162840` | `ic_standard` |

All four icons are **XML vector drawables** in `res/drawable/`. The `ic_standard` star path was scaled to fill its 16×16 viewport (the original 24×24 viewport left 33% empty space, making the star visually smaller than the other icons).

---

## Icon Rules by Screen

| Screen / Component | Icon size | Position | Tint | Alpha | Notes |
|---|---|---|---|---|---|
| `GameModeCard` | `fillMaxHeight(0.42f)` + `aspectRatio(1f)` | `TopEnd` + `offset(x=56, y=-48)` | `appColors.onAccentSurface` | `0.25f` | Clipped at top-right corner by the card's `clip(RoundedCornerShape(20.dp))` |
| `PartyCard` | `size(150.dp)` | `TopEnd` + `offset(x=48, y=-40)` | `appColors.onAccentSurface` | `0.25f` | Same corner treatment as `GameModeCard` |
| `LastPartyCard` | `size(32.dp)` | Centered inside a 52 dp gradient box | `appColors.onAccentSurface` | *(none)* | Icon sits fully inside the colored thumbnail |
| `GameConfigScreen` | `size(72.dp)` | Inline in the header Row | *(none — original colors)* | *(none)* | Displayed as a bare image, no background container |

> **Corner icon effect** (`GameModeCard`, `PartyCard`): the icon is placed at `Alignment.TopEnd` and then shifted further outside via `offset`. The parent Box's `clip(RoundedCornerShape(...))` crops whatever extends beyond the card boundary, creating the intentional partially-visible corner decoration.

---

## Gradient Usage

Gradients use `Brush.linearGradient(theme.gradientColors)` (default top-left → bottom-right direction).

### `GameModeCard`

The gradient colors are washed to `Ink.Strong` before being passed to the brush. This adds slight transparency to the card background (letting the app background show through faintly) without affecting the text or icon, which remain fully opaque:

```kotlin
val cardGradient = theme.gradientColors.map { it.ink(Ink.Strong) }
Modifier.background(Brush.linearGradient(cardGradient))
```

Text on gradient backgrounds is always `MaterialTheme.appColors.onAccentSurface` (at an `Ink` rung for supporting copy) and is never driven by `MaterialTheme.colorScheme`. That token is white in both themes — it exists so the intent reads as "ink on an accent fill" rather than an unexplained literal.

### `PartyCard`

Gradient applied at full opacity directly on the card `Box`. All text uses `appColors.onAccentSurface`, at an `Ink` rung for supporting copy. The photo-count chip sits on `appColors.chipScrim` at `Ink.Faint`.

### `LastPartyCard`

Gradient applied to the 52 dp square thumbnail box only. Party name, subtitle, and date are displayed outside the gradient area and use `MaterialTheme.colorScheme` colors normally.

---

## `GameModesDatasource`

`GameModesDatasource.gameModesList` was updated to reference the new vector icons instead of the deleted PNG illustrations:

| Mode | Old `imageId` | New `imageId` |
|---|---|---|
| Standard | `img_standard_illustration` | `ic_standard` |
| Couples | `img_couples_mode_illustration` | `ic_couples` |
| Bar | `img_bar_mode_illustration` | `ic_bar` |
| Party Puzl | `img_partypuzz_mode_illustration` | `ic_partypuzz` |

`GameMode.imageId` is the value forwarded through navigation as `gameModeImage` to `GameConfigScreen`.

---

## Key Files

| File | Role |
|---|---|
| `ui/common/GameModeTheme.kt` | Single source of truth: `GameModeTheme` data class + `gameModeTheme()` lookup |
| `data/local/appData/appDataSource/GameModesDatasource.kt` | Game mode list; `imageId` now points to XML icons |
| `res/drawable/ic_standard.xml` | Star icon (viewport adjusted to 16×16 to match other icons' fill ratio) |
| `res/drawable/ic_bar.xml` | Martini glass icon |
| `res/drawable/ic_couples.xml` | Heart icon |
| `res/drawable/ic_partypuzz.xml` | Trophy / cup icon |
| `ui/views/home/GameModeCard.kt` | Pager card: gradient bg, corner icon, white text |
| `ui/views/home/LastPartyCard.kt` | Home screen party summary: gradient thumbnail, white icon |
| `ui/views/parties/PartyCard.kt` | Parties list card: gradient bg, corner icon |
| `ui/views/gameConfig/ui/GameConfigScreen.kt` | Config screen header: bare icon (no background) |
| `ui/views/game/gameScreen/GameScreenTheme.kt` | Game screen background: mode gradient tinted over the app background |

---

## Game screen background

The in-game background is the one place the mode palette is used as an atmosphere rather than an accent. It is also the one screen that does not take its background from its route, because it follows the **turn** instead: `rememberGameBackground(uiState)` in `GameScreenTheme.kt` returns a `PageBackground` per deal phase, which `Modifier.appBackground()` then draws like any other screen's.

| Phase | Variant | Tint |
|---|---|---|
| A mode event is showing (bar deal, outcome) | `Flat` | — the outcome screens paint their own |
| Photo moment | `TintedGlow` at `TintStrength.Prominent`, centred 20 % down, base by 70 % | The **mode**'s first gradient stop |
| Challenge revealed | `TintedGlow` | The **deal**'s own tone — a truth reads teal, a dare pink |
| Between turns | `Tinted`, base by 55 % | The mode's first gradient stop |

The effect is that Bar mode plays under a warm orange sky and Couples under a pink one, while the surface colours underneath stay the standard themed ones. Before this, every mode played on the same fixed navy.

Note the tint switches source mid-turn: the reveal follows the *deal* rather than the mode, so picking Truth or Dare recolours the screen. See [theming.md](theming.md) for the variants themselves and `TintStrength`.

---

## Related

- [theming.md](theming.md) — Colour tokens, `AppColors`, and the two elevation models
- [game-deal-flow.md](game-deal-flow.md) — Where the background is applied
- [outcome-presentation.md](outcome-presentation.md) — Per-mode reward and punishment theming
