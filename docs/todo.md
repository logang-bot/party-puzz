# TODO

Follow-up tasks that are known but deliberately deferred.

## Set up a developer website (blocks full Unity Ads fill)

Unity's dashboard is currently rejecting the app's `app-ads.txt` check with `badUrlForm`, and separately flags missing authorized sellers. Per Unity's docs, banner ad demand is significantly reduced without a valid `app-ads.txt`, so this is likely suppressing ad fill right now.

- [ ] Stand up a minimal public website (GitHub Pages is the easiest free option — gives a real, stable domain like `username.github.io`).
- [ ] Host `app-ads.txt` at the **root** of that domain (e.g. `https://yourdomain.com/app-ads.txt`), populated with the full list from Unity Dashboard → **Setup → Organization Settings → App-ads.txt section → "Show full list"**.
- [ ] Set that same root domain (no path, no trailing slash) in Unity Dashboard → **Setup → Organization Settings → Developer Website**.
- [ ] Set the same domain in the Play Store listing's **Website** field (Play Console → Store presence → Store listing).
- [ ] Reuse the same site to host the **Privacy Policy** page required by Play Console → App content → Privacy policy (already tracked as a first-release TODO in `docs/release.md`).

See `docs/ads.md` for the rest of the Unity Ads setup this depends on.
