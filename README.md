# ☕ Coffeery

A privacy-first Android brewing companion. Pick your gear, dial in strength and
roast, get an exact recipe (dose, water, grind, temperature), and follow a
step-by-step timed brew. Everything is local — no account, no network.

Built from a coffee research report: every ratio, temperature, grind size and
brew step comes from that source data, bundled as an app asset and seeded into
the on-device database.

## Highlights

- **Custom design language** — no Material 3. Bespoke color, type, spacing and
  shape systems with hand-drawn Canvas line-art icons and micro-interactions
  (the strength slider fills darker, like brewing coffee, as you drag it).
- **11 built-in methods** — V60, Chemex, Kalita Wave, French Press, AeroPress,
  Moka Pot, Turkish (Cezve), Cold Brew, Siphon, Espresso, Clever Dripper.
- **Custom gear** — add your own brewer; the app classifies it and suggests
  sensible ratio/temperature/grind defaults.
- **Strength slider** — live ratio, descriptive band and color feedback.
- **Roast tuning** — light/medium/dark nudges temperature and grind.
- **Step-by-step brew timer** — per-method timed steps with pour targets and a
  keep-screen-on session.
- **Saved recipes** — name and store favorites (Room).
- **Learn** — short knowledge cards + a "how was your cup?" troubleshooting form.
- **Full i18n** — English (default) + Turkish, no hardcoded UI strings.

## Architecture

MVVM with a single source of truth and unidirectional data flow.

```
data/
  model/    domain models (Equipment, BrewStepDef, enums)
  local/    Room (entities, DAOs, database) + PresetLoader (JSON asset)
  repo/     CoffeeRepository (built-ins + custom gear, defaults)
ui/
  theme/    custom design system (Color, Type, Shape, Theme)
  components/ custom composables (Text, Buttons, Slider, Cards, Icons, Bars…)
  screens/  brew (calculator + timer), equipment, recipes, learn, root
util/       BrewMath (recipe engine), Format
```

- **State:** `AppViewModel` (StateFlow) holds all app state; screens are driven
  by `AppUiState` and emit events back to the ViewModel.
- **Data:** `assets/equipment_presets.json` is the transcribed research data;
  string keys resolve to localized resources at load time.
- **Persistence:** Room stores saved recipes and custom gear only.

## Tech

| | |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (Foundation only — no Material) |
| Min / Target / Compile SDK | 26 / 34 / 34 |
| Build | Gradle 8.9, AGP 8.5.2, KSP 2.0.21-1.0.28 |
| DB | Room 2.6.1 |
| JDK (CI) | 17 |

`minSdk 26` gives full vector drawables, adaptive icons and `java.time`
without desugaring while covering the large majority of active devices.

## Build

CI builds the debug APK automatically on every push (see
`.github/workflows/build.yml`) and uploads it as the `coffeery-debug-apk`
artifact. The workflow pins Gradle 8.9 and regenerates the wrapper in an
isolated step to avoid `gradle-wrapper.jar` checksum issues.

Locally (with Android SDK + JDK 17):

```bash
./gradlew assembleDebug
```
