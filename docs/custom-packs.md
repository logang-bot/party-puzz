# Custom Packs

Packs the user writes themselves. Reached from **Settings → Your packs**, and drawn from in-game exactly like the built-in decks.

`PackTier.CUSTOM` had existed as a placeholder since the pack system landed; this is the feature behind it.

---

## Why authored text needs its own tables

A built-in prompt is split across three places so it stays translatable — the text lives in `strings.xml` and a `questions` row points at it by `(QuestionSource, index)`. See [question-packs.md](question-packs.md).

**A prompt the user typed has no translation to point at.** So custom packs get two tables of their own:

| Table | Holds |
|---|---|
| `custom_packs` | Name, description, declared category, spice, `createdAt`. The things `QuestionPackCatalog` supplies for a built-in pack. |
| `custom_entries` | The prompt text itself, plus whatever the entry's type needs. |

Both hang off `question_packs.id` with `ON DELETE CASCADE`, so a custom pack still gets an ordinary `question_packs` row. That row is what carries the tier and the on/off flag, and what the seeder and the content loader join against — a custom pack is a normal pack that happens to keep its content elsewhere.

### Why not nullable columns on `questions`?

Three reasons, in increasing order of importance:

1. `QuestionSource`'s whole contract is "which resource deck". A `CUSTOM_TRUTHS` value would force a bogus branch in `QuestionPromptResolver.primaryArrayOf()`, whose `when` returns an `R.array`, and would make `sizeOf()` meaningless.
2. Trivia needs three extra fields and sticky dares one, so it would mean ~5 permanently-null columns on the ~200 built-in rows — and the `"<SOURCE>_<index>"` primary key collides across custom packs.
3. **`QuestionDao.replaceAll()` does `DELETE FROM questions`** every time `QuestionCatalog.MAPPING_VERSION` changes. Keeping authored text in a table the seeder cannot address is a structural guarantee, not a `WHERE` clause someone forgets in a later refactor.

### The two ways custom packs used to die

Both were real, and both are now guarded:

- `QuestionPackSeeder` calls `deleteRetiredCatalogPacks(...)` on **every launch** to retire packs the catalog no longer defines. A custom pack is never in the catalog, so the query carries `WHERE tier != 'CUSTOM'`. Without it every authored pack disappears on the *next* app start — which passes a same-session smoke test.
- `replaceAll()` needs no guard: it only touches `questions`.

---

## Data model

```kotlin
enum class SpiceLevel { MILD, MEDIUM, SPICY }

enum class CustomEntryType(val category: PackCategory) {
    TRUTH(PackCategory.TRUTH_OR_DARE),
    DARE(PackCategory.TRUTH_OR_DARE),
    STICKY_DARE(PackCategory.STICKY_DARE),
    TRIVIA(PackCategory.GENERAL_KNOWLEDGE)
}
```

`CustomEntryType.category` mirrors `QuestionSource.category` so the content loader can fan out over authored entries with the same `when` it already uses. `TRUTH` and `DARE` are separate values for the same reason `OFFICIAL_TRUTHS` and `OFFICIAL_DARES` are: they are separate pools, and a pack holding only one half is worth flagging.

There is deliberately **no mini-game type** — mini-games are code, not prompts — so `MINI_GAME` is absent from the category picker (`AuthorableCategories`).

**Spice is cosmetic.** It picks the pack's icon and accent and shows as a chip. Nothing filters on it; built-in packs express intensity by membership instead (`premium_spicy`, `NSFW_*`).

**A pack's declared category is a label.** The content loader pools by each *entry's* type, never by the pack's, so a pack may hold any mix of entry types. The declared category drives the row chip and the default type when authoring a new entry.

---

## What each entry type stores

| Type | Fields used | Notes |
|---|---|---|
| `TRUTH` / `DARE` | `text` | Pooled straight into `truths` / `dares`. |
| `STICKY_DARE` | `text`, `durationSeconds` | Duration is picked from `StickyDurationPresets` — 60 / 300 / 600 / 1800 s. |
| `TRIVIA` | `text`, `optionA`, `optionB`, `correctOption` | **Two** options, matching `TriviaPrompt` and the two answer cards the game screen renders. |

`correctOption` is stored as a one-character `TEXT` (`"A"` / `"B"`) because Room has no built-in `Char` converter; it is widened back to a `Char` at the boundary.

### Sticky dares: the two generated fields

`StickyDarePrompt` carries four fields, but authoring only asks for two:

- **`presentContinuous`** falls back to the dare text. The ticker reads *"&lt;player&gt; is &lt;presentContinuous&gt; for &lt;durationLabel&gt;"*, and it is also the dedup key in `GameScreenViewModel` that stops one player collecting the same sticky dare twice — so it must never be blank. The authoring validator enforces non-blank text for exactly this reason.
- **`durationLabel`** is generated from the picked seconds via the `sticky_dare_duration_minutes` plural, so it still localises even though the dare text does not. Built-in sticky dares carry a hand-written label in a parallel array instead.

