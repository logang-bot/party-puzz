# Theming

Every colour in the app comes from `ui/theme/`. Nothing outside that package
declares a colour literal, with one documented exception (the photo lightbox in
`PhotoViewerDialog`, which is black in both themes because a cream backdrop
would cast the photos it surrounds).

The palette is a port of the PartyPuzz design system. Light is warm cream with
**cool** teal-black ink; dark is a teal surface ramp. The two used to be
generated from unrelated seeds — light from a yellow/olive one, dark from a teal
one — which is why the old light theme read as muddy: warm brown ink on a warm
cream background separates only by lightness, never by hue.

---

## Where things live

| File | Role |
|---|---|
| `ui/theme/Color.kt` | Raw brand/accent tokens, plus both Material 3 schemes |
| `ui/theme/Theme.kt` | The two `ColorScheme`s, `AppColors`, and `PartyPuzlTheme` |
| `ui/theme/Emphasis.kt` | The `Ink` / `Wash` alpha scale — the only alphas allowed outside this package |
| `ui/theme/AppModifiers.kt` | `Modifier.appCard()`, `Modifier.ctaScrim()` |
| `ui/theme/PageBackground.kt` | The `PageBackground` variants and `TintStrength` |
| `ui/theme/AppBackgrounds.kt` | `Modifier.appBackground()` — how each variant is drawn |
| `ui/theme/PageTintState.kt` | `ReportPageTint`, how a screen colours its own background |
| `ui/theme/PackAccentColors.kt` | What each `PackAccent` name resolves to |

Dynamic colour is **deliberately off**. `PartyPuzlTheme` never calls
`dynamicLightColorScheme`/`dynamicDarkColorScheme`, because the whole palette is a port of a
brand design system — wallpaper-derived colour would replace exactly the thing that makes the
app look like itself. Material Theme Builder output ships dynamic colour switched on by
default; do not "restore" it.

---

## The rule that is easy to get wrong

`colorScheme.primary` is bright teal `#2EB6C6` in **both** themes, with
`onPrimary` = `#062028`. That matches the design's CTA exactly, but it means
`primary` is only valid as a **fill or a stroke**:

```kotlin
// Correct — primary as a fill or stroke
Button(onClick = …)                                   // teal fill, dark ink
Modifier.border(2.dp, MaterialTheme.colorScheme.primary)   // active player ring

// Wrong — bright teal text on the cream background is ~1.9:1
Text(text = …, color = MaterialTheme.colorScheme.primary)

// Correct — an accent that has to stay readable on the page background
Text(text = …, color = MaterialTheme.appColors.brandAccent)
```

`brandAccent` flips per theme (`#9FDCE4` dark / `#0F5B66` light) and is the token
for accent **text and icons**. Both come from the design, which overrides only
`--brand-deep` in its light block and leaves `--brand` alone.

---

## `AppColors`

Material 3 has no slot for several roles the design needs, so `Theme.kt` adds an
`@Immutable` bag provided next to `LocalDarkTheme` and reached as
`MaterialTheme.appColors.*`:

| Token | Purpose |
|---|---|
| `brandAccent` | Accent text/icons on the page background — see above |
| `onAccentSurface` | Ink on a mode/deal/outcome gradient, or on any accent fill. White in both themes |
| `pageScrim` | Full-screen dim behind a dialog or blocking spinner |
| `chipScrim` | Translucent chip over a photo or a coloured card |
| `panelFill`, `panelFillRaised`, `panelFillSelected` | A panel a step above the page — input rows, option chips, stat tiles |
| `cardSurface`, `cardBorder`, `cardShadow` | Consumed by `Modifier.appCard()` |
| `pageBaseBright`, `pageBaseWarm` | The colour a non-default page ramp settles on at the bottom — what a `Modifier.ctaScrim()` on those screens has to fade into |
| `glassTint`, `glassEdge`, `onGlass` | The mini-game countdown's frosted panel |
| `badgeOfficial`, `badgePremium`, `badgeCustom` | Question-pack tier chips |

`pageScrim` is named that way on purpose: `colorScheme.scrim` also exists and is plain opaque
black, so a bare `scrim` in the bag was a trap.

The panel fills stay **translucent** rather than becoming `surfaceContainer*`. An opaque
surface role would sit as a flat patch on the gradients `appBackground` paints; a wash follows
the gradient underneath it. That is also why content on a panel fill still uses
`onBackground` — the page is still what is showing through.

The mode gradients, deal accents and outcome palettes are **not** in the bag.
They are identical in both themes, so they stay as plain top-level values in
`Color.kt` — a CompositionLocal would be ceremony for no benefit.

---

## Named alpha

`Emphasis.kt` holds the only alphas allowed on a colour outside `ui/theme`. Before it existed
the codebase carried **36 distinct hand-typed alpha values across 53 files** — five different
ones for "secondary caption text" alone — so no call site could be compared to any other.

```kotlin
// Wrong — a number nobody can compare to the one on the next screen
color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)

// Correct
color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary)
```

