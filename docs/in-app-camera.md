# In-App Camera

Every photo in the app — player avatars in Create Player and party photos during a game — is taken
with an in-app CameraX camera, not the device's camera app.

## Why not the system camera

The app used to launch the system camera with `ActivityResultContracts.TakePicture()`. Handing off to
another app caused two problems:

- **Stutter.** Starting the OEM camera app is slow on many devices, and the round trip shows.
- **Lost game sessions.** While the camera app was in front, low-RAM devices often killed PartyPuzl.
  The game session lives only in memory, so coming back reopened the app on Home (see
  [navigation.md](navigation.md#process-restart-opens-on-home)).

An in-app camera keeps PartyPuzl in the foreground, so neither happens. It is also a prerequisite for
face filters, which need access to the camera frames. The trade-off is that photos skip OEM
processing (night mode, HDR). That is acceptable for party snapshots and avatars.

---

## Flow

```
Caller (CreatePlayerScreen / GameScreen)
    │  isCameraOpen = true      (only after the CAMERA permission is granted)
    ▼
InAppCameraDialog  (full-screen Dialog, its own window)
    │
    ├─ LiveCameraContent
    │     CameraViewfinder  ─ live preview, front lens by default
    │     overlay()         ─ empty slot for future filters
    │     CameraControls    ─ close · shutter · flip
    │           │
    │           └─ shutter ──► CameraController.capture() ──► temp JPEG in cacheDir
    │
    └─ PhotoReviewContent   (once a photo exists)
          AsyncImage of the capture
          PhotoReviewControls ─ Retake (back to live) · Use photo ──► onCaptured(uri)
```

It's a `Dialog`, not a navigation route. The calling screen stays composed underneath, so
`GameScreen` keeps its in-memory session and nothing goes through `savedStateHandle`.

| Caller | `onCaptured(uri)` | `onDismiss` |
|---|---|---|
| `CreatePlayerScreen` | `viewModel.onCapturedImage(uri)` | closes the dialog |
| `GameScreen` | `viewModel.onPhotoCaptured(uri)` | `viewModel.onCameraRequestDismissed()` — the turn advances |

The view models are unchanged. They still receive a `content://` FileProvider URI and copy it into
`filesDir` (`player_photos/` or `party_photos/{partyId}/`).

---

## `CameraController`

Plain class (not a ViewModel) created by `rememberCameraController()`. It owns the CameraX
`Preview` and `ImageCapture` use cases and the current lens.

| Member | Behaviour |
|---|---|
| `lensFacing` | Compose state; starts on `LENS_FACING_FRONT` |
| `bind(owner, surfaceProvider)` | Awaits `ProcessCameraProvider`, binds both use cases to the lifecycle owner |
| `flipLens()` | Switches lens and rebinds |
| `capture()` | Writes a JPEG via `createTempPictureFile()` and returns `uriForCacheFile(file)` |
| `unbind()` | `unbindAll()`; called when the viewfinder leaves composition |

Details worth knowing:

- **Missing lens.** If the device lacks the requested lens, `rebind()` falls back to the other one.
- **Mirroring.** Front-lens captures set `Metadata.isReversedHorizontal`, so the saved selfie matches
  the mirrored preview.
- **Latency over quality.** `CAPTURE_MODE_MINIMIZE_LATENCY`.
- **Releasing the camera.** `CameraViewfinder` binds in a `LaunchedEffect` and unbinds in a
  `DisposableEffect`. The camera is released while the review step is showing and when the dialog
  closes.
- **Failures.** A failed capture returns `null` from `captureOrNull`. The shutter re-enables, and no
  error is shown.
- **Viewfinder.** `PreviewView` in `COMPATIBLE` (TextureView) mode, hosted with `AndroidView`.
  `camera-compose` was not used because it needs a newer Compose BOM than the project's.
- **Temp files.** Captures land in the root of `cacheDir`, the only path the `FileProvider` exposes
  (see [builds.md](builds.md#manifest-permissions)). Photos discarded with Retake stay there until
  the system clears the cache.

---

## Colours

The camera chrome is dark in both themes, so the viewfinder reads like a camera. The tokens live in
`ui/theme/Color.kt`:

| Token | Use |
|---|---|
| `CameraBackdrop` | Dialog background, shutter icon |
| `CameraControlContent` | Icons, shutter ring, Retake label |
| `CameraControlScrim` | Circle behind the close and flip buttons |

---

## Filters (planned)

`InAppCameraDialog` takes `overlay: @Composable BoxScope.() -> Unit`, which is drawn above the live
preview. The intended path:

1. Add an `ImageAnalysis` use case to `CameraController`, running ML Kit Face Detection (contours).
2. Draw stickers such as bunny ears in `overlay` with a `Canvas` positioned from the face points.
3. Merge the overlay onto the captured bitmap before handing the URI to `onCaptured`.
4. Face warps such as "big eyes" need a GPU stage (CameraX `CameraEffect` / OpenGL shader), not
   an overlay.

---

## Key files

| File | Contents |
|---|---|
| `ui/camera/InAppCameraDialog.kt` | Dialog, live/review switch, `captureOrNull` |
| `ui/camera/CameraController.kt` | CameraX binding, lens flip, capture |
| `ui/camera/CameraViewfinder.kt` | `rememberCameraController`, `PreviewView` host, bind/unbind effects |
| `ui/camera/CameraControls.kt` | Close, shutter, flip |
| `ui/camera/PhotoReviewControls.kt` | Retake / Use photo |
| `utils/ContextExtensions.kt` | `createTempPictureFile`, `uriForCacheFile` |

Dependencies: `androidx.camera:camera-core`, `camera-camera2`, `camera-lifecycle` and
`camera-view`, all on the `camerax` version in `gradle/libs.versions.toml` (1.4.2).

## String resources

| Key | Value |
|---|---|
| `camera_shutter` | "Take photo" (content description) |
| `camera_flip` | "Switch camera" |
| `camera_close` | "Close camera" |
| `camera_retake` | "Retake" |
| `camera_use_photo` | "Use photo" |
| `camera_captured_photo` | Content description of the review image |
