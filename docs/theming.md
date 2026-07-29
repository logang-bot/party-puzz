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
| `ui/theme/AppModifiers.kt` | `Modifier.appBackground()` and `Modifier.appCard()` |

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
| `onAccentSurface` | Ink on a mode/deal/outcome gradient. White in both themes |
| `scrim` | Full-screen dim behind a dialog or blocking spinner |
| `chipScrim` | Translucent chip over a photo or a coloured card |
| `cardSurface`, `cardBorder`, `cardShadow` | Consumed by `Modifier.appCard()` |
| `glassTint`, `glassEdge`, `onGlass` | The mini-game countdown's frosted panel |
| `badgeOfficial`, `badgePremium`, `badgeCustom` | Question-pack tier chips |

The mode gradients, deal accents and outcome palettes are **not** in the bag.
They are identical in both themes, so they stay as plain top-level values in
`Color.kt` — a CompositionLocal would be ceremony for no benefit.

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

## Backgrounds

`Modifier.appBackground()` is applied once, on the root `Scaffold` in
`HomeNavigation.kt` (with `containerColor = Color.Transparent`), so every route
inherits it:

- **Light** — vertical `#FFF5E6` → `#FFE6D6` → `#FFD9C2`
- **Dark** — a radial teal glow anchored at top centre, `#18424A` → `#0F2A30` →
  `#0B1F24`, mirroring the design's `radial-gradient(120% 60% at 50% 0%, …)`

The game screen overrides this with a mode-tinted variant — see
[game-mode-visual-identity.md](game-mode-visual-identity.md).

Previews use `appBackground()` too, so an artboard shows what the screen really
sits on rather than a hand-typed hex.

---

## Adding a colour

1. If it is theme-invariant (a gradient stop, a fixed accent), add a top-level
   `val` to the brand/accent block in `Color.kt`.
2. If it flips per theme and Material has a slot for it, set that slot in both
   schemes.
3. Only if it flips **and** Material has no slot, add a field to `AppColors` and
   give it a value in both `LightAppColors` and `DarkAppColors`.

The check before committing: `Color(0x`, `Color.White` and `Color.Black` should
return hits only under `ui/theme/`.

---

## Splash

The splash window is themed in XML, not Compose: `res/values/colors.xml` and
`res/values-night/colors.xml` define `splash_background`, which must stay in sync
with `backgroundGradientLightStart` and `backgroundGradientDarkEnd`.

**Known mismatch:** `themes.xml` uses `Theme.AppCompat.DayNight`, so the splash
follows the *system* dark mode while Compose follows the persisted `ThemeMode`.
They disagree when the user forces Light or Dark. Fixing it needs the preference
read before the splash is drawn.

---

## Related

- [game-mode-visual-identity.md](game-mode-visual-identity.md) — Per-mode gradients and icons
- [outcome-presentation.md](outcome-presentation.md) — Reward / punishment palettes
- [minigames.md](minigames.md) — The countdown overlay's frosted panel