---

## The playability warning

`EnabledPackContent.availableCategories` requires **both** truths and dares before `TRUTH_OR_DARE` is playable — the reveal screen offers both cards with no way to hide one.

That rule applies to the whole pooled deck, not per pack, so a truths-only custom pack is perfectly playable next to one that has dares. The manager therefore **warns and never blocks**:

| Warning | When |
|---|---|
| `EMPTY` | No entries yet |
| `TRUTHS_ONLY` | Has truths, no dares |
| `DARES_ONLY` | Has dares, no truths |

Shown on the pack card in the manager and as a line under the header in the editor.

---

## Screens

| Screen | Package | Does |
|---|---|---|
| `CustomPacksScreen` | `ui/views/customPacks/list/ui` | The manager: every pack, its switch, edit/delete, the playability warning, sticky "Create new pack" CTA |
| `CreateCustomPackScreen` | `.../create/ui` | The pack shell in four numbered steps — name, category, spice, description (140 chars) |
| `CustomPackEditorScreen` | `.../editor/ui` | A pack's contents; entries added one at a time |
| `CreateCustomEntryScreen` | `.../entry/ui` | One entry: step 01 picks the type, later steps change to match, live "How it will play" preview |

Creating a pack navigates straight into its empty editor, popping the create screen, so the next thing the author sees is "add your first entry". Editing an existing pack just pops back.

Colour and icon mappings live in `ui/views/customPacks/model/CustomPackLook.kt` as extension properties, keeping Compose types out of the data layer — the same split `QuestionPackDefinition` uses for built-in packs. `SpiceLevel.accent` resolves through the shared `PackAccent` enum (`SpiceLevel.packAccent` in `data/models`, then `PackAccent.color` in `ui/theme`), so a custom pack and a built-in one name their colour the same way.

---

## Schema

Database **v10**. `MIGRATION_9_10` creates both tables and the `index_custom_entries_packId` index.

The statements were verified against Room's own generated schema rather than hand-written: `exportSchema` was flipped on temporarily, the build run once, the `createSql` copied from `10.json`, and both edits reverted. This matters because `exportSchema = false` leaves nothing to diff against, and a *registered* migration whose SQL doesn't match throws on open — `fallbackToDestructiveMigration()` does **not** rescue that path.

---

## Key Files

| File | Role |
|---|---|
| `data/models/CustomPackModels.kt` | `SpiceLevel`, `CustomEntryType`, duration presets, the two drafts |
| `data/local/entities/CustomPackEntity.kt` | Pack metadata row |
| `data/local/entities/CustomEntryEntity.kt` | **The only table holding prompt text** |
| `data/local/dao/CustomPackDao.kt` | `CustomPackSummary` — metadata + enabled flag + per-type counts in one query |
| `data/local/dao/CustomEntryDao.kt` | Entries per pack, and the playable JOIN |
| `data/repositories/CustomPackRepositoryImpl.kt` | Id generation, the paired-row insert, draft → entity |
| `data/packs/QuestionPackContentLoader.kt` | Second pass that pools authored entries into the same deck |
| `data/local/dao/QuestionPackDao.kt` | `deleteRetiredCatalogPacks` — the `tier != 'CUSTOM'` guard |
| `navigation/CustomPacksGraph.kt` | The four destinations |
| `ui/views/gameConfig/PackLabel.kt` | Resource-or-literal pack name, so one row serves built-in and custom |
| `ui/views/gameConfig/ui/CustomPackGroup.kt` | The Custom group on the setup screen |
| `ui/views/customPacks/model/CustomPackLook.kt` | Spice/type → icon, accent, labels |

---

## On the setup screen

Authored packs appear in the **Custom** group alongside the official and premium ones, with the same row design, a live `Custom · 1/2` count, and a **Manage** link into the authoring flow. Toggling a row there is a per-session choice exactly as it is for a built-in pack — both write `question_packs.isEnabled`.

Two things had to change for that to work:

- **`PackUiModel.name`** was an `@StringRes Int` and could not hold a user-typed name. It is now a sealed `PackLabel` — `Resource` or `Literal` — resolved by `PackLabel.text()` at the render site.
- **`GameConfigState.hasEnabledPack`** counted only official and premium packs. Since the content loader pools authored entries into the same deck, an enabled custom pack is a playable game; leaving them out meant turning every official pack off disabled Start on a non-empty deck.

Details in [game-config.md](game-config.md).

---

## Related

- [question-packs.md](question-packs.md) — the built-in pack system this plugs into
- [game-config.md](game-config.md) — the setup screen
- [game-deal-flow.md](game-deal-flow.md) — how deal availability is consumed
