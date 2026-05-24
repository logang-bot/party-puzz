# Builds

## Build types

| Build type | Ads | Debuggable | Purpose |
|---|---|---|---|
| `debug` | Test IDs | Yes | Local development |
| `staging` | Test IDs | No | QA — release-like, safe to test without risking invalid AdMob traffic |
| `release` | Real IDs | No | Production / Play Store |

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
adb install app/build/outputs/apk/staging/app-staging.apk
```

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
3. Select or create your keystore
4. Select the `release` build variant
5. The signed bundle lands in `app/release/`

> Keep your keystore (upload key) and passwords safe — if lost, you can request a reset via Play Console → **Setup → App signing → Request upload key reset**. See `docs/release.md` for details on how Play App Signing works.
