# Question Packs

Question packs are the collections of prompts the game draws from. Players switch them on and off on the setup screen; whatever is enabled becomes the deck for that game.

Packs replaced the old "Pick what to play" category toggles. A pack still feeds exactly one deal, so switching off every pack of a category is what removes that deal from the game.

---

## Where a question lives

A question is split across three places. This is the central design decision, so it's worth stating plainly:

| Part | Where | Why |
|---|---|---|
| **Text** | `res/values*/strings.xml` | Prompts must stay translatable. Moving them into Room would freeze them in whichever language was active at seed time. |
| **Pack membership** | Room table `questions` | This is what the user reorganises. |
| **Enabled / unlocked** | Room table `question_packs` | The only pack state the user changes. |

A `QuestionEntity` therefore carries **no text**. It points at a position in a source array, and `QuestionPromptResolver` reads the text at draw time in the current locale.

> This split applies to **built-in** packs only. A prompt the user wrote has no translation to point at, so custom packs store their text verbatim in their own tables — see [custom-packs.md](custom-packs.md).

```kotlin
@Entity(tableName = "questions", foreignKeys = [ForeignKey(
    entity = QuestionPackEntity::class,
    parentColumns = ["id"], childColumns = ["packId"],
    onDelete = ForeignKey.CASCADE)],
    indices = [Index("packId")])
data class QuestionEntity(
    @PrimaryKey val id: String,   // "OFFICIAL_TRUTHS_13"
    val packId: String,
    val source: QuestionSource,
    val sourceIndex: Int,
    val isEnabled: Boolean = true
)
```

> **Never persist an `R.array` id.** Resource ids are regenerated on every build and can shift, so a stored one silently starts pointing at a different array. `source` is a [`QuestionSource`] enum instead — Room stores enum constants by name, which is stable across builds. `QuestionPromptResolver` is the only place that maps a source to its actual `R.array` ids.

`isEnabled` on a question is per-question opt-out. Nothing toggles it yet — the setup screen works at pack level — but the column is what makes "mute this one question" possible later without a migration.

---

## Index drift, and the two guards against it

Pointing at an array by index is the fragile part of this design. Two rules keep it safe:

1. **Source arrays are append-only.** Never reorder or delete an item — add to the end. Reordering silently reassigns questions to the wrong packs.
2. **Editing an array means bumping `QuestionCatalog.MAPPING_VERSION`.** The seeder compares it against the value stored in DataStore and rebuilds every question row when they differ. That repairs any drift.

A rebuild resets per-question enable flags. Pack-level toggles and unlocks are untouched.

As a backstop, `QuestionPackSeeder` drops any index that no longer resolves and `QuestionPromptResolver` bounds-checks every read, so an un-bumped edit degrades to a shorter deck rather than a crash mid-party.

---

## The packs

14 built-in packs — 11 official + 3 premium — plus however many the user writes themselves. Official packs are the original flat decks split by theme, so a group can pick the *kind* of night they want.

The three values of `PackTier` are what the setup screen groups by: `OFFICIAL` (free, on by default), `PREMIUM` (needs unlocking) and `CUSTOM` (authored, always unlocked).

### Truth or Dare — `truth_texts` (40) + `dare_texts` (52)

| Pack | Truths | Dares | Total |
|---|---|---|---|
| **Icebreakers** — light, safe for any group | 7 | 16 | 23 |
| **Confessions** — admissions, lies, guilt, phone dirt | 18 | 8 | 26 |
| **Party Animals** — physical, performance, silly & gross | 5 | 20 | 25 |
| **This Room** — the people present; absorbed the Love & Exes questions | 10 | 8 | 18 |

Every Truth-or-Dare pack deliberately holds **both** halves — see the empty-pool rule below.

### Sticky Dares — `sticky_dares` (22)

| Pack | Count | Theme |
|---|---|---|
| **Voices & Accents** | 7 | how you sound |
| **Verbal Tics** | 7 | speech patterns and word rules |
| **Body & Persona** | 8 | gestures, posture, character |

### General Knowledge — `gk_questions` (30)

| Pack | Count |
|---|---|
| **World Geography** | 9 |
| **Space & Science** | 12 |
| **Mixed Bag** — math, art, music, sports | 9 |

### Mini-games

**Mini-games** is the one pack with no question rows — mini-games are code, not prompts. `QuestionPackContentLoader` checks the pack's enabled flag directly, and the setup screen reports `MiniGame.entries.size` rather than a misleading zero.

### Premium

| Pack | Category | Content |
|---|---|---|
| **Movie Night** | General Knowledge | `movie_gk_*` (12) |
| **Spicy** | Sticky Dare | `spicy_sticky_dares_*` (8) |
| **NSFW Confessions** | Truth or Dare | `nsfw_truth_texts` + `nsfw_dare_texts` (10 + 10) |

Premium packs own their arrays outright, so they take every index.

### Custom

Written by the user, so they have no catalog entry, no fixed count and no string arrays — their prompts are stored verbatim in `custom_entries`. They are always unlocked and are never touched by the seeder. Everything about them is in [custom-packs.md](custom-packs.md).

---

## The empty-pool rule

