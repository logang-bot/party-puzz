# Game Mode Visual Identity

Each game mode has a fixed visual identity — a gradient palette and a vector icon — that appears consistently across every screen that references a game mode. These values are **not theme-dependent**: the same colors and icon are used regardless of the active Material3 theme or dark/light mode.

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

| Mode | Gradient (start → end) | Icon |
|---|---|---|
| Standard | `#2EB6C6` → `#1C4F5C` | `ic_standard` |
| Bar | `#FF8A5C` → `#FF5B8A` | `ic_bar` |
| Couples | `#FF5B8A` → `#8B6CFF` | `ic_couples` |
| Party Puzz | `#A8E063` → `#1C7A87` | `ic_partypuzz` |
| *(fallback)* | `#2A4060` → `#162840` | `ic_standard` |

All four icons are **XML vector drawables** in `res/drawable/`. The `ic_standard` star path was scaled to fill its 16×16 viewport (the original 24×24 viewport left 33% empty space, making the star visually smaller than the other icons).

---

## Icon Rules by Screen

| Screen / Component | Icon size | Position | Tint | Alpha | Notes |
|---|---|---|---|---|---|
| `GameModeCard` | `fillMaxHeight(0.42f)` + `aspectRatio(1f)` | `TopEnd` + `offset(x=56, y=-48)` | `Color.White` | `0.25f` | Clipped at top-right corner by the card's `clip(RoundedCornerShape(20.dp))` |
| `PartyCard` | `size(150.dp)` | `TopEnd` + `offset(x=48, y=-40)` | `Color.White` | `0.25f` | Same corner treatment as `GameModeCard` |
| `LastPartyCard` | `size(32.dp)` | Centered inside a 52 dp gradient box | `Color.White` | *(none)* | Icon sits fully inside the colored thumbnail |
| `GameConfigScreen` | `size(72.dp)` | Inline in the header Row | *(none — original colors)* | *(none)* | Displayed as a bare image, no background container |

> **Corner icon effect** (`GameModeCard`, `PartyCard`): the icon is placed at `Alignment.TopEnd` and then shifted further outside via `offset`. The parent Box's `clip(RoundedCornerShape(...))` crops whatever extends beyond the card boundary, creating the intentional partially-visible corner decoration.

---

## Gradient Usage

Gradients use `Brush.linearGradient(theme.gradientColors)` (default top-left → bottom-right direction).

### `GameModeCard`

The gradient colors have `alpha = 0.9f` applied before being passed to the brush. This adds slight transparency to the card background (letting the app background show through faintly) without affecting the text or icon, which remain fully opaque:

```kotlin
val cardGradient = theme.gradientColors.map { it.copy(alpha = 0.9f) }
Modifier.background(Brush.linearGradient(cardGradient))
```

Text on gradient backgrounds is always fixed `Color.White` (with alpha variants for supporting copy) and is never driven by `MaterialTheme.colorScheme`.

### `PartyCard`

Gradient applied at full opacity directly on the card `Box`. All text uses fixed `Color.White` and `Color.White.copy(alpha = ...)`.

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
| Party Puzz | `img_partypuzz_mode_illustration` | `ic_partypuzz` |

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
