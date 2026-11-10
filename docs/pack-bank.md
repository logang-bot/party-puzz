# Pack Bank

The premium packs' prompts are not in the APK. They live in a public static JSON file, the
**pack bank**, served by GitHub Pages from the `partypuzl-packs` repo:

    https://logang-bot.github.io/partypuzl-packs/v1/packs.json   (BuildConfig.PACK_BANK_URL)

The file format and how to publish changes are documented in that repo's `README.md`.

**What this does and doesn't protect.** The premium text is no longer readable by unpacking the
APK, and packs can be edited without an app release. But the URL is public, so anyone who finds it
can read the content. A purchase-verified download is sketched in
[pack-bank-cloudflare.md](pack-bank-cloudflare.md).

---

## How the app uses it

| Piece (`data/packBank/`) | Role |
|---|---|
| `PackBankClient` | `GET` with `If-None-Match` and a 10 s timeout. Returns `Fetched` / `NotModified` / `Failed` and never throws |
| `PackBankCache` | Last good download in `filesDir/pack_bank/` (body + ETag), written via temp file + rename |
| `PackBankParser` | `org.json`, tolerant: bad entries, languages or packs are skipped; an unknown `schemaVersion` or non-JSON rejects the file |
| `PackBankRepository` | Singleton. Exposes `content` and `isRefreshing`. `loadCache()` reads the device copy; `refresh(force)` downloads |
| `PackBankContent` | pack id → language → `RemotePackPrompts` (`truths`, `dares`, `stickyDares`, `trivia`) |
| `currentPackLanguage()` | The app's language setting, else the device language. Packs fall back to `en` |
| `data/packs/PremiumPackContentLoader` | Adds enabled premium packs' prompts to the deck. Sticky-dare labels come from `QuestionPromptResolver.durationLabel` |

**When it downloads.**
- `MainActivity.onStart()` calls `refresh()` every time the app comes to the foreground.
  `refresh()` skips the network if the last successful check was under 12 hours ago in this
  process, so a cold start always checks.
- An unchanged file costs only a 304 with no body, thanks to the ETag.
- Tapping an unlocked premium pack that has no content (setup screen) calls `refresh(force = true)`.
- It downloads for **every** user, not just premium. The file is public anyway, and this way a
  purchase made offline (at a party) unlocks packs that are already on the device.

**When it fails.** Nothing crashes and nothing is lost:
- No network, timeout, 404 or 5xx → `Failed`. The content stays whatever it was (cached, or empty
  on a never-connected install).
- A download that doesn't parse (a broken publish) is ignored and the cache is not overwritten.
- Official, custom and mini-game packs never depend on the network.
- On the setup screen a premium pack with no content reads "Downloading…", "Couldn't download · tap
  to retry" or "Downloads when you're online", and doesn't count toward enabling Start.

## Changing premium content

Edit `v1/packs.json` in `partypuzl-packs` and push; see its README. No app release is needed unless:
- you add a **new pack id**: it needs a `QuestionPackDefinition` in `QuestionPackCatalog` (name,
  spice, `PackTier.PREMIUM`) and a `CATALOG_VERSION` bump. Ids the app doesn't know are ignored;
- you change the **format**: bump `schemaVersion` in a new app release first, since older apps keep
  their cache when they see an unknown version. For an incompatible change, publish to a new path
  (`v2/packs.json`) and leave `v1` in place for older installs.

## Migration notes

- Premium prompts used to be `R.array.movie_quiz_*`, `spicy_sticky_dares_*` and `nsfw_*` in both
  `strings.xml` files, referenced through `QuestionSource.MOVIE_TRIVIA`, `SPICY_STICKY_DARES` and
  `NSFW_TRUTHS`/`NSFW_DARES`. All of that was removed. `QuestionCatalog.MAPPING_VERSION` 3 rebuilds
  the `questions` table without the premium rows. The table is only read after the seeder runs, so
  old rows naming the removed enum values are never loaded.
- `partypuzl-packs/tools/export_from_strings.py` produced the first `packs.json` from those arrays.
  The app checkout before the removal is the source if it ever needs re-running.

## Tests

All unit tests (`./gradlew testDebugUnitTest`):

| Test | Covers |
|---|---|
| `PackBankClientTest` | The real `HttpURLConnection` code against a local socket server: 200 + ETag, ETag sent → 304, no ETag on first download, 404, 500, unreachable host |
| `PackBankParserTest` | Full document, unknown schema, non-JSON, malformed entries/packs skipped, English fallback, counts |
| `PackBankCacheTest` | Empty on fresh install, round trip with ETag, ETag removal, no temp files left |
| `PackBankRepositoryTest` | Cache before network, new download replaces content + cache, failure and 304 keep content, broken download never overwrites the cache, `isRefreshing`, 12 h throttle and `force`, retry after failure |
| `PremiumPackContentLoaderTest` | Only enabled premium packs are dealt, localised sticky-dare labels, an undownloaded pack adds nothing |
| `PackUiMappersTest`, `GameConfigStateTest` | Premium counts from the bank, `needsDownload`, Start not enabled by an undownloaded pack |
| `QuestionPackContentLoaderTest` | Premium prompts join the same deck as official and custom ones |

`org.json` is a `testImplementation` because Android's stub throws on the JVM.

Not unit-tested: the `MainActivity.onStart()` trigger, the retry tap in `GameConfigViewModel` (a
one-line call into the tested repository), and `currentPackLanguage()` (a thin wrapper over
AppCompat). The release checklist's offline/online pass covers these on a device
([release.md](release.md), step 4).