`EnabledPackContent.availableCategories` requires **both** truths and dares before offering `TRUTH_OR_DARE`. The reveal screen shows a Truth card and a Dare card with no way to hide one, so a deck with truths but no dares would hand back an empty prompt when someone picked Dare.

The other three categories need only their own list to be non-empty.

---

## Adding a pack

1. Add prompts to the end of an existing source array, or add a new array to `values/strings.xml` **and** `values-es/strings.xml` (parallel arrays must stay the same length).
2. If you added a new array, add a `QuestionSource` constant and wire it up in `QuestionPromptResolver`.
3. Add a `QuestionPackDefinition` to `QuestionPackCatalog` and list it in `all`.
4. Add the index list to `QuestionCatalog.byPack`.
5. **Bump `QuestionCatalog.MAPPING_VERSION`.**
6. Add the pack's name string (`pack_*`) in both languages.

No database migration is needed. Packs are seeded with `INSERT OR IGNORE` so existing toggles survive, and `deleteRetiredCatalogPacks` retires packs the catalog no longer defines — their questions cascade away. That query skips `PackTier.CUSTOM`, which is never in the catalog; see [custom-packs.md](custom-packs.md).

---

## Unlocking a premium pack

Tapping a locked row opens `UnlockChoiceBottomSheet` with two options:

| Option | Grants | Persisted where |
|---|---|---|
| **Watch a short ad** | This pack only, until the process dies | `SessionUnlocksSource` (in-memory) |
| **Unlock everything** | Every premium pack, permanently, plus ad removal | `remove_ads` purchase → `isAdFree` in DataStore, mirrored to `isUnlocked` rows |

The purchase reuses the existing `remove_ads` product rather than adding a second SKU — see [ads.md](ads.md). `GameConfigViewModel` treats `isAdFree == true` as "all premium unlocked".

A pack unlocked by ad is switched **on** immediately, and its meta line reads "Unlocked for this session" so the unlock doesn't look permanent. `isEnabled` is always ANDed with `isUnlocked` when building the UI model, so a locked pack can never read as enabled whatever the stored flag says.

---

## Feeding the game

`QuestionPackContentLoader.loadEnabledContent()`:

1. Runs `QuestionPackSeeder.seedIfNeeded()`.
2. Reads `getPlayableQuestions()` — questions in enabled packs, not individually muted (one JOIN, done in SQL).
3. Resolves each row's text through `QuestionPromptResolver` and pools it by category.
4. Reads `getPlayableEntries()` — the same JOIN over `custom_entries` — and pools authored text into the *same* lists, bypassing the resolver. See [custom-packs.md](custom-packs.md).

Because both passes feed one set of pools, the game screen never learns that custom packs exist; it just asks for a deck.

`GameScreenViewModel` calls it once in `init` — packs cannot be toggled mid-game — and stores the result in `packContent`. Two things follow:

- **Prompt draws** — `buildChallengeContent()` reads from `packContent`. Two enabled Icebreakers-and-Confessions packs become one pooled truth list; the game doesn't know which pack a prompt came from.
- **Deal availability** — `availableCategories` is written into `GameScreenState.enabledCategories` and `availableDealTypes` filters on it. See [game-deal-flow.md](game-deal-flow.md).

`enabledCategories` defaults to all four so the first frame renders normally, then narrows when the load returns.

---

## Key Files

| File | Role |
|---|---|
| `data/models/QuestionSource.kt` | Stable source enum + `QuestionRef` |
| `data/local/appData/appDataSource/QuestionCatalog.kt` | **The categorisation** — index lists per pack, `MAPPING_VERSION` |
| `data/local/appData/appDataSource/QuestionPackCatalog.kt` | The 14 pack definitions |
| `data/local/entities/QuestionEntity.kt` | Question → pack row |
| `data/local/entities/QuestionPackEntity.kt` | Pack enabled / unlocked row |
| `data/local/dao/QuestionDao.kt` | Playable-question JOIN, counts, `replaceAll` |
| `data/packs/QuestionPromptResolver.kt` | Source + index → text (the only `R.array` map) |
| `data/packs/QuestionPackSeeder.kt` | Keeps both tables in sync with the catalog — skips `PackTier.CUSTOM` |
| `data/packs/QuestionPackContentLoader.kt` | Enabled packs → playable deck |
| `data/models/PackPrompts.kt` | `EnabledPackContent` and the empty-pool rule |
| `data/local/appData/appDataSource/SessionUnlocksSource.kt` | Rewarded-ad unlocks |
| `ui/views/gameConfig/ui/QuestionPacksSection.kt` | The three groups — Official, Premium, Custom |
| `ui/views/gameConfig/ui/CustomPackGroup.kt` | The Custom group's rows, count and Manage link |
| `ui/views/gameConfig/PackLabel.kt` | Resource-or-literal pack name, so one row serves built-in and custom |

---

## Related

- [custom-packs.md](custom-packs.md) — packs the user writes themselves
- [game-config.md](game-config.md) — the setup screen the packs live on
- [game-deal-flow.md](game-deal-flow.md) — how deal availability is consumed
- [ads.md](ads.md) — rewarded ad and the `remove_ads` purchase
