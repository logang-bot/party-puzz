# Premium Packs Runbook

Step-by-step manual procedures for the premium packs and the pack bank that serves them. For how
the app uses the pack bank, see [pack-bank.md](pack-bank.md). For the JSON format, see the
`partypuzl-packs` repo's `README.md`.

| What | Where |
|---|---|
| Pack bank repo | `github.com/logang-bot/partypuzl-packs` (local checkout: `../partypuzl-packs`) |
| Served file | `https://logang-bot.github.io/partypuzl-packs/v1/packs.json` |
| URL in the app | `BuildConfig.PACK_BANK_URL`, set in `app/build.gradle.kts` `defaultConfig` |
| Pack definitions | `QuestionPackCatalog` (`data/local/appData/appDataSource/`) |

---

## 1. Edit prompts in an existing premium pack

No app release is needed.

1. In `partypuzl-packs`, edit `v1/packs.json`. Each pack is `packs.<packId>.<language>`, with the
   lists `truths`, `dares`, `sticky` or `trivia` (format in the repo README).
   - Edit **every language** you ship (`en`, `es`). A language missing from a pack falls back to `en`.
   - Truth or Dare packs need both `truths` and `dares` in each language.
   - Trivia `correct` must be `"A"` or `"B"`. Sticky dares need `text`, `presentContinuous` and
     `durationSeconds` (> 0).
