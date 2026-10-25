# Builds

## Build types

| Build type | Debuggable | Purpose |
|---|---|---|
| `debug` | Yes | Local development |
| `staging` | No | QA — release-like |
| `release` | No | Production / Play Store |

`main` has no ads. On the `ads/admob` and `ads/meta` branches each build type also sets
`USE_TEST_ADS` — test ads in `debug`/`staging`, real ones in `release`; see that branch's
`docs/ads-*.md`.

All three share one `applicationId` (`com.restrusher.partypuzl`) — to the device they are the same app, and only one can be installed at a time.

---

## One signing key for every build type

Every build type signs with the same key, through the `shared` signing config in `app/build.gradle.kts`. That is what makes switching envs an **update** rather than a reinstall: Android refuses an update whose signature differs from the installed one, and the uninstall that used to follow wiped the DB and every photo in `filesDir`. With one key, a debug build installs straight over a staging install and keeps its data, so a staging-only bug can be reproduced and debugged on the exact same parties and photos.

The key details live in `keystore.properties` at the repo root — gitignored, never commit it:

```properties
storeFile=/absolute/path/to/upload-key.jks
storePassword=…
keyAlias=…
keyPassword=…
```

Use the same keystore you sign staging and Play uploads with. Without the file (a fresh clone, CI) every build type falls back to the Android debug key, so builds still work — they just stop being interchangeable with a staging/release install.

> A copy installed from the Play Store is re-signed by Google (Play App Signing), so no local build can update it. Uninstall the Play copy before installing a local build on that device.

---

## No backup, by design

`android:allowBackup="false"` and `res/xml/data_extraction_rules.xml` turn off both Google cloud backup and device-to-device transfer in every env — `allowBackup="false"` alone still allows device transfer on Android 12+, hence the explicit excludes for every domain.

- **Uninstall wipes everything.** The DB, `filesDir/player_photos` and `filesDir/party_photos` go with the app, and nothing comes back on reinstall. Before this was turned off, Auto Backup restored an old DB on reinstall, which is why uninstalling never seemed to clear a broken state.
- **Updates keep everything.** Android never clears app data on an update. The only thing that can is Room's `fallbackToDestructiveMigration()`, which runs when a schema bump ships without a `Migration` — so every version bump needs one. See the version history in [create-player-feature-spec.md](create-player-feature-spec.md#database-version).
- Photos the user explicitly saves to the gallery (`Pictures/PartyPuzl`, via MediaStore) are shared storage and survive an uninstall — that is the user's copy, not app data.

---

## How to build

### From the command line

```bash
# Debug
./gradlew assembleDebug

# Staging
./gradlew assembleStaging

# Release
./gradlew assembleRelease
```

Output APKs are written to `app/build/outputs/apk/<buildType>/`.

### From Android Studio

**Debug** — just hit **Run** (▶).

**Staging / Release** — change the active build variant first:
1. **Build → Select Build Variant** (or open the Build Variants panel in the bottom-left)
2. Pick `staging` or `release`
3. **Run** to install on device, or **Build → Build APK(s)** for a bare APK

### Installing staging on a device

Staging is not debuggable, so Android Studio's default **app** run configuration is unavailable when it is the active build variant. Build and install manually instead:

```bash
./gradlew assembleStaging
adb install -r app/build/outputs/apk/staging/app-staging.apk
```

`-r` installs over whatever env is already there and keeps its data, which works as long as both were signed with the shared key. To debug something that only happens on a staging install, install a **debug** build over it from Android Studio — same app, same key, same data, and the debugger attaches.

### Build output locations

| Build type | Output |
|---|---|
| Debug / Staging APK | `app/build/outputs/apk/<buildType>/` |
| Signed release AAB | `app/release/` |
| Signed staging APK | `app/staging/` |

`app/release/` and `app/staging/` are gitignored — never commit build outputs.

---

### Signed release (Play Store upload)

The Play Store requires a signed artifact. Use Android Studio:

1. **Build → Generate Signed Bundle / APK**
2. Choose **Android App Bundle** (preferred) or **APK**
3. Select or create your keystore — the same one `keystore.properties` points at
4. Select the `release` build variant
5. The signed bundle lands in `app/release/`

> Keep your keystore (upload key) and passwords safe — if lost, you can request a reset via Play Console → **Setup → App signing → Request upload key reset**. See `docs/release.md` for details on how Play App Signing works.
