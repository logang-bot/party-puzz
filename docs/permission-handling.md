# Permission Handling

## Overview

Runtime permission requests follow an MVVM pattern: the **ViewModel** owns the permission dialog queue state, and the **Composable screen** handles the Android system interactions (launching the permission request and showing the dialog).

`CAMERA` is the only permission currently requested. It is used in two places:

| Location | Pattern | Purpose |
|---|---|---|
| `CreatePlayerScreen` | Full dialog queue flow (see below) | Take a player profile photo |
| `GameScreen` | Silent inline check (see below) | Capture a party album photo during a game session |
| `PartyDetailScreen` | `RequestPermission` launcher | Grant camera access to enable the in-game photo feature |

---

## Components

### `PermissionDialog` (`ui/common/PermissionDialog.kt`)

A reusable `@Composable` `AlertDialog` that renders a rationale or permanent-decline message for any permission.

| Prop | Type | Purpose |
|---|---|---|
| `permissionTextProvider` | `PermissionTextProvider` | Supplies the body text string resource ID |
| `isPermanentlyDeclined` | `Boolean` | Switches between rationale and "go to settings" copy |
| `onDismiss` | `() -> Unit` | Dismiss without action |
| `onOkClick` | `() -> Unit` | Re-request permission (shown when not permanently declined) |
| `onGoToAppSettingsClick` | `() -> Unit` | Open app settings (shown when permanently declined) |

Button label and body copy are resolved from string resources via `stringResource()`:

```
isPermanentlyDeclined = false  →  "OK" button  +  rationale copy
isPermanentlyDeclined = true   →  "Grant permission" button  +  settings-redirect copy
```

### `PermissionTextProvider` interface

```kotlin
interface PermissionTextProvider {
    @StringRes
    fun getDescription(isPermanentlyDeclined: Boolean): Int
}
```

Each permission has its own implementation that returns the appropriate `@StringRes` ID. This keeps display logic out of the ViewModel and dialog composable.

| Implementation | Permission |
|---|---|
| `CameraPermissionTextProvider` | `Manifest.permission.CAMERA` |

String resources used:

| Resource | Value |
|---|---|
| `R.string.camera_permission_rationale` | Rationale shown on first/subsequent denials |
| `R.string.camera_permission_permanently_declined` | Copy shown after permanent denial |

### `CreatePlayerViewModel` — queue state

```kotlin
val visiblePermissionDialogQueue = mutableStateListOf<String>()
```

A `SnapshotStateList` of permission strings that currently need a dialog shown. Compose automatically recomposes when it changes.

| Method | Behaviour |
|---|---|
| `onPermissionResult(permission, isGranted)` | If denied and not already queued, adds the permission string to the queue |
| `dismissDialog()` | Removes the first item in the queue |

### `CreatePlayerScreen` — system interactions

The screen owns the two `ActivityResultLauncher`s that interact with the Android system:

```
multiplePermissionResultLauncher   →   RequestMultiplePermissions contract
cameraLauncher                     →   TakePicture contract
```

**Permission check flow (on "Take Photo" tap):**

```
User taps "Take Photo"
    │
    ├─ CAMERA granted?  ──Yes──►  cameraLauncher.launch(uri)
    │
    └─ No  ──────────────────►  multiplePermissionResultLauncher.launch(permissionsToRequest)
                                        │
                                        └─ Result callback
                                               │
                                               └─ viewModel.onPermissionResult(permission, isGranted)
                                                       │
                                                       └─ denied?  ──►  added to visiblePermissionDialogQueue
```

**Dialog rendering:**

```kotlin
dialogQueue
    .reversed()          // most-recently-added permission shown first
    .forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.CAMERA -> CameraPermissionTextProvider()
                else -> return@forEach      // unknown permissions are silently skipped
            },
            isPermanentlyDeclined = !mainActivity!!.shouldShowRequestPermissionRationale(permission),
            onDismiss = viewModel::dismissDialog,
            onOkClick = {
                viewModel.dismissDialog()
                multiplePermissionResultLauncher.launch(arrayOf(permission))
            },
            onGoToAppSettingsClick = { mainActivity.openAppSettings() }
        )
    }
```

`shouldShowRequestPermissionRationale()` returns `false` both before the permission has ever been requested **and** after the user has permanently declined it. The dialog is only shown after a failed request (i.e., when the queue is non-empty), so `false` at that point reliably means permanent denial.

---

---

## In-Game Camera Check (`GameScreen`)

The game screen uses a simpler, silent permission check — no dialog queue, no rationale. The check happens at the exact moment the user taps "Take a photo" on the camera request card:

```
User taps "Take a photo" (onCameraRequested callback)
    │
    ├─ CAMERA granted?  ──Yes──►  cameraLauncher.launch(uri)
    │
    └─ No  ──────────────────►  viewModel.onCameraRequestDismissed()
                                    (camera card dismissed silently, deal resets)
```

This is intentional: the camera request card is an **opportunistic** prompt. If the permission is missing the moment is simply skipped rather than interrupting the game with a permission dialog. Users who want the feature can grant the permission from `PartyDetailScreen`.

**Launchers in `GameScreen`:**

```
cameraUri       →  FileProvider URI for a temp file in cacheDir
cameraLauncher  →  TakePicture contract
                       success  →  viewModel.onPhotoCaptured(uri)
                       failure  →  viewModel.onCameraRequestDismissed()
```

---

## PartyDetailScreen Permission Grant

`PartyDetailScreen` shows the photo album section. If the camera permission is not yet granted, a button is shown instead of the album:

```
hasCameraPermission == false
    └─ Button: "Allow camera to capture party moments"
            └─ permissionLauncher.launch(Manifest.permission.CAMERA)
                    └─ result updates hasCameraPermission local state
```

`hasCameraPermission` is a local `var` initialised with `ContextCompat.checkSelfPermission` on first composition. No ViewModel involvement — the permission state only affects which UI variant is shown.

---

## Adding a New Permission

1. Add the permission to `permissionsToRequest` in `CreatePlayerScreen.kt`.
2. Create a new `PermissionTextProvider` implementation with rationale and permanent-decline strings.
3. Add the string resources to `res/values/strings.xml`.
4. Add a `when` branch in the dialog-rendering loop in `CreatePlayerScreen` mapping the `Manifest.permission.*` constant to the new provider.
5. Add the permission to `AndroidManifest.xml`.

---

## String Resources

All user-visible strings for the permission UI live in `res/values/strings.xml`:

| Key | Usage |
|---|---|
| `permission_required` | Dialog title |
| `permission_grant` | Confirm button when permanently declined |
| `ok` | Confirm button when rationale is shown |
| `camera_permission_rationale` | Camera body text — rationale case |
| `camera_permission_permanently_declined` | Camera body text — permanent-decline case |
