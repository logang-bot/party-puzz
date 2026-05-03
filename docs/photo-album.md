# Photo Album

Each party has a photo album — a collection of photos automatically captured during game sessions. Photos are viewable in `PartyDetailScreen` and are permanently deleted when the party is deleted.

---

## Overview

```
Game session (CHALLENGE_SHOWN)
    │
    │  pendingCameraRequest rolled at deal-start (~33% chance)
    │
    ├─ Dare dismissed  ──► camera request card appears (on top of challenge card)
    │
    └─ Mode event dismissed  ──► camera request card appears (on top of challenge card)
                │
                ├─ User taps "Take Photo"  ──► camera app opens
                │       │
                │       └─ Photo saved to  filesDir/party_photos/{partyId}/photo_{ts}.jpg
                │              └─ Record inserted into  party_photos  table
                │
                └─ User taps anywhere else  ──► dismissed, no photo saved
                        │
                        └─ Deal resets to IDLE
```

---

## Trigger Conditions

The camera request only fires when **all** of the following are true:

| Condition | Detail |
|---|---|
| Session is party-linked | `GamePlayersList.currentPartyId != null` |
| Random roll succeeds | ~33 % probability (`CAMERA_TRIGGER_PROBABILITY = 0.33`) |
| Camera permission is granted | Checked at the moment the user taps "Take Photo" |
| Trigger point reached | Dare challenge dismissed **or** Bar / Couples mode event dismissed |

> The random roll is decided at `CHALLENGE_SHOWN` time (start of the challenge phase), not at dismissal. This ensures the game knows upfront whether a camera card will follow, allowing a smooth transition without flashing the "tap to play" idle state in between.

---

## In-Game Camera Request Card

### Entry / exit animation

Same animation as the challenge card: scale from 85 % + fade in (350 ms / 300 ms). On exit: scale to 85 % + fade out (300 ms / 250 ms).

### Flip reveal

When the card enters, a `FlipCard` animates from its front (blank) to its back face (600 ms Y-rotation, `FastOutSlowInEasing`), revealing the camera request content.

### Background

Solid `MaterialTheme.colorScheme.surface` — opaque, unlike the glass-style challenge card. This makes the camera card visually distinct and signals a different kind of interaction.

### Content

```
┌─────────────────────────────────────┐
│                                     │
│   Freeze this moment forever        │  headlineMedium / Bold / onSurface
│                                     │
│   Some memories are worth keeping   │  bodyMedium / onSurface 65 %
│   — take a photo before it slips    │
│   away.                             │
│                                     │
│         [ 📷  Take a photo ]        │  filled Button (theme defaults)
│                                     │
│        Tap anywhere to skip         │  bodySmall / onSurface 45 %
│                                     │
└─────────────────────────────────────┘
```

Tapping **anywhere on the card** (not just the button) calls `onCameraRequestDismissed`, so the user can easily skip.

### Layering

While the camera card is visible, `dealPhase` remains `CHALLENGE_SHOWN`. This keeps the challenge card visible beneath the camera card, avoiding a jarring flash of the idle state:

```
[main card — tap to play / cycling / photo]   always rendered
[challenge card]                               visible while dealPhase == CHALLENGE_SHOWN
[camera request card]                          on top when showCameraRequest == true
```

---

## State Model

New fields in `GameScreenState`:

| Field | Type | Purpose |
|---|---|---|
| `pendingCameraRequest` | `Boolean` | Rolled at `CHALLENGE_SHOWN`; consumed when the trigger point is reached |
| `showCameraRequest` | `Boolean` | `true` while the camera request card is on screen |

Both fields are cleared by `resetDealState()`.

### Flow diagram

```
onGameDealTapped()
    └─ CHALLENGE_SHOWN update sets pendingCameraRequest = (random < 0.33 && partyId != null)

onModeEventDismissed()
    ├─ pendingCameraRequest == true  ──► clearEvent + showCameraRequest = true  (stay in CHALLENGE_SHOWN)
    └─ false  ──► resetDealState

onChallengeDismissed()  [dare branch only]
    ├─ pendingCameraRequest == true  ──► showCameraRequest = true  (stay in CHALLENGE_SHOWN)
    └─ false  ──► resetDealState

onCameraRequestDismissed()  ──► resetDealState
onPhotoCaptured(uri)        ──► save file + DB record, then resetDealState
```

---

## Photo Storage

| Aspect | Detail |
|---|---|
| Directory | `context.filesDir/party_photos/{partyId}/` |
| Filename | `photo_{timestamp}.jpg` |
| Source | Temporary URI created via `FileProvider` in `GameScreen`, written by the system camera app |
| Copy step | `InputStream` from `ContentResolver` piped to the destination file (on `Dispatchers.IO`) |

---

## Data Layer

