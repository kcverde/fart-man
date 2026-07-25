# Working on Fart Man

A pass-and-play hangman game for Android. Kotlin, Jetpack Compose, Material 3,
Room. See [README.md](README.md) for what the game is and how the source tree is
laid out; this file covers how to work on it.

Public repo: `kcverde/fart-man` on the user's personal GitHub.

## Working with the user

They are learning git, GitHub, and development process, and want to watch it
happen rather than have it done invisibly.

- Say what a command does and why, in a sentence or two, before running it.
- Name a concept the first time it comes up, then move on. Don't re-teach it.
- Stay brief. They will ask for more when they want it, and a wall of text
  costs them more than a gap does.
- Flag when a step is load-bearing versus ceremony — a PR on a solo docs change
  is practice, not review. Telling those apart is part of what they're after.
- Recommend, then wait. Don't bundle steps opaquely or run ahead.

## Environment

The shell does not inherit a JDK or the Android platform tools, and environment
exported in one command does not survive into the next. Prefix every command
that needs them:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$HOME/Library/Android/sdk/platform-tools:$PATH"
```

Without this you get "Unable to locate a Java Runtime" or `adb: command not
found`, which look like broken tooling and are not.

There is one AVD, named `fartman` (Pixel 8). It is normally shut down; boot it
only when a change actually needs on-device verification.

## Build and test

```bash
./gradlew verifyRoborazziDebug   # the whole JVM suite, including screenshot diffs
./gradlew assembleRelease        # the only thing that exercises R8
```

`verifyRoborazziDebug` runs the unit tests *and* the golden-image comparison, so
it is the single command worth running before pushing. Everything is JVM-only —
there is no `androidTest` source set and nothing here needs a device to test.

Cold builds take roughly four minutes, locally and on CI alike.

## Conventions

**Comments explain why, never what.** Every comment in this codebase justifies a
decision that would otherwise look arbitrary — why the shake events are
buffered, why the ViewModel must be built in `@Before`, why the test clock is
driven manually. If a comment would only restate the code, leave it out.

**Two-space indent**, trailing commas, Kotlin official style. There is no
formatter wired up yet, so match the surrounding file by hand.

**Dependencies go through the version catalog** at
[gradle/libs.versions.toml](gradle/libs.versions.toml) — no inline coordinates
in `build.gradle.kts`.

## Architecture invariants

These are load-bearing. Breaking one compiles fine and fails at runtime.

- **`game/` is Android-free.** `GameUiState` and `GameRules` import nothing from
  the platform, which is what lets the rules be tested without Robolectric.
  Saved state is therefore written as primitives rather than making the state
  `Parcelable`.
- **`FartManViewModel.set()` is the single write path.** Every mutation funnels
  through it so that nothing can change the round without also recording it to
  `SavedStateHandle`. Do not assign `_uiState.value` directly.
- **`soundEnabled` is deliberately absent from saved state.** It lives in
  `SettingsStore` (DataStore), which is durable across installs rather than
  per-process. The `init` block reads it asynchronously.
- **`SoundPlayer` is an interface** so tests can substitute
  `RecordingSoundPlayer`. Keep `AndroidSoundPlayer` the only thing that touches
  `SoundPool`.
- **Build the ViewModel under test in `@Before`, not a field initializer.**
  Fields are constructed before JUnit applies rules, so a ViewModel created
  there captures the real main dispatcher and silently drops everything launched
  into `viewModelScope`. This failure is invisible — the test just sees stale
  state.

## Screenshot tests

Eight goldens live in `app/src/test/screenshots/` and are committed. They exist
mainly to keep dark mode honest: the game was built light-only with a hardcoded
palette, so the dark scheme regresses easily without anyone noticing.

```bash
./gradlew recordRoborazziDebug   # rewrite the goldens
./gradlew verifyRoborazziDebug   # fail on any visual change
```

Re-record only when a visual change is intended, and actually look at the diff
before committing it — a blind re-record turns the whole suite into decoration.
Fart Man bobs on an infinite transition that never lets the test clock go idle,
which is why `ScreenshotTest` sets `mainClock.autoAdvance = false` and advances
a fixed 500 ms; don't remove that.

## CI

[.github/workflows/ci.yml](.github/workflows/ci.yml) runs on every push to
`main` and every PR: `verifyRoborazziDebug`, then `assembleRelease`, uploading
the test and Roborazzi reports when something fails. It reports status only —
there is no branch protection gating merges, by the user's choice.

Action versions are pinned to majors and were all several majors stale on the
first run. When touching the workflow, check the current majors rather than
copying whatever the docs show.

## Known non-issues

Both of these were investigated at length and are deliberately not fixed. Don't
re-diagnose them from scratch.

- **KSP `NullPointerException` annotation on CI.** `Cannot invoke
  "ksp.com.intellij.openapi.application.Application.getService(...)"` on an
  `AWT-EventQueue-0` thread. It is a teardown race inside KSP's shaded IntelliJ;
  `:app:kspReleaseKotlin` completes, the step exits 0, and it appears
  intermittently. `-Djava.awt.headless=true` would probably not help (headless
  JVMs still have an EDT) and changing `kotlin.compiler.execution.strategy`
  would trade real breakage for cosmetic tidiness.
- **Sound icon flickers unmuted on cold start.** The mute setting is read from
  DataStore asynchronously in the ViewModel's `init`, so the first frame shows
  the default. No sound plays during the gap. Judged not worth an API change.

## Verifying on the emulator

The release build has been verified end to end once (all screens render under
resource shrinking, Room survives R8, DataStore and `SavedStateHandle` both
survive process death). If you need to repeat it:

- The release APK is **unsigned**. Sign it with the debug keystore via
  `apksigner` for local install; do not add signing config to the repo without
  the user's say-so.
- **Any process-death test must poll `dumpsys activity activities` until
  `mHaveState=true` and `mIcicle` is non-empty before killing the app.** Never
  sleep and hope. Killing early produces `app died, no saved state` in logcat
  and looks exactly like a state-restoration bug — this cost a session once and
  produced a wrong R8-regression diagnosis.
- `adb shell am kill` will not kill a foreground app. Use `adb root` then
  `kill -9 <pid>`.
- `keyevent 4` (Back) exits the app when the IME is not showing, and `keyevent
  111` (ESC) does not dismiss the Compose IME. Screenshot after every step
  rather than trusting a sequence of blind taps.

## Platform backlog

As of 2026-07-25, unstarted, roughly in the order last recommended:

1. **Android Lint** — no static analysis runs today. Run `lintRelease`, fix what
   is real, add a `lint {}` block, baseline only what is consciously deferred,
   add the step to CI.
2. **Renovate** — would have caught both stale-pin episodes unprompted.
3. **Spotless + ktfmt and `.editorconfig`** — the style above, enforced.
4. **Java 11 → 17** in `compileOptions` plus the matching Kotlin `jvmTarget`.
   `compileOptions` still says 11 while the README asks for JDK 17.
5. **Release signing** — keystore plus a gitignored `keystore.properties` or env
   vars. Only matters once the app is distributed.
6. **`createComposeRule` → v2 API** — swaps `UnconfinedTestDispatcher` for
   `StandardTestDispatcher` and may need explicit synchronization added to the
   screenshot tests.

The user's stated end goal is gameplay work; platform items are groundwork, so
prefer finishing them cheaply over gold-plating them.
