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
| `PackBankClient` | `GET` with `If-None-Match` and a 10 s timeout. Returns `Fetched` / `NotModified` / `Failed(PackBankError)` and never throws |
| `PackBankError` | `NO_CONNECTION` (any `IOException`), `RATE_LIMITED` (HTTP 429), `SERVER_UNAVAILABLE` (other HTTP errors), `INVALID_CONTENT` (didn't parse) |
| `PackBankCache` | Last good download in `filesDir/pack_bank/` (body + ETag), written via temp file + rename |
| `PackBankParser` | `org.json`, tolerant: bad entries, languages or packs are skipped; an unknown `schemaVersion` or non-JSON rejects the file |
| `PackBankRepository` | Singleton. Exposes `content` and `isRefreshing`. `loadCache()` reads the device copy; `refresh(force)` downloads and returns `PackBankRefresh` (`Updated` / `Unchanged` / `Skipped` / `Failed(error)`) |
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

**What the user sees.** The background check from `MainActivity` is silent whether it works or not.
The setup screen speaks up only when the user is waiting on a download, which is when a pack they
own has no content: automatically once per visit, or when they tap the row to retry. Then:
- the existing `LoadingScrim` covers the screen while the download runs;
- a failure shows a long snackbar explaining why:

| Error | Message |
|---|---|
| `NO_CONNECTION` | "Couldn't download the premium packs. Check your internet connection and try again." |
| `RATE_LIMITED` | "The pack server is getting too many requests right now. Please try again in a while." |
| `SERVER_UNAVAILABLE`, `INVALID_CONTENT` | "The pack server isn't available right now. Your other packs still work. Try again later." |

**About GitHub's limits.** GitHub Pages has no hard quota. Its 100 GB/month bandwidth limit is soft,
and when GitHub throttles a site it answers **429**, which the app reports as `RATE_LIMITED`. There
is no signal that says "free quota exceeded" specifically. A disabled Pages site or a deleted file is
a 404 (`SERVER_UNAVAILABLE`). At about 26 KB per full download, and a few hundred bytes per 304,
the soft limit is far away.

## Changing premium content

Step-by-step procedures (editing, adding a pack or language, rebuilding the repo, rollback) are in
[premium-packs-runbook.md](premium-packs-runbook.md). In short: edit `v1/packs.json` in
`partypuzl-packs` and push. No app release is needed unless:
- you add a **new pack id**: it needs a `QuestionPackDefinition` in `QuestionPackCatalog` (name,
  spice, `PackTier.PREMIUM`). No `CATALOG_VERSION` bump: missing catalog packs are inserted on
  every launch. Ids the app doesn't know are ignored;
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
| `PackBankClientTest` | The real `HttpURLConnection` code against a local socket server: 200 + ETag, ETag sent → 304, no ETag on first download, 404 / 500 → `SERVER_UNAVAILABLE`, 429 → `RATE_LIMITED`, unreachable host → `NO_CONNECTION` |
| `PackBankParserTest` | Full document, unknown schema, non-JSON, malformed entries/packs skipped, English fallback, counts |
| `PackBankCacheTest` | Empty on fresh install, round trip with ETag, ETag removal, no temp files left |
| `PackBankRepositoryTest` | Cache before network, new download replaces content + cache, failure and 304 keep content, broken download never overwrites the cache (`INVALID_CONTENT`), the `PackBankRefresh` each case returns, `isRefreshing`, 12 h throttle and `force`, retry after failure |
| `PackDownloadMessagesTest` | Which snackbar each failure shows, and none on success |
| `PremiumPackContentLoaderTest` | Only enabled premium packs are dealt, localised sticky-dare labels, an undownloaded pack adds nothing |
| `PackUiMappersTest`, `GameConfigStateTest` | Premium counts from the bank, `needsDownload`, Start not enabled by an undownloaded pack |
| `QuestionPackContentLoaderTest` | Premium prompts join the same deck as official and custom ones |

`org.json` is a `testImplementation` because Android's stub throws on the JVM.

Not unit-tested: the `MainActivity.onStart()` trigger, the auto-download / retry / scrim wiring in
`GameConfigViewModel` (no ViewModel test harness for that screen yet; it calls the tested
repository and the tested message mapping), and `currentPackLanguage()` (a thin wrapper over
AppCompat). The release checklist's offline/online pass covers these on a device
([release.md](release.md), step 4).
