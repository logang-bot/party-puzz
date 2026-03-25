# Permission Handling

## Overview

Runtime permission requests follow an MVVM pattern: the **ViewModel** owns the permission dialog queue state, and the **Composable screen** handles the Android system interactions (launching the permission request and showing the dialog).

Currently only `CAMERA` is requested, triggered when the user taps "Take Photo" in `CreatePlayerScreen`.

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
