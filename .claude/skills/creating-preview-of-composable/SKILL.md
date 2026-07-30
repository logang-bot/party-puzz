---
name: creating-preview-of-composable
description: Read BEFORE writing or editing any @Preview function, or before making a composable previewable. Do not write a preview from memory - this project's previews have required theme wrappers, annotation parameters, naming and file-placement rules, plus a state-hoisting step for screens, that you will get wrong if you guess. Applies whenever you are about to type @Preview in a Kotlin file, however small the change. Covers @Preview functions; creating-composables covers ordinary composables.
---

When creating a new preview composable follow these guidelines.

1. **Always put the preview in the same file as the target composable**: The preview needs to be in
   the same file as its composable. If adding the preview pushes the file past the size limit in
   `creating-files-or-classes`, split the file rather than moving the preview elsewhere.
2. **Create variants for light and dark mode**: Every new preview needs two variants, using
   `PartyPuzlTheme(themeMode = ThemeMode.LIGHT)` and `PartyPuzlTheme(themeMode = ThemeMode.DARK)`.
   `ThemeMode` comes from `com.restrusher.partypuzl.data.preferences.ThemeMode`.
3. **Name and annotate consistently**: previews are `private fun` named `<Target>LightPreview` /
   `<Target>DarkPreview`, annotated with
   `@Preview(name = "Target – Light", showBackground = true, widthDp = 360)` (en dash, matching the
   existing previews in the project).
4. **Wrap the content in the app background**: put the target inside
   `Box(Modifier.appBackground().padding(16.dp))`. The real background is applied once at the nav
   scaffold, so previews are the exception that calls `appBackground()` directly.
5. **Never preview a composable that takes a ViewModel**: `hiltViewModel()` cannot resolve in the
   preview renderer, so the preview fails to render. Extract a stateless
   `XxxContent(state, callbacks...)` composable holding everything drawable, leave only the state
   collection and `LaunchedEffect`s in `XxxScreen`, and preview the content.
6. **Fake data lives next to the previews**: a `private val` above the preview functions, or a
   package-level preview-data file when several files in the feature need the same sample.

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
