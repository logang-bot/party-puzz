# Testing the premium purchase

How to check the `remove_ads` one-time purchase end to end. Purchases can only be tested on a build
**installed from Google Play**. A debug build installed from Studio or `adb` can't load the product,
so every buy button shows "Purchases are unavailable right now".

Background: [ads.md](ads.md#premium--the-one-time-purchase). Play Console setup: [release.md](release.md), setup step 4.

---

## One-time setup

1. **Payments profile.** Play Console → Setup → Payments profile is complete.
2. **Product.** Monetize with Play → Products → One-time products → `remove_ads`:
   - It is **Active** and has a price.
   - The name and description talk about premium packs and unlimited custom packs, and don't mention removing ads.
3. **License tester.** Play Console (all apps) → Settings → **License testing**:
   - Add the Google account signed in on the test phone.
   - Set the response to `RESPOND_NORMALLY`.
   - License testers see "Test card, always approves" (and declining or slow variants) and are never charged.
4. **Internal testing.** Testing → Internal testing:
   - Add the same account as a tester.
   - Upload a release `.aab` with a `versionCode` higher than the last one ([release.md](release.md)).
   - Open the opt-in link on the phone and install from the Play Store.

> If the price never appears and every button says "unavailable", the cause is almost always one of
> these: the app wasn't installed from Play, the account isn't a license tester or tester, the
> product isn't Active, or the product was activated less than a few hours ago.

## Test pass

Run these on the Play-installed build, signed in with the license-tester account.

### Before buying
- [ ] Setup screen: premium packs show a lock. Tapping one opens the sheet with **only** "Unlock everything", plus the price (no "Watch a short ad" on `main`).
- [ ] Settings → Purchases → **Purchase App** shows the price in its subtitle.
- [ ] Your packs: with one pack already, "Create pack" opens the premium sheet. The hint reads "Free version · 1 of 1 packs used".
- [ ] In a pack with 5 entries, "Add entry" opens the premium sheet.

### Cancel and failure
- [ ] Open the purchase sheet and back out. Nothing unlocks and no toast appears.
- [ ] Buy with **"Test card, always declines"**. The toast says the purchase didn't go through, and nothing unlocks.

### Pending payment
- [ ] Buy with **"Slow test card, approves after a few minutes"**. The toast says the payment is pending, and nothing unlocks yet.
- [ ] Wait a few minutes, then leave the app and come back. Premium unlocks (the app rechecks purchases each time it returns to the foreground).

### Successful purchase
Use a fresh account, or refund first (below), to repeat from each entry point.
- [ ] Buy from the **unlock sheet** with "Test card, always approves". The toast says "Premium unlocked" and all three premium packs unlock on the setup screen.
- [ ] Settings shows **App Purchased**.
- [ ] Your packs: the free-version hint is gone, and you can create a second pack and a 6th entry.
- [ ] Repeat once starting from **Settings → Purchase App**.
- [ ] Each premium pack plays when enabled on its own.

### Premium content downloads
Premium prompts come from the pack bank ([pack-bank.md](pack-bank.md)), not the APK.
- [ ] After buying, each premium pack shows its prompt count (24 / 20 / 40), not a download message.
- [ ] Airplane mode on, force-stop, reopen: premium packs still show their counts and still play (cached copy).
- [ ] Clear the app's storage, keep airplane mode on, and open the app: premium rows read "Downloads when you're online" (premium isn't restored yet either, since that needs Play too). Turn the network on, then leave the app and come back: premium is restored and the counts appear.

### Survives a reinstall
- [ ] Uninstall, reinstall from Play, open the app. Within a few seconds premium is restored without buying again.

### Refund relocks
1. Play Console → **Order management** → find the test order → **Refund**, and tick **Remove entitlement**.
2. Close the app fully, wait a minute, and reopen it.
- [ ] The premium packs are locked again and switched off, and they never come up in a game.
- [ ] Custom packs over the free limit still play and can be edited, but creating another pack or entry opens the premium sheet again.

> Play can take a few minutes to drop a refunded purchase from the device's cache. If it's still
> unlocked, wait and reopen the app again.

### Acknowledgement
- [ ] Three days after a test purchase, the order in Order management is still **not** auto-refunded. Play refunds purchases that were never acknowledged, so a refund here means acknowledgement is broken.
