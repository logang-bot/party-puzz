# Create Player Feature Spec

## Overview

The Create Player screen lets a user configure a new player (name + photo) and persist them to the
Room database. On confirmation, the player is linked to a shared `Party` entity that groups all
players added during the current game session.

---

## User Flow

1. User navigates from `GameConfigScreen` → `CreatePlayerScreen`.
2. User optionally takes a camera photo or picks a random drawable avatar.
3. User optionally enters or generates a random name.
4. User taps **Confirm**.
5. A loading overlay appears while the async DB writes complete.
6. Screen navigates back to `GameConfigScreen`.
7. User can repeat steps 1–6 to add more players; they all land in the same party.

---

## Data Model

### `PlayerEntity` (`players` table)

| Column | Type | Description |
|---|---|---|
| `id` | `Int` (PK, autoGenerate) | Unique player ID |
| `nickName` | `String` | Display name |
| `gender` | `Gender` | Enum: `Male`, `Female`, `Unknown`. Optional — defaults to `Unknown` when the user leaves the selector blank. |
| `interestedIn` | `InterestedIn` | Enum: `Man`, `Woman`, `Both`. In Couples mode the user must pick a value (Confirm is disabled until chosen). In all other modes the selector is hidden and `Both` is saved automatically. |
| `photoPath` | `String?` | Absolute path to camera photo copied into `filesDir/player_photos/` |
| `avatarName` | `String?` | Drawable resource entry name, e.g. `img_dummy_avatar_3` |

### `PartyEntity` (`parties` table)

| Column | Type | Description |
|---|---|---|
| `id` | `Int` (PK, autoGenerate) | Unique party ID |
| `name` | `String` | Auto-generated name, e.g. `"Party 1741478400000"` |
| `dateCreation` | `Long` | Unix timestamp (ms) at creation time |

### `PartyPlayerCrossRef` (`party_player_cross_ref` table)

| Column | Type | Description |
|---|---|---|
| `partyId` | `Int` (composite PK) | References `PartyEntity.id` |
| `playerId` | `Int` (composite PK) | References `PlayerEntity.id` |

---

## Image Storage Strategy

| Source | Storage location | DB field populated |
|---|---|---|
| Camera photo | Copied to `filesDir/player_photos/<timestamp>.png` | `photoPath` |
| Random drawable avatar | Stays in APK; only name is stored | `avatarName` |
| Neither | — | both `null` |

**Reloading images:**
- Camera photo: `Uri.fromFile(File(photoPath))`
- Drawable avatar: `resources.getIdentifier(avatarName, "drawable", packageName)`

---

## Party Session Management

`GamePlayersList.currentPartyId` (singleton `Int?`) tracks the active party across multiple
`CreatePlayerScreen` visits within a single game session:

- **First player saved** → a new `PartyEntity` is created; its ID is stored in `currentPartyId`.
- **Subsequent players** → `currentPartyId` is reused; no new party is created.
- **New game session** → `GamePlayersList.resetPlayersList()` clears both `PlayersList` and
  `currentPartyId`.

---

## Architecture

### Layer breakdown

```
CreatePlayerScreen
    └── CreatePlayerViewModel  (@HiltViewModel)
            ├── PlayerRepository  → PlayerRepositoryImpl
            │       └── PlayerProxy (@DatabaseProxy) → PlayerLocalProxy → PlayerDao
            └── PartyRepository   → PartyRepositoryImpl
                    └── PartyProxy  (@DatabaseProxy) → PartyLocalProxy  → PartyDao
```

### Key classes

| Class | Location | Role |
|---|---|---|
| `CreatePlayerViewModel` | `ui/views/createPlayer/` | Orchestrates save logic, exposes `navigationEvents` channel |
| `PlayerRepositoryImpl` | `data/repositories/` | Wraps `PlayerProxy`, returns `Long` player ID |
| `PartyRepositoryImpl` | `data/repositories/` | Creates parties and cross-ref links |
| `PlayerLocalProxy` | `data/local/proxies/` | Room-backed `PlayerProxy` |
| `PartyLocalProxy` | `data/local/proxies/` | Room-backed `PartyProxy` |
| `PartyPuzzDatabase` | `data/local/` | Room DB v2; entities: `Player`, `Party`, `PartyPlayerCrossRef` |
| `GamePlayersList` | `data/local/appData/appDataSource/` | Singleton holding in-memory player list + `currentPartyId` |

