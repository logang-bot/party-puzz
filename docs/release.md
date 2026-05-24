# Releasing to the Play Store

## First-time setup (do once)

### 1. Create a signing keystore (upload key)

In Android Studio: **Build → Generate Signed Bundle/APK → Android App Bundle → Create new keystore**

- Save the keystore file **outside the project directory** and never commit it to git
- Store the keystore password, key alias, and key password somewhere safe (password manager + cloud drive)

> **How signing works with Play App Signing (mandatory for new apps):**
> You hold an **upload key** — used only to sign the AAB you upload. Google holds the actual **app signing key** used to distribute the app to devices. If you lose your upload key, you can request a reset via Play Console → **Setup → App signing → Request upload key reset**. You can always recover.

### 2. Create the app in Play Console

Go to [play.google.com/console](https://play.google.com/console) → **Create app** and fill in the basic details.

### 3. Complete the store listing

Play Console shows a task list. Required before publishing:

- **Main store listing** — title, short/full description, screenshots (phone required), feature graphic (1024×500 px), icon (512×512 px)
- **Content rating** — complete the questionnaire
- **Target audience** — confirm not targeting children
- **App access** — describe any login-gated features
- **Ads declaration** — declare the app contains ads
- **Data safety** — declare what data is collected (check AdMob's data disclosure requirements)

### 4. Create the Remove Ads in-app product

Play Console → your app → **Monetize → Products → In-app products → Create product**

- Product ID: `remove_ads` (must match exactly)
- Type: One-time
- Set price and activate it

See `docs/ads.md` for more context.

---

## Every release

### 1. Bump the version in `app/build.gradle.kts`

```kotlin
versionCode = 2        // must be higher than the previous upload — Play Store rejects duplicates
versionName = "1.1"    // what users see in the store
```

### 2. Generate the signed AAB

**Build → Generate Signed Bundle/APK → Android App Bundle → Next**

- Select your keystore and enter credentials
- Select the `release` build variant
- The signed `.aab` lands in `app/release/`

Or via command line (requires signing config in `build.gradle.kts`):
```bash
./gradlew bundleRelease
```

### 3. Upload to Play Console

Play Console → your app → **Testing → Internal testing → Create new release → Upload** the `.aab`

Always start with internal testing to verify the build works correctly on a real Play Store install before promoting.

### 4. Test the release build

Install from the internal testing track on a real device and verify:

- [ ] App opens correctly
- [ ] Ads display (banner + app open)
- [ ] "Remove Ads" purchase flow opens correctly
- [ ] App open ad does not show after purchasing Remove Ads
- [ ] Banners do not show after purchasing Remove Ads

### 5. Promote to production

Play Console → **Production → Create new release → select the AAB already uploaded → Start rollout**

Consider a staged rollout (e.g. 20%) for larger updates to catch regressions before full distribution.

---

## First release checklist

On the very first release there are extra steps before promoting to production:

- [ ] Add a privacy policy URL to Play Console → **App content → Privacy policy**
- [ ] Publish to internal testing first and verify the build works
- [ ] **Closed testing** — create a closed testing track with at least **20 testers** who must opt in and stay enrolled for **14 consecutive days** before the production track unlocks (required for personal Google accounts)
- [ ] Wait for AdMob to approve the app (they crawl the Play Store listing — can take a few days)
- [ ] Verify the `remove_ads` in-app product is created and active in Play Console
- [ ] Test the Remove Ads purchase using a Play Store license tester account
- [ ] Once AdMob approves and the 14-day closed testing period is complete → promote to production