2. Bump `contentVersion` by one.
3. Validate, then publish (see [§5](#5-validate-and-publish-a-change)).

Devices pick it up on their next cold start, or within 12 hours on a running app.

## 2. Add a new premium pack

This needs **both** a pack bank change and an app release. Publish the content first: an app that
defines a pack the bank doesn't have shows it as "Couldn't download", while a bank entry the app
doesn't know is simply ignored.

**a. Pack bank (publish first)**

1. Pick a stable id: `premium_<name>`, lowercase and underscores. It can never change once
   shipped, because users' enabled state is stored against it.
2. Add `packs.premium_<name>` to `v1/packs.json` with an `en` entry (and `es`), filling the
   list(s) for its game mode:

   | Category in the app | JSON lists |
   |---|---|
   | `PackCategory.TRUTH_OR_DARE` | `truths` **and** `dares` |
   | `PackCategory.STICKY_DARE` | `sticky` |
   | `PackCategory.QUIZ` | `trivia` |

3. Bump `contentVersion`, then validate and publish ([§5](#5-validate-and-publish-a-change)).

**b. App**

1. Add the name string in **both** `values/strings.xml` and `values-es/strings.xml`, next to the
   other pack names: `<string name="pack_<name>">…</string>`.
2. In `QuestionPackCatalog`, add a definition under the `── Premium ──` banner:
   ```kotlin
   val PREMIUM_<NAME> = QuestionPackDefinition(
       id = "premium_<name>",          // exactly the JSON key
       tier = PackTier.PREMIUM,
       category = PackCategory.QUIZ,   // must match the JSON lists, see the table above
       nameRes = R.string.pack_<name>,
       spice = SpiceLevel.MEDIUM       // MILD / MEDIUM / SPICY; this decides icon and colour
   )
   ```
   and add `PREMIUM_<NAME>` to `all` (its position there is the display order).
3. **Don't** add anything to `QuestionCatalog`, `QuestionSource` or `strings.xml` arrays. Premium
   prompts don't use them.
4. **Don't** bump `CATALOG_VERSION` for a new pack: the seeder inserts missing catalog packs on
   every launch. Bump it only if you change an **existing** pack's `tier` or `category`.
5. Build and run `./gradlew testDebugUnitTest`. On a device: the new pack appears locked under
   Premium with its prompt count, and once owned it plays.
6. Release as usual ([release.md](release.md)). Owners of premium get the new pack automatically,
   since premium unlocks every `PackTier.PREMIUM` pack.

## 3. Add a new language

1. App: add the `values-<lang>/strings.xml` translation and the locale in
   `res/xml/locales_config.xml` (normal app localisation).
2. Pack bank: add a `<lang>` entry to **every** premium pack in `v1/packs.json`. A pack without it
   shows English. The app picks the language from its own language setting, or the device language
   when that is "System".
3. Publish the pack bank. The new language works on any app version as soon as the file is live.

## 4. Rebuild the pack bank from scratch

Use this if the repo is lost, or to move it to another account or host.

1. **Content.** `v1/packs.json` is the only file that matters. Sources, in order of preference:
   - your local `../partypuzl-packs` checkout, or any clone of the repo;
   - a device: the app's cache at `files/pack_bank/packs_v1.json`
     (`adb exec-out run-as com.restrusher.partypuzl cat files/pack_bank/packs_v1.json`, debug builds only);
   - the original export: check out the app at `2c16d91` (the last commit with premium arrays in
     `strings.xml`) and run
     `python3 tools/export_from_strings.py <that checkout>/app/src/main/res > v1/packs.json`.
2. **Repo.** Create a **public** GitHub repo named exactly `partypuzl-packs` on the
   `logang-bot` account (no README, no template). Then, in the local folder:
   ```sh
   git init -b main            # if it isn't a git repo yet
   touch .nojekyll             # serve files as-is, no Jekyll processing
   git add -A && git commit -m "Publish the premium packs as static JSON"
   git remote add origin git@github.com:logang-bot/partypuzl-packs.git
   git push -u origin main
   ```
   The repo must contain `v1/packs.json`, `.nojekyll`, `README.md` and `tools/export_from_strings.py`.
3. **Pages.** Repo → Settings → Pages → Source **Deploy from a branch** → branch `main`, folder
   `/ (root)` → Save. The first deploy takes a minute or two (progress under Actions).
4. **Verify** as in [§5](#5-validate-and-publish-a-change) step 3.
5. **Different host or path?** The URL is compiled into the app. Change `PACK_BANK_URL` in
   `app/build.gradle.kts`, release the app, and keep the old URL serving until most users have
   updated: older installs keep using the old one and fall back to their cache when it fails.
   Any static host works, as long as it serves the file over HTTPS and, ideally, sends an `ETag`.

## 5. Validate and publish a change

1. **Validate** in `partypuzl-packs`:
   ```sh
   python3 -m json.tool v1/packs.json > /dev/null && echo valid
   ```
   Then count prompts per pack and language and compare with what you expect:
   ```sh
   python3 -c "import json; d=json.load(open('v1/packs.json'))
   for p,l in d['packs'].items():
       for k,v in l.items(): print(p, k, {n: len(x) for n, x in v.items()})"
   ```
2. **Publish:** commit and `git push`. Pages redeploys in about a minute.
3. **Verify** the live file:
   ```sh
   URL=https://logang-bot.github.io/partypuzl-packs/v1/packs.json
   curl -sS -o /dev/null -w "%{http_code} %{size_download}B\n" -D - "$URL" | grep -i -e etag -e '^[0-9]'
   curl -sS -o /dev/null -w "%{http_code}\n" -H 'If-None-Match: "<etag from above>"' "$URL"
   ```
   Expect `200` with the new size, then `304` for the conditional request.
4. **On a device:** cold-start the app, open the setup screen, and check the premium counts.

## 6. Roll back a bad publish

The app never replaces its cache with a file it can't parse, so a **broken** publish
(invalid JSON, unknown `schemaVersion`) does no harm to devices that already have content. It only
affects new installs. A **valid but wrong** publish (prompts deleted, wrong text) does reach
devices.

Either way, fix it from git:
```sh
git revert HEAD && git push
```
Devices recover on their next check.

## Rules that keep devices safe

- Never rename or reuse a pack id.
- Never remove a pack or a language that shipped apps depend on.
- Keep `schemaVersion` at 1 unless an app release that understands the new format is out. For an
  incompatible format, publish to `v2/packs.json` and leave `v1` in place for older installs.
- Bump `contentVersion` on every content change.