| Scale | Rungs | For |
|---|---|---|
| `Ink` | `Strong` .85 · `Prominent` .75 · `Standard` .65 · `Secondary` .55 · `Tertiary` .45 · `Muted` .35 · `Faint` .25 | Text and icons |
| `Wash` | `Hairline` .15 · `Fill` .12 · `Soft` .08 · `Faint` .05 | Fills and hairlines |

Rungs are 10 % apart on `Ink`. Anything closer is not a distinction the eye makes on these
backgrounds, and the gap is what stops the scale drifting back into 36 values. A value that
falls exactly between two rungs takes the **more opaque** one, so nothing on this scale can
lose contrast.

Both scales keep the alpha rather than resolving to an opaque colour, because this ink sits on
gradients and has to blend against whatever pixel is actually behind it.

An alpha computed at runtime — an animation, a drag fraction — is not on the scale and stays a
plain `copy(alpha = …)`. The mini-games are the only place that happens.

---

## `onBackground` or `onSurface`

They are the same value in both schemes today, which is exactly why the choice used to be
arbitrary — `PartyDetailComponents` reached for both in the same file for the same caption
role. The rule is now mechanical:

- **`onSurface`** — content on a container the app paints: `Modifier.appCard()`, an
  `AlertDialog`, a `ModalBottomSheet`, or anything filled from a `surface*` role.
- **`onBackground`** — content on anything `Modifier.appBackground()` paints, including the
  drawer and the translucent `panelFill*` washes.

The game screen was the biggest offender: ~60 sites used `onSurface` for text sitting straight
on `PageBackground.Flat`.

---

## Two elevation models

Dark and light lift a card off the page differently, and swapping one for the
other does not work — a 4 % white wash is invisible against cream, and a drop
shadow is invisible against near-black. `Modifier.appCard(shape)` picks the right
one:

| | Dark | Light |
|---|---|---|
| Fill | white @ 4 % | opaque `#FFFFFF` |
| Border | white @ 8 % | ink @ 10 % |
| Shadow | none | soft drop shadow |

> The light-mode shadow draws **outside** the composable's bounds. An ancestor
> that clips will cut it off; in that case fall back to a stronger border.

`appCard` is deliberately not applied to surfaces that animate their own border
for selection or focus (`LastPartyCard`, `NameOptionsContainer`) — it would fight
the animation.

---

## Sticky bottom CTAs

Every screen with a pinned primary action pins it **over** the scrolling content,
not in a reserved strip below it. The design writes that strip as

```css
background: linear-gradient(180deg, transparent, var(--bg-0) 50%);
```

so the page keeps going behind the button and dissolves into the page base
halfway down. `Modifier.ctaScrim(baseColor)` is that gradient. The recipe at every
call site is the same:

```kotlin
StartGameButton(
    …,
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .ctaScrim()
        .navigationBarsPadding()
        .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp)
)
```

with the scrolling content carrying `bottom = 96.dp` so its last row can still be
reached. Two ordering rules are load-bearing:

- `ctaScrim()` goes **before** `navigationBarsPadding()`, so the fade paints behind
  the system bar instead of stopping short of the screen edge.
- It goes on the **caller's** modifier, outside the `alpha` the button applies to
  itself when disabled — otherwise the fade dims along with the button.

`baseColor` must be whatever the screen's own background settles on at the bottom.
It defaults to `colorScheme.background`, which is right for every
`PageBackground.Tinted` screen; create-player rides the warm ramp and passes
`appColors.pageBaseWarm` instead.

> The fade is to `baseColor.copy(alpha = 0f)`, never `Color.Transparent` —
> the latter is transparent *black*, and dragging it through the ramp puts a grey
> cast on the cream light theme.

**Screens using it:** game config, the four custom-pack screens, create-player.

---

## Backgrounds

There is no single app background. The design gives all but three screens their
own, and the shape is nearly always the same: a coloured wash at the top fading
into the **page base**, which is `colorScheme.background` — the design's
`--bg-0`, `#FBF6EE` in light and `#0B1F24` in dark.

Do not confuse the page base with `backgroundGradientLightStart` (`#FFF5E6`).
That is the *top stop of the warm ramp*, which only Home, the parties list and
create-player fade through. Painting it as a page colour is what made every
screen read as peach.

`Modifier.appBackground(PageBackground)` in `ui/theme/AppBackgrounds.kt` owns all
of this. The variants are named for the role a screen plays, not for their
geometry, so a caller passes at most a tint and never resolves a colour itself:

| Variant | Used by |
|---|---|
| `Home(tint)` | Home — top glow tinted by the carousel's visible mode, over the warm ramp |
| `Warm` | Parties list, create player (dark falls back to the default teal glow) |
| `Bright` | Settings — warm at the top, fading to `appColors.pageBaseBright` |
| `TintedBright(tint)` | Party detail |
| `Tinted(tint, strength, baseStop)` | Game config, the pack screens, the deal picker |
| `TintedGlow(tint, …)` | Challenge reveal, photo moment |
| `Flat` | The game shell and the outcome screens, which paint their own |
| `Drawer` | The navigation drawer sheet |