### `PartyPhotoEntity`

```
table: party_photos
```

| Column | Type | Notes |
|---|---|---|
| `id` | `Int` | Auto-generated primary key |
| `partyId` | `Int` | Foreign key → `parties.id`; `onDelete = CASCADE` |
| `photoPath` | `String` | Absolute path to the file on device storage |
| `takenAt` | `Long` | `System.currentTimeMillis()` at insert time |

> `onDelete = CASCADE` means photo records are removed automatically by Room when the party row is deleted.

### `PartyPhotoDao`

| Method | Returns | Notes |
|---|---|---|
| `insert(photo)` | `Long` | New row ID |
| `getPhotosForParty(partyId)` | `Flow<List<PartyPhotoEntity>>` | Ordered by `takenAt DESC` |
| `delete(photo)` | `Unit` | Used for individual photo deletion |

### Repository / Proxy

Follows the same Proxy → Repository pattern as `Party` and `Player`:

```
PartyPhotoDao
    └─ PartyPhotoLocalProxy  (implements PartyPhotoProxy)
        └─ PartyPhotoRepositoryImpl  (implements PartyPhotoRepository)
            └─ injected where needed via Hilt
```

`PartyPhotoRepository` methods:

| Method | Notes |
|---|---|
| `addPhoto(partyId, photoPath)` | Creates and inserts a `PartyPhotoEntity` |
| `getPhotosForParty(partyId)` | Returns a `Flow` for live observation |
| `deletePhoto(photo)` | Deletes a single record |

### DI wiring

| Module | Addition |
|---|---|
| `DatabaseModule` | `providePartyPhotoDao` |
| `ProxyModule` | Binds `PartyPhotoLocalProxy → PartyPhotoProxy` (`@DatabaseProxy`) |
| `RepositoryModule` | `providePartyPhotoRepository` |

### Database version

Bumped from **5 → 6** with `fallbackToDestructiveMigration` in place. `PartyPhotoEntity` added to the `@Database` entities list. The database is currently at **v7** (see `create-player-feature-spec.md` for the full version history).

### `PartyWithPlayers` — eager photo loading

`PartyWithPlayers` includes a `@Relation` field for photos:

```kotlin
@Relation(parentColumn = "id", entityColumn = "partyId")
val photos: List<PartyPhotoEntity>
```

This means every call to `getAllPartiesWithPlayers()` or `getPartyById()` also loads the party's photo list in the same Room transaction. Party list cards (`PartyCard`, `LastPartyCard`) use `party.photos.size` directly for photo count display — no separate `PartyPhotoDao` query is needed for that purpose.

`PartyDetailViewModel` still observes `partyPhotoRepository.getPhotosForParty(partyId)` as a live `Flow` for the full detail screen, where real-time updates (a new photo captured mid-session) must be reflected immediately.

---

## PartyDetailScreen — Album Section

`PartyPhotoAlbumSection` is rendered between the players grid and the delete button.

### Permission gate

```
hasCameraPermission == false
    └─ Button: "Allow camera to capture party moments"
            └─ tapping launches RequestPermission contract for CAMERA

hasCameraPermission == true  &&  photos.isEmpty()
    └─ "No photos yet. Play a game session to capture moments!"

hasCameraPermission == true  &&  photos non-empty
    └─ FlowRow of 96 dp × 96 dp thumbnails (8 dp gap), loaded with Coil 3
           └─ each thumbnail is tappable → opens PhotoViewerDialog at that index
```

Permission state is a local `var hasCameraPermission` checked with `ContextCompat.checkSelfPermission` on first composition and updated by the `RequestPermission` launcher result.

### Photo observation

`PartyDetailViewModel` observes `partyPhotoRepository.getPhotosForParty(partyId)` as a `Flow`, updating `PartyDetailState.photos` in real time. Photos appear in the album without requiring a screen refresh.

---

## Photo Viewer

Tapping any thumbnail opens a full-screen `PhotoViewerDialog` that lets the user browse all album photos and save individual ones to the device gallery.

### Structure

```
PhotoViewerDialog  (Dialog — separate Android window, full screen)
    ├─ PhotoPager          HorizontalPager, swipe left/right to navigate
    ├─ PhotoViewerTopBar   overlay at the top
    │       ├─ Close button (left)
    │       └─ DropdownMenu "⋮" (right)
    │               └─ "Save"  ──►  onDownload(currentPhotoPath)
    └─ SnackbarHost        overlay at the bottom, shows save result
```

> `PhotoViewerDialog` creates its own Android window via `Dialog`. The `SnackbarHost` is placed inside that same window — this is required because a `SnackbarHost` on the screen behind the dialog would never be visible.

### Navigation

`HorizontalPager` (`rememberPagerState(initialPage = tappedIndex)`) handles swipe navigation. The initial page is the index of the thumbnail the user tapped.

