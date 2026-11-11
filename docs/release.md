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

**Trader status (EU Digital Services Act).** Selling the premium in-app product makes you a
*trader* under the DSA. Declare it in Play Console's developer account settings (Play
prompts for the trader declaration there). Play then shows your name, address, email and phone on the store listing
to EU users. That is the legal "who made this app" disclosure; the website in `logang-bot.github.io`
is not a substitute for it.

### 3. Complete the store listing

> **Alcohol policy.** The Nov 2026 submission was rejected under *Inappropriate Content → Alcohol* for the full description ("Bar Time… pre-game", "whoever… drinks") and a screenshot showing a shot of tequila. The listing must follow the same [responsible-drinking rules](bar-mode.md#responsible-drinking-rules-play-policy) as the app:
> - No shots, chugging, "nights out" or "pre-game" pitch.
> - Losers "take a sip" or "lose the round".
> - Say "any drink works, including non-alcoholic".
> - End with an "18+ only. Play responsibly — know your limits, never drink and drive." line.
> - Screenshots must not show heavy-drinking copy.
>
> Also keep the description in step with the build. A `main` release has no ads, so it must not say "remove ads".

Play Console shows a task list. Required before publishing:

- **Main store listing** — title, short/full description, screenshots (phone required; generate them with [store-screenshots.md](store-screenshots.md)), feature graphic (1024×500 px), icon (512×512 px)
- **Content rating**: complete the IARC questionnaire honestly. PartyPuzl is a drinking game, so answer **Yes** to alcohol references/consumption, **Yes → "Suggestive references and innuendo in text"** only for sexual themes (kisses, flirting, crushes; nothing refers to sex itself or is shown visually), **No** to crude humour (that category means bodily-function humour: belching, flatulence, feces, urine, vomit, and the app has none), and **No** to user interaction or sharing, since the app is offline with no accounts. Expect around PEGI 16/18 and ESRB Mature. Understating it gets the release rejected under *Content Ratings* ("the content rating may be inaccurate"). Fix that with *Start new questionnaire*, not by toning down the answers
- **Target audience**: **18+ only**. This matches the in-app `AgeConfirmationDialog` (see [navigation.md](navigation.md#responsible-drinking-dialogs))
- **App access** — describe any login-gated features
- **Ads declaration** — `main` contains **no ads**; a build from `ads/admob` or `ads/meta` does — declare accordingly
- **Advertising ID** (App content → Advertising ID) — **No** for a release from `main`. `main` has no ad or analytics SDK, so its merged manifest has no `com.google.android.gms.permission.AD_ID`; a "Yes" here makes Play reject the release with *"Your advertising ID declaration says that your app uses advertising ID…"*. Do **not** add the permission to silence it. An ad branch (`ads/admob`, `ads/meta`) uses the advertising ID — switch this to **Yes** before shipping one
- **Data safety** — declare what data is collected (an ad branch adds its network's disclosures — see its `docs/ads-*.md`). `main`'s only network call of its own downloads the public premium pack bank from GitHub Pages and sends no user data, so it adds no data type. See [builds.md](builds.md#manifest-permissions). The answers for `main`:
  - *Does your app collect or share any of the required user data types?* **Yes**, for the purchase only
  - **Financial info → Purchase history**: collected, **not** shared, **not** processed ephemerally, **required** (only when the user buys), purpose **App functionality**. Play Billing handles the order and the app reads the purchase back to unlock premium. Google's wording on whether Billing alone counts is ambiguous; declaring it is the safe side, and under-declaring is what Play flags
  - *Is all data encrypted in transit?* **Yes** (Play Billing and the HTTPS pack download)
  - *Can users request deletion?* Nothing is held by us; purchase records live with Google. Answer that no account or server data exists
  - Nothing else: photos, player names and parties never leave the device, and "collected" in Play's terms means sent off it

### 4. Set up the premium in-app product

The product `remove_ads` already exists. Its id can never change, so the app keeps using it (see
[ads.md](ads.md#premium--the-one-time-purchase)). What it needs:

Step-by-step test pass: [testing-purchases.md](testing-purchases.md).

1. **Payments profile**: Setup → Payments profile must be complete (merchant account), or nothing can be priced.
2. **A build with billing on a track**: Play only allows products once it has seen a build carrying the `BILLING` permission. An internal-testing upload is enough.
3. **The product**: Monetize with Play → Products → **One-time products** → `remove_ads`.
   - Name / description: describe premium packs and unlimited custom packs. **Do not mention removing ads** while shipping from `main`, which has none.
   - Confirm the price and the purchase option, and that the product is **Active**.
4. **License testers**: Settings → License testing → add your Google account(s), response `RESPOND_NORMALLY`. Their purchases are never charged.
5. **Install from Play**: product details (and so the purchase sheet and price) only load for a build installed from a Play track, signed with the release key, with an `applicationId` and `versionCode` Play has seen. A sideloaded debug build reports "Purchases are unavailable right now".
6. **Store listing**: "Contains in-app purchases" is shown automatically. Mention the premium packs in the full description.
7. **Data safety**: declare **Financial info → Purchase history** as in [step 3](#3-complete-the-store-listing). Purchases go through Google Play and the app itself sends nothing off the device.

### 5. Ad network setup (ad branches only)

`main` has no ads, so there is nothing to do here when releasing from it. When releasing from
`ads/admob` or `ads/meta`, follow the keys checklist in that branch's `docs/ads-admob.md` /
`docs/ads-meta.md` — including the still-outstanding real rewarded unit / placement id. See
[ads.md](ads.md).

Any ad branch (`ads/levelplay` included) also needs, before it ships:

- **A consent prompt for EEA/UK users (GDPR).** Personalised ads need consent before the SDK
  initialises. Use Google's UMP SDK or a certified CMP and pass the result to LevelPlay's consent
  API. Google's ad demand only serves EEA/UK traffic through a Google-certified CMP.
- **US state privacy (CCPA and similar).** Pass the "do not sell/share" signal to the network.
- **The privacy policy**, **Advertising ID → Yes**, **Ads → Yes** and the **Data safety** form
  updated with the network's data types (device or other IDs, approximate location, app
  interactions, diagnostics, shared with the ad network for advertising).
- **`app-ads.txt`** carries the network's lines at the root of the developer website
  (`logang-bot.github.io/app-ads.txt`), and the store listing's website points there.

---

## Every release

### 1. Bump the version in `app/build.gradle.kts`

```kotlin
versionCode = 12       // must be higher than every earlier upload, on any track
versionName = "1.1"    // what users see in the store
```

The first public release is **`versionName = "1.0"`, `versionCode = 11`**. The closed-testing
builds used codes up to 10 and the name "2.1". The name was reset for launch, and only the code has
to keep going up. Never reuse or lower a `versionCode`.

If this release bumps the database version, commit the new `app/schemas/…/<version>.json` with
its migration — see [builds.md](builds.md#room-schema-export).

### 2. Generate the signed AAB

**Build → Generate Signed Bundle/APK → Android App Bundle → Next**

- Select the **upload key** (`partyPuzlUploadKey.jks`, created with this wizard and kept outside
  the repo) and enter its passwords. Studio signs with the key you pick here and ignores
  `keystore.properties`, which only matters for command-line builds. Play only accepts this key,
  because it is registered under **App integrity → App signing**. Keep a backup of the file and its
  passwords in a password manager
- Select the `release` build variant
- The signed `.aab` lands in `app/release/`

Or via command line — `app/build.gradle.kts` signs with the keystore in `keystore.properties` (see [builds.md](builds.md#one-signing-key-for-every-build-type)); without that file the bundle comes out debug-signed and Play rejects it:
```bash
./gradlew bundleRelease
```

### 3. Upload to Play Console

> **Switching between `main` and an ad branch?** Update **App content → Advertising ID** (No for
> `main`, Yes for an ad branch) and the **Data safety** form *before* submitting, and make sure the
> privacy policy matches the build. Play checks the declaration against every *active* artifact on
> every track, so an older build still live on a testing track can trip the AD_ID check too — replace
> it with the new build on that track, or pause the track.

Play Console → your app → **Testing → Internal testing → Create new release → Upload** the `.aab`

Always start with internal testing to verify the build works correctly on a real Play Store install before promoting.

### 4. Test the release build

Install from the internal testing track on a real device and verify:

- [ ] App opens correctly
- [ ] From `main`: no ads anywhere, and no empty gap at the bottom of Home, Parties, Party detail or Settings
- [ ] From an ad branch: run the ad checks in its `docs/ads-*.md`
- [ ] Premium packs appear locked on the setup screen; tapping one opens the unlock sheet with the purchase option and its price (and, from `main`, no "Watch a short ad" row)
- [ ] With a license tester: buying from the unlock sheet unlocks all three premium packs; buying from Settings → Purchase App does the same and the row turns into "App Purchased"
- [ ] Refund the test order (Order management): on next launch the premium packs relock and switch off, and custom packs are capped again
- [ ] Free user: Your packs allows 1 pack; a second opens the premium sheet. A pack allows 5 entries; the 6th opens it too
- [ ] Pack bank: on a fresh install with network, the premium rows show prompt counts (24 / 20 / 40) within a few seconds. Then reinstall, open the app in **airplane mode**: the rows read "Downloads when you're online", free and custom packs still play, and nothing crashes. As a premium user with no content yet, opening the setup screen offline shows the loading overlay, then the "Check your internet connection" snackbar. Turn the network on, tap an unlocked premium row: the overlay shows and the pack fills in
- [ ] Deal several quiz questions: the correct answer lands on the left and right roughly evenly, including on a repeat of a question already seen (the deal-time option swap)
- [ ] Each premium pack plays enabled on its own — Movie Night offers quiz only, Spicy offers sticky dares only, NSFW deals both Truth and Dare and neither card comes back empty
- [ ] `./gradlew connectedDebugAndroidTest` passes — `QuestionPackIntegrityTest` is the only guard on the catalog-to-array indices

- [ ] From an ad branch only: "Watch a short ad" grants the pack for the session
- [ ] **Upgrading over an existing install keeps saved parties and photos** — the v10 migration adds the custom-pack tables, the v11 one adds `custom_packs.isAvailable`; a wrong statement throws on open. See [custom-packs.md](custom-packs.md)
- [ ] **Upgrading keeps the game mode on played parties** — play a party on the previous release, then upgrade. Its card on Parties still shows the mode; `MIGRATION_12_13` only clears a mode whose stored resource id no longer matches this build. A crash on opening Parties means a stale `R.string` id reached `stringResource()` again
- [ ] **Upgrading keeps the quiz packs** — upgrade over a v13 install. The app opens, and the quiz packs (World Geography, Space & Science, Mixed Bag, Movie Night) still show on the setup screen. A crash on launch means `MIGRATION_13_14` did not rewrite `GENERAL_KNOWLEDGE` to `QUIZ`
- [ ] **Uninstall then reinstall starts empty** — no parties, no photos. Anything coming back means a backup rule was re-enabled; see [builds.md](builds.md#no-backup-by-design)
- [ ] **Upgrading keeps authored packs** — install the previous release, write a pack, then upgrade. The pack is still there and still available. Losing it means `MIGRATION_10_11` failed validation and the destructive fallback ran
- [ ] Settings → Your packs: write a pack, add one entry of each type, confirm it plays in-game
- [ ] New entry screen offers **three** deal cards; Truth or Dare asks which half in step 02, and every path runs 01–04
- [ ] Switching a pack off in Your packs removes it from the setup screen's Custom group entirely, and its entries never come up in a game; switching it back on restores it, already selected
- [ ] Toggling a custom pack's row *on the setup screen* does not flip its switch in Your packs — they are separate flags
- [ ] **A process kill mid-game reopens on Home** — start a game, press Home, run `adb shell am kill com.restrusher.partypuzl`, reopen from Recents. The app lands on Home (not an empty game), and the party is on Parties. Rotating during a game still keeps it. See [navigation.md](navigation.md#process-restart-opens-on-home)
- [ ] Force-stop and relaunch: the custom pack and its entries survive (regression test for the seeder's `tier != 'CUSTOM'` guard)
- [ ] Settings → Open-source licenses lists the libraries, and tapping one shows its licence text. An empty list means the generated `aboutlibraries.json` went missing; see [open-source-licenses.md](open-source-licenses.md)

### 5. Promote to production

Play Console → **Production → Create new release → select the AAB already uploaded → Start rollout**

Consider a staged rollout (e.g. 20%) for larger updates to catch regressions before full distribution.

---

## First release checklist

On the very first release there are extra steps before promoting to production:

- [ ] Add the privacy policy URL to Play Console → **App content → Privacy policy**:
  `https://logang-bot.github.io/partypuzl-legal/privacy-policy.html` (repo `partypuzl-legal`,
  with an **L**. A `partypuzz` typo in `SettingsScreen` made it 404 before 1.0). Its text must match
  the branch you ship. A release from `main` must not describe AdMob ads
- [ ] Publish to internal testing first and verify the build works
- [ ] **Closed testing** — create a closed testing track with at least **20 testers** who must opt in and stay enrolled for **14 consecutive days** before the production track unlocks (required for personal Google accounts)
- [ ] Releasing from an ad branch only: wait for the network to approve the app (see its `docs/ads-*.md`)
- [ ] Verify the `remove_ads` in-app product is active in Play Console, with premium-pack wording (setup step 4)
- [ ] Releasing from an ad branch only: give `PACK_UNLOCK_REWARDED` a real id (setup step 5) — until then the "Watch a short ad" option earns nothing
- [ ] Test the Purchase App purchase using a Play Store license tester account
- [ ] Once any ad network approves and the 14-day closed testing period is complete → promote to production

---

## 1.0 release notes (first public release)

Decided before shipping 1.0 from `main`:

| Topic | Decision |
|---|---|
| Version | `1.0` / code `11` (see [step 1](#1-bump-the-version-in-appbuildgradlekts)) |
| Ads | None. `main` ships `NoAdsProvider` until there's a working AdMob account. See [ads.md](ads.md) |
| Purchase App (Settings) | Visible, but raised "Coming soon!" in 1.0. Enabled afterwards: see [ads.md](ads.md#premium--the-one-time-purchase) |
| R8 | **Off** for 1.0. Closed testing ran without it, so turning it on would ship an untested build. Planned for a later release. See [builds.md](builds.md#r8-code-shrinking) |
| Process kill mid-game | Reopens on Home; the game is not restored. Players and parties are already in Room. See [navigation.md](navigation.md#process-restart-opens-on-home) |
| Permissions | `coil-network-okhttp` removed; `INTERNET` remains only because Play Billing brings it. `FileProvider` narrowed to `cache-path`. See [builds.md](builds.md#manifest-permissions) |
| Room schema | Export turned on; `app/schemas/` is committed from v14. See [builds.md](builds.md#room-schema-export) |
| Privacy policy | Link fixed, and the text rewritten for the no-ads build (`partypuzl-legal` `e66de25`, then `3b21530`, "Last updated: November 10, 2026"): no ads, all data local, uninstall deletes it with no backup, camera use, Google Play Billing for the purchase, and GitHub Pages for the premium pack download. When switching to an ad branch, the policy must gain that network's disclosures first |
| Open-source licences | Settings → Open-source licenses shows every bundled library and its licence text, generated at build time. See [open-source-licenses.md](open-source-licenses.md) |

Planned for a later release:
- **Ads frequency (user feedback).** On `ads/admob` the App Open ad showed on every return to the app,
  including after the camera. Fix: show it once per cold start. Root cause and steps are in
  [ads.md](ads.md#known-issue-app-open-ad-shows-on-every-return-to-the-app-adsadmob).
- **R8.** Turn it on, then run it through internal testing before production.