### DI wiring (`di/`)

- `DatabaseModule` — provides `PlayerDao` and `PartyDao` from `PartyPuzzDatabase`.
- `ProxyModule` — binds `PlayerLocalProxy → @DatabaseProxy PlayerProxy` and
  `PartyLocalProxy → @DatabaseProxy PartyProxy`.
- `RepositoryModule` — provides `PlayerRepository` and `PartyRepository` singletons.

---

## ViewModel: `confirmPlayer()`

Runs on `Dispatchers.IO` inside `viewModelScope`:

1. Set `isLoading = true`.
2. If `capturedImageUri != Uri.EMPTY` → copy from cache to `filesDir/player_photos/<ts>.png`; store absolute path as `photoPath`.
3. If `randomAvatarRes != null` → resolve entry name via `resources.getResourceEntryName(res)`; store as `avatarName`.
4. Insert `PlayerEntity` via `playerRepository.createPlayer()` → returns `Long` player ID.
5. Read `GamePlayersList.currentPartyId`; if `null` → create a new party via `partyRepository.createParty()` and store the result.
6. Link player to party via `partyRepository.linkPlayerToParty(partyId, playerId)`.
7. Set `isLoading = false`.
8. Send `Unit` on `_navigationEvents` channel → screen calls `navigateBack()`.

---

## UI Changes (`CreatePlayerScreen`)

- Uses `hiltViewModel()` instead of `viewModel()`.
- Accepts a `navigateBack: () -> Unit` parameter wired in `HomeNavigation` as
  `{ navController.popBackStack() }`.
- `LaunchedEffect(Unit)` collects `viewModel.navigationEvents` and calls `navigateBack()`.
- Confirm button `onClick` calls `viewModel.confirmPlayer()`.
- Screen wrapped in a `Box`; when `uiState.isLoading` is `true`, a semi-transparent black overlay
  with a `CircularProgressIndicator` is shown on top.
- `PlayerFormContent`'s inner `Column` uses `verticalScroll` to handle overflow on small screens.

### Selectors in `PlayerFormContent`

| Composable | File | Behaviour |
|---|---|---|
| `GenderOptionsContainer` | `GenderOptionsContainer.kt` | Two-button row (Male / Female) using `ic_male` / `ic_female`. **Couples mode only** — hidden otherwise; `Gender.Unknown` is saved automatically. |
| `InterestedInOptionsContainer` | `GenderOptionsContainer.kt` | Three-button row (Man / Woman / Both) using `ic_man` / `ic_woman` / `ic_wc`. **Couples mode only** — hidden otherwise; `InterestedIn.Both` is saved automatically. |

Both selectors accept a nullable selected value (`Gender?` and `InterestedIn?`) so the UI can represent the "not yet chosen" state without a sentinel value. Visibility is controlled by `AnimatedVisibility(visible = isCouplesMode)`.

### Game-mode-aware field visibility

`isCouplesMode` is threaded from the navigation route through to the ViewModel and UI:

1. **Route** — `CreatePlayerScreen(isCouplesMode: Boolean = false)` carries the flag. `HomeNavigation` sets it to `true` only when `gameModeName == R.string.couples_game_mode`.
2. **ViewModel** — reads `isCouplesMode` from `SavedStateHandle` in `init`. When `false`, `gender` is initialised to `Gender.Unknown` and `interestedIn` to `InterestedIn.Both` so the Confirm button is immediately enabled once the player has a name.
3. **UI** — `PlayerFormContent` wraps `GenderOptionsContainer` and `InterestedInOptionsContainer` in `AnimatedVisibility(visible = isCouplesMode)`. In non-couples modes the selectors are never shown and the pre-set defaults are saved transparently.

---

## Database Version

| Version | Change |
|---|---|
| 1 | Initial schema (`players` table) |
| 2 | Added `parties` and `party_player_cross_ref` tables; added `photoPath`/`avatarName` columns to `players`. `fallbackToDestructiveMigration` handles the upgrade. |
| 3 | Added bar mode support (no schema change to `players`). |
| 4 | Added `interestedIn: InterestedIn` column to `players`. |
| 5 | Both `gender` and `interestedIn` columns present on `players`; `gender` is optional (`Unknown` default), `interestedIn` is required. |
