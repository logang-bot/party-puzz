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
- **Ads declaration** — `main` contains **no ads**; a build from `ads/admob` or `ads/meta` does — declare accordingly
- **Data safety** — declare what data is collected (an ad branch adds its network's disclosures — see its `docs/ads-*.md`)

### 4. Create the Purchase App in-app product

Play Console → your app → **Monetize → Products → In-app products → Create product**

- Product ID: `remove_ads` (must match exactly)
- Type: One-time
- Set price and activate it

See `docs/ads.md` for more context.

### 5. Ad network setup (ad branches only)

`main` has no ads, so there is nothing to do here when releasing from it. When releasing from
`ads/admob` or `ads/meta`, follow the keys checklist in that branch's `docs/ads-admob.md` /
`docs/ads-meta.md` — including the still-outstanding real rewarded unit / placement id. See
[ads.md](ads.md).

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

Or via command line — `app/build.gradle.kts` signs with the keystore in `keystore.properties` (see [builds.md](builds.md#one-signing-key-for-every-build-type)); without that file the bundle comes out debug-signed and Play rejects it:
```bash
./gradlew bundleRelease
```

### 3. Upload to Play Console

Play Console → your app → **Testing → Internal testing → Create new release → Upload** the `.aab`

Always start with internal testing to verify the build works correctly on a real Play Store install before promoting.

### 4. Test the release build

Install from the internal testing track on a real device and verify:

- [ ] App opens correctly
- [ ] From `main`: no ads anywhere, and no empty gap at the bottom of Home, Parties, Party detail or Settings
- [ ] From an ad branch: run the ad checks in its `docs/ads-*.md`
- [ ] "Purchase App" purchase flow opens correctly
- [ ] Premium packs appear locked on the setup screen; tapping one opens the unlock sheet
- [ ] **Both unlock options raise a "Coming soon!" toast and nothing else** — that is `FeatureFlags.PACK_UNLOCK_COMING_SOON` doing its job. If either one actually unlocks a pack, the flag has been turned off before its two prerequisites landed; see [ads.md](ads.md)
- [ ] Deal several quiz questions: the correct answer lands on the left and right roughly evenly, including on a repeat of a question already seen (the deal-time option swap)
- [ ] Each premium pack plays enabled on its own — Movie Night offers quiz only, Spicy offers sticky dares only, NSFW deals both Truth and Dare and neither card comes back empty
- [ ] `./gradlew connectedDebugAndroidTest` passes — `QuestionPackIntegrityTest` is the only guard on the catalog-to-array indices

*Once `PACK_UNLOCK_COMING_SOON` is removed, these two become live again and must be checked:*

- [ ] "Watch a short ad" grants the pack for the session
- [ ] Purchasing the app unlocks all three premium packs permanently
- [ ] **Upgrading over an existing install keeps saved parties and photos** — the v10 migration adds the custom-pack tables, the v11 one adds `custom_packs.isAvailable`; a wrong statement throws on open. See [custom-packs.md](custom-packs.md)
- [ ] **Upgrading keeps the game mode on played parties** — play a party on the previous release, then upgrade. Its card on Parties still shows the mode; `MIGRATION_12_13` only clears a mode whose stored resource id no longer matches this build. A crash on opening Parties means a stale `R.string` id reached `stringResource()` again
- [ ] **Upgrading keeps the quiz packs** — upgrade over a v13 install. The app opens, and the quiz packs (World Geography, Space & Science, Mixed Bag, Movie Night) still show on the setup screen. A crash on launch means `MIGRATION_13_14` did not rewrite `GENERAL_KNOWLEDGE` to `QUIZ`
- [ ] **Uninstall then reinstall starts empty** — no parties, no photos. Anything coming back means a backup rule was re-enabled; see [builds.md](builds.md#no-backup-by-design)
- [ ] **Upgrading keeps authored packs** — install the previous release, write a pack, then upgrade. The pack is still there and still available. Losing it means `MIGRATION_10_11` failed validation and the destructive fallback ran
- [ ] Settings → Your packs: write a pack, add one entry of each type, confirm it plays in-game
- [ ] New entry screen offers **three** deal cards; Truth or Dare asks which half in step 02, and every path runs 01–04
- [ ] Switching a pack off in Your packs removes it from the setup screen's Custom group entirely, and its entries never come up in a game; switching it back on restores it, already selected
- [ ] Toggling a custom pack's row *on the setup screen* does not flip its switch in Your packs — they are separate flags
- [ ] Force-stop and relaunch: the custom pack and its entries survive (regression test for the seeder's `tier != 'CUSTOM'` guard)

### 5. Promote to production

Play Console → **Production → Create new release → select the AAB already uploaded → Start rollout**

Consider a staged rollout (e.g. 20%) for larger updates to catch regressions before full distribution.

---

## First release checklist

On the very first release there are extra steps before promoting to production:

- [ ] Add a privacy policy URL to Play Console → **App content → Privacy policy**
- [ ] Publish to internal testing first and verify the build works
- [ ] **Closed testing** — create a closed testing track with at least **20 testers** who must opt in and stay enrolled for **14 consecutive days** before the production track unlocks (required for personal Google accounts)
- [ ] Releasing from an ad branch only: wait for the network to approve the app (see its `docs/ads-*.md`)
- [ ] Verify the `remove_ads` in-app product is created and active in Play Console
- [ ] Releasing from an ad branch only: give `PACK_UNLOCK_REWARDED` a real id (setup step 5) — until then the premium unlock earns nothing
- [ ] **Only after both of the above:** remove `FeatureFlags.PACK_UNLOCK_COMING_SOON` to re-enable the unlock sheet. It is shipping switched off *on purpose* — leaving it on is safe, turning it off early sells a door that leads nowhere. Undo steps in [ads.md](ads.md)
- [ ] Test the Purchase App purchase using a Play Store license tester account
- [ ] Once any ad network approves and the 14-day closed testing period is complete → promote to production