`TintStrength` maps the alpha suffixes the design's CSS puts on its gradient
stops (`40`/`35`/`30`/`25`/`22`), so a value there traces back to a real design
number rather than to taste.

### Where each screen's variant is decided

`pageBackgroundFor(destination, tint)` in `navigation/PageBackgrounds.kt` maps
route → variant, and it is applied **once** on the root `Scaffold` in
`HomeNavigation.kt` (with `containerColor = Color.Transparent`). Keeping it there
rather than on each screen is deliberate: the background has to paint behind the
app bar too, and a per-screen background leaves a seam under the bar.

Five screens tint from data a route cannot see — the mode the home carousel is
showing, a party's last mode, a pack's spice accent, the kind of game deal being
written. They call `ReportPageTint(colour)` (`ui/theme/PageTintState.kt`), which
writes into a `PageTintState` the root scaffold provides and reads back:

```kotlin
ReportPageTint(uiState.pack?.spice?.accent)
```

A tint cannot outlive the screen that set it, because `ReportPageTint` clears it
on disposal — and only if nothing else reported since, so the arriving screen
wins the window where both are composed. That used to be a `setPageTint` callback
on every screen signature plus a route-string comparison; the invariant is now
structural rather than a string compare, and the screens carry no background
parameter at all.

The game screen is the one screen that paints its own, because its background
follows the turn rather than the route: `rememberGameBackground(uiState)` in
`GameScreenTheme.kt`. See
[game-mode-visual-identity.md](game-mode-visual-identity.md).

Previews use `appBackground(…)` too, so an artboard shows what the screen really
sits on rather than a hand-typed hex.

---

## Adding a colour

1. If it is theme-invariant (a gradient stop, a fixed accent), add a top-level
   `val` to the brand/accent block in `Color.kt`.
2. If it flips per theme and Material has a slot for it, set that slot in both
   schemes.
3. Only if it flips **and** Material has no slot, add a field to `AppColors` and
   give it a value in both `LightAppColors` and `DarkAppColors`.

Before reaching for a new colour, check it is not an existing one at a different **emphasis** —
that is what `Ink` and `Wash` are for.

The check before committing:

```bash
git grep -nE 'Color\(0x|Color\.(White|Black)|copy\(alpha *= *[0-9]|\.copy\(alpha *= *(if|when)' \
  -- 'app/src/main/java' | grep -v '/ui/theme/'
```

It should return only the three `PhotoViewerDialog` lines. The earlier version of this check
looked for hex literals alone, which had passed for a long time while 183 hand-typed alphas
accumulated behind it — a derived colour is a literal one step removed.

---

## Data does not know about colour

Nothing under `data/` imports `androidx.compose.ui.graphics.Color`. The catalog names an
accent, `ui/theme` decides what it looks like:

```kotlin
// data/models/PackAccent.kt   — a name
enum class PackAccent { TEAL, PINK, CORAL, VIOLET, SKY, ROSE, LIME, YELLOW }

// ui/theme/PackAccentColors.kt — what the name looks like
val PackAccent.color: Color get() = when (this) { PackAccent.TEAL -> BrandTeal; … }
```

`gameModeTheme(gameModeNameRes)` in `ui/common/GameModeTheme.kt` is the same split for game
modes, and was the pattern this followed.

---

## Splash

The splash window is themed in XML, not Compose: `res/values/colors.xml` and
`res/values-night/colors.xml` define `splash_background`, which must stay in sync
with `backgroundGradientLightStart` and `backgroundGradientDarkEnd`.

**Known mismatch:** `themes.xml` uses `Theme.AppCompat.DayNight`, so the splash
follows the *system* dark mode while Compose follows the persisted `ThemeMode`.
They disagree when the user forces Light or Dark. Fixing it needs the preference
read before the splash is drawn.

## Vector drawables

Five drawables hardcode brand hex that duplicates `Color.kt` with nothing linking
the two — a sync hazard the other 37 avoid by using `@android:color/white` and
letting the caller tint them:

| Drawable | Hex | Duplicates |
|---|---|---|
| `ic_bar.xml`, `ic_couples.xml` | `#ff5b8a` | `AccentPink` |
| `ic_partypuzz.xml` | `#a8e063` | `AccentLime` |
| `ic_standard.xml` | `#2EB6C6` | `BrandTeal` |
| `ic_splash_icon.xml` | `#fff5e6`, `#ffd25a` | `backgroundGradientLightStart`, `AccentYellow` |

They are mode icons drawn on their own gradient, so they never need to follow the
theme — but if a brand colour moves in `Color.kt`, these do not.

---

## Related

- [game-mode-visual-identity.md](game-mode-visual-identity.md) — Per-mode gradients and icons
- [outcome-presentation.md](outcome-presentation.md) — Reward / punishment palettes
- [minigames.md](minigames.md) — The countdown overlay's frosted panel
