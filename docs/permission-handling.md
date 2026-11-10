# Permission Handling

## Overview

Runtime permission requests are handled in the composable layer: launchers and rationale dialogs live in the screen (or a `remember…` helper such as `rememberCameraPermissionRequester`), and view models never see permission state.

Two permissions are currently requested at runtime:

| Permission | API scope | Location | Pattern | Purpose |
|---|---|---|---|---|
| `CAMERA` | All | `CreatePlayerScreen` | `rememberCameraPermissionRequester` (see below) | Take a player profile photo |
| `CAMERA` | All | `GameScreen` | Silent inline check (see below) | Capture a party album photo during a game session |
| `CAMERA` | All | `PartyDetailScreen` | `RequestPermission` launcher | Grant camera access to enable the in-game photo feature |
| `WRITE_EXTERNAL_STORAGE` | API ≤ 28 only | `PartyDetailScreen` | `RequestPermission` launcher + pending-path pattern | Save a photo to the device gallery from the photo viewer |

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

### `rememberCameraPermissionRequester` (`ui/common/CameraPermission.kt`)

`CreatePlayerScreen` asks for the camera through this composable helper. It owns its own
`RequestPermission` launcher and dialog state, so no ViewModel is involved.

```
User taps "Take Photo"  →  cameraPermission.request()
    │
    ├─ CAMERA granted?  ──Yes──►  onGranted()  →  isCameraOpen = true  →  InAppCameraDialog
    │
    └─ No  ──►  launcher.launch(CAMERA)
                    │
                    ├─ granted  ──►  onGranted()
                    │
                    └─ denied   ──►  PermissionDialog
                                        ├─ OK                ──►  request again
                                        └─ Grant permission  ──►  app settings (permanent denial)
```

`isPermanentlyDeclined` is `!shouldShowRequestPermissionRationale(CAMERA)`. That call returns
`false` both before the first request and after a permanent denial. The dialog only shows after a
failed request, so `false` at that point reliably means permanent denial.

See [in-app-camera.md](in-app-camera.md) for what happens once the camera opens.

---

## In-Game Camera Check (`GameScreen` + `GameScreenViewModel`)

The in-game camera permission is checked **twice** — silently both times, with no dialog queue or rationale.

**Check 1 — at deal start (`GameScreenViewModel`):**

`pendingCameraRequest` is only set to `true` when the camera permission is already granted at the moment the challenge card appears. If permission is missing, the camera request card will never show for that deal.

```
CHALLENGE_SHOWN phase begins
    │
    ├─ partyId != null AND CAMERA granted?  ──Yes──►  pendingCameraRequest = true
    │
    └─ Either condition false  ───────────►  pendingCameraRequest = false  (card never shows)
```

**Check 2 — on button tap (`GameScreen`):**

A second check runs when the user taps "Take a photo" as a safety net in case the permission was revoked mid-session.

```
User taps "Take a photo" (onCameraRequested callback)
    │
    ├─ CAMERA granted?  ──Yes──►  isCameraOpen = true  →  InAppCameraDialog
    │
    └─ No  ──────────────────►  viewModel.onCameraRequestDismissed()
                                    (camera card dismissed silently, deal resets)
```

This is intentional: the camera request card is an **opportunistic** prompt. Users who want the feature can grant the permission from `PartyDetailScreen`.

**Camera result in `GameScreen`:**

```
InAppCameraDialog
    onCaptured(uri)  →  viewModel.onPhotoCaptured(uri)
    onDismiss        →  viewModel.onCameraRequestDismissed()
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

---

## Gallery Save Permission (`PartyDetailScreen`)

Saving a photo to the device gallery from the photo viewer requires `WRITE_EXTERNAL_STORAGE` on **API 24–28** only. On API 29+ (scoped storage), `MediaStore` writes require no permission.

The permission is declared in `AndroidManifest.xml` with `android:maxSdkVersion="28"` so it is never requested on modern devices.

### Runtime flow (API 24–28 only)

```
User taps "Save" in PhotoViewerDialog
    │
    ├─ API >= 29  ──────────────────────────────►  viewModel.downloadPhoto(path)
    │
    └─ API < 29
            │
            ├─ WRITE_EXTERNAL_STORAGE granted?  ──Yes──►  viewModel.downloadPhoto(path)
            │
            └─ No  ──►  pendingDownloadPath = path
                        writePermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
                                │
                                └─ granted  ──►  viewModel.downloadPhoto(pendingDownloadPath)
                                   denied   ──►  pendingDownloadPath = null  (silent no-op)
```

`pendingDownloadPath` is a local `var` in `PartyDetailScreen` that holds the photo path across the permission request so the download can be retried automatically on grant.

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
