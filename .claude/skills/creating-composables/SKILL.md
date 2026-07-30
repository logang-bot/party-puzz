---
name: creating-composables
description: Required whenever you create a composable function - a screen, dialog, bottom sheet, card, row, section or any other piece of UI. Covers how to structure the composable and the light and dark @Preview functions that ship with it, in the same file and the same change. The preview conventions - theme wrapper, annotation parameters, naming, and the state-hoisting step for screens that take a ViewModel - are project-specific and you will get them wrong from memory, so read this before writing the composable, not after.
---

When creating a composable take into consideration these guidelines

1. **Always put a modifier as the first optional parameter**: If an optional modifier parameter is needed always put as the first optional one
2. **Split into child composables**: The size of a composable function is not limited but try to split into small composables functions
3. **Identify business logic**: If you identify a business logic inside a composable function move it to a new ViewModel file and link it to the composable
4. **Every composable that renders UI ships with previews**: write them in the same change as the
   composable, never as a follow-up. Only non-rendering composables are exempt — value-returning
   helpers and side-effect-only composables. The rules below are not guessable, so follow them
   exactly:
   - **Same file as the target**: the preview lives in the same file as its composable. If adding it
     pushes the file past its size limit, split the file rather than moving the preview elsewhere.
   - **Light and dark variants**: every preview needs two, using
     `PartyPuzlTheme(themeMode = ThemeMode.LIGHT)` and `PartyPuzlTheme(themeMode = ThemeMode.DARK)`.
     `ThemeMode` comes from `com.restrusher.partypuzl.data.preferences.ThemeMode`.
   - **Name and annotate consistently**: previews are `private fun` named `<Target>LightPreview` /
     `<Target>DarkPreview`, annotated with
     `@Preview(name = "Target – Light", showBackground = true, widthDp = 360)` (en dash, matching the
     existing previews in the project).
   - **Wrap the content in the app background**: put the target inside
     `Box(Modifier.appBackground().padding(16.dp))`. The real background is applied once at the nav
     scaffold, so previews are the exception that calls `appBackground()` directly.
   - **Never preview a composable that takes a ViewModel**: `hiltViewModel()` cannot resolve in the
     preview renderer, so the preview fails to render. Extract a stateless
     `XxxContent(state, callbacks...)` composable holding everything drawable, leave only the state
     collection and `LaunchedEffect`s in `XxxScreen`, and preview the content.
   - **Fake data lives next to the previews**: a `private val` above the preview functions, or a
     package-level preview-data file when several files in the feature need the same sample, as in
     `ui/views/customPacks/CustomPackPreviewData.kt`.

Example:

```kotlin
@Preview(name = "MetaChip – Light", showBackground = true, widthDp = 360)
@Composable
private fun MetaChipLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().padding(16.dp)) {
            MetaChip(label = "12 entries")
        }
    }
}
```
