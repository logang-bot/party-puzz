# Store screenshots

`ScreenshotTour` (an instrumented test) walks the app and screenshots each screen. The PNGs land on
this PC in `store-screenshots/<en-US|es-ES>/<light|dark>/NN_name.png` at the repo root. That folder
is gitignored, so review the shots and then upload the ones you want in Play Console → Main store
listing.

## Running it

1. Start the **screenshot emulator** from Android Studio's Device Manager. It's a 1080×1920 device
   used only for this. Play rejects screenshots whose long side is more than 2× the short side,
   so a Pixel 9a (1080×2424) is too tall.
2. Run `ScreenshotTour` like any instrumented test, from Studio or with
   `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.restrusher.partypuzl.screenshots.ScreenshotTour`.
   If more than one device is connected, set `ANDROID_SERIAL`.
3. Open `store-screenshots/`.

One run captures all four variants (English/Spanish × light/dark), which comes to 16 short tests.
To capture fewer, set the instrumentation arguments `locale` (`en`/`es`) and/or `theme`
(`light`/`dark`).

Running it **uninstalls the app and clears its data** on that device. That's Gradle's normal
test cleanup plus `clearPackageData`. Never run it on an emulator whose data you want to keep.

The tour switches on SystemUI demo mode while it runs (12:00 clock, full battery and wifi, no
notification icons) and switches it off afterwards.

## How the screenshots reach the PC

1. `ScreenCapture` takes a full-screen capture through UiAutomation and writes
   `<locale>__<theme>__NN_name.png` through **TestStorage** (`androidx.test.services:storage`).
2. Before uninstalling, Gradle pulls TestStorage files to
   `app/build/outputs/connected_android_test_additional_output/`. That needs the `test-services`
   `androidTestUtil`.
3. `collectStoreScreenshots` (in `app/build.gradle.kts`) runs after every
   `connectedDebugAndroidTest` and turns the `__` separators into folders under
   `store-screenshots/`.

## How the tour works

- `app/src/debug/.../screenshots/ScreenshotHostActivity` is a debug-only stand-in for
  `MainActivity`. It seeds demo data (`ScreenshotSeeder`, `DemoContent`), then shows the real
  `HomeNavigation` with the test's own NavController. It differs from `MainActivity` in two ways:
  - it skips the responsible-drinking dialogs;
  - it lets the test jump straight to any route.

  Release builds never contain it.
- `ScreenshotTour` has one test per area: home/setup, game deal, mini-games, and
  parties/packs/settings. The Test Orchestrator with `clearPackageData` gives each test an empty
  database, so the demo data is seeded once per test, not piled up. Each test numbers its shots
  from a fixed offset, so the files sort in listing order.
- The tour taps through **UiAutomator** (by on-screen text), not a Compose test rule. A Compose
  rule takes over the app's animation clock, so every enter transition stays frozen on its first
  frame and the shots come out blank. With UiAutomator the app runs in real time, which is why the
  tour waits fixed times (`settle`).

## Adding a scene

Add a `driver.navigate(SomeRoute)` + `driver.capture("name")` pair to the matching test. If the
test now has more shots, bump the `firstShotNumber` of the tests after it.

## Policy

The game shots use Classic mode, and the dealt card is random. Check `game_challenge` before
uploading: per [release.md](release.md), no shot may show heavy-drinking copy. Re-run the variant
if it does.
