# Open-source licences

Most of PartyPuzl's libraries (AndroidX, Room, Hilt, Coil, kotlinx) are Apache 2.0. That licence
lets us ship them in a closed-source app on one condition: the app must carry a copy of the
licence. **Settings → Open-source licenses** does that.

## How it works

| Piece | Role |
|---|---|
| `com.mikepenz.aboutlibraries.plugin` (Gradle) | Walks the resolved dependency graph on every build and writes `app/build/generated/aboutLibraries/<variant>/res/raw/aboutlibraries.json`, with each library's licence and the full Apache 2.0 text |
| `com.mikepenz:aboutlibraries-core` | Parses that JSON at runtime |
| `data/licenses/OpenSourceLicensesSource` | Loads it off the main thread and maps it to `OpenSourceLibrary`, sorted by name |
| `ui/views/licenses/LicensesScreen` + `LicensesViewModel` | The list. Tapping a row opens `LicenseTextDialog` with the full text |
| `LicensesRoute` | Reached from Settings → Legal |

Nobody maintains a list by hand. A new dependency shows up on the next build.

## Notes

- **Licences without a bundled text.** Play Billing and Play Services use the *Android Software
  Development Kit License*, and CameraX also cites libyuv's BSD licence. The plugin has no text for
  those, so the dialog shows the licence name and its URL instead.
- **Version pin.** The plugin and core are pinned to `11.2.3` because the project is on Kotlin
  2.0.0. Newer 11.x and 12.x lines expect a newer Kotlin. Move them together when Kotlin is bumped.
- **R8 / resource shrinking.** The core looks the JSON up with `getIdentifier("aboutlibraries",
  "raw", …)`. The shrinker's default safe mode keeps it, because that string literal is in the
  code. If strict shrinking is ever turned on, add `@raw/aboutlibraries` to `res/raw/keep.xml`.
  Check the screen in the release checklist ([release.md](release.md#4-test-the-release-build)).