### Save to gallery

Triggered from the three-dot menu → "Save". The save runs on `Dispatchers.IO` in the ViewModel:

| API level | Mechanism |
|---|---|
| API 29+ (Q+) | `MediaStore` scoped storage — no extra permission needed. Photos are saved to `Pictures/PartyPuzz/`. `IS_PENDING` flag is set during write and cleared on success. |
| API 24–28 | `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` — requires `WRITE_EXTERNAL_STORAGE` runtime permission (see *Permission Handling*). |

**Result feedback** — `downloadResult: DownloadResult?` in `PartyDetailState`:

| Value | Snackbar message |
|---|---|
| `DownloadResult.SUCCESS` | "Saved to your gallery" |
| `DownloadResult.FAILURE` | "Couldn't save the photo" |

A `LaunchedEffect(downloadResult)` inside `PhotoViewerDialog` observes the value, calls `snackbarHostState.showSnackbar(message)`, then calls `onDownloadResultConsumed()` to reset the state to `null`.

### State fields

New fields added to `PartyDetailState`:

| Field | Type | Purpose |
|---|---|---|
| `viewerPhotoIndex` | `Int?` | Non-null while the viewer is open; value is the initial pager page |
| `downloadResult` | `DownloadResult?` | Set by `downloadPhoto()`; consumed by the dialog snackbar |

### ViewModel methods

| Method | Action |
|---|---|
| `openPhotoViewer(index)` | Sets `viewerPhotoIndex = index` |
| `closePhotoViewer()` | Sets `viewerPhotoIndex = null` |
| `downloadPhoto(photoPath)` | Saves file via MediaStore; updates `downloadResult` |
| `clearDownloadResult()` | Resets `downloadResult` to `null` after snackbar is shown |

---

## Party Deletion — File Cleanup

When the user confirms party deletion in `PartyDetailViewModel.confirmDelete()`:

1. `isDeleting = true` — the loading spinner overlay is shown immediately.
2. Photo file paths are snapshotted from `_uiState.value.photos` **before** the coroutine launches (avoids races with the Flow emitting during deletion).
3. Inside `withContext(Dispatchers.IO)`:
   - Each photo file is deleted with `File(path).delete()`.
   - `partyRepository.deleteParty(partyId)` is called — Room's cascade delete removes all `party_photos` records automatically.
4. `isDeleting = false` + `navigateBack = true`.

> The `isDeleting` loading overlay was already in place in `PartyDetailScreen`; no UI changes were needed to cover the extended deletion time.

---

## Key Files

| File | Role |
|---|---|
| `PartyPhotoEntity.kt` | Room entity; `party_photos` table definition |
| `PartyPhotoDao.kt` | DAO: insert, query by party, delete |
| `PartyPhotoProxy.kt` / `PartyPhotoLocalProxy.kt` | Proxy interface and Room-backed implementation |
| `PartyPhotoRepository.kt` / `PartyPhotoRepositoryImpl.kt` | Repository interface and implementation |
| `GameScreenState.kt` | `pendingCameraRequest` and `showCameraRequest` fields |
| `GameScreenViewModel.kt` | Roll logic, trigger points, photo file copy + DB insert |
| `GameDealSection.kt` | `CameraRequestContent` composable; camera request `AnimatedVisibility` layer |
| `GameScreen.kt` | `FileProvider` URI setup, `TakePicture` launcher, permission check on button tap |
| `PartyDetailState.kt` | `photos`, `viewerPhotoIndex`, `downloadResult` fields; `DownloadResult` enum |
| `PartyDetailViewModel.kt` | Photo Flow observation; viewer state; download logic; file + record cleanup on delete |
| `PartyDetailScreen.kt` | Permission launchers, `PartyPhotoAlbumSection` and `PhotoViewerDialog` usage |
| `PartyDetailComponents.kt` | `PartyPhotoAlbumSection` composable |
| `PhotoViewerDialog.kt` | `PhotoViewerDialog`, `PhotoPager`, `PhotoViewerTopBar` composables |

---

## String Resources

| Key | Usage |
|---|---|
| `camera_request_title` | Camera card heading ("Freeze this moment forever") |
| `camera_request_subtitle` | Camera card body copy |
| `camera_request_button` | "Take a photo" button label |
| `party_album_title` | Section heading in `PartyDetailScreen` |
| `party_album_empty` | Empty-state copy when no photos exist |
| `grant_camera_permission` | Button label when camera permission is not yet granted |
| `close` | Content description for the viewer close button |
| `photo_options` | Content description for the three-dot menu button |
| `download_photo` | "Save" — menu item label |
| `download_success` | "Saved to your gallery" — snackbar on success |
| `download_failed` | "Couldn't save the photo" — snackbar on failure |
