<p align="center">
  <img src="assets/logo.svg" width="80" alt="Coffeery logo — pour-over cone + carafe line art">
</p>

# Coffeery

A privacy-first Android brewing companion — pick your gear, dial in strength and
roast, get an exact recipe, and follow a step-by-step timed brew with
per-pour adjustment, background notifications, and full brew journaling.
Everything is local: no account, no network, no tracking.

## Highlights

- **24 brewing methods** — V60, Chemex, Kalita Wave, French Press, AeroPress, Moka Pot,
  Cezve, Ibrik, Cold Brew, Siphon, Espresso, Clever Dripper, Hario Switch,
  Origami, April, Stagg X, Timemore, Beehouse, Cafec Flower, Phin, Cold Drip,
  Percolator, Batch Brewer, Napoletana
- **Custom gear** — add your own brewer with category defaults and auto-generated steps
- **Live ratio recalculation** — three-field input (coffee / ratio / water) with
  instant cross-recalculation in manual mode, or strength-slider auto mode
- **Step-by-step brew timer** — per-method timed steps, pour targets, per-pour
  ±5% water adjustment, merge-pours mode, sound + vibration alerts
- **Background brewing** — foreground service with persistent notification keeps
  the timer running when the app is minimized
- **YouTube tutorials** — every preset links to a verified recipe video
- **Brew journal** — log every brew with rating, tasting notes, grind size, and
  linked coffee bean; 12-week calendar heatmap, streak counter, analytics card
- **Bean inventory** — track your coffee beans with origin, roaster, roast date
- **Learn** — 30 knowledge cards across 9 chapters with locked/unlocked step-map,
  extraction calculator, water mineral reference, and 8-point taste diagnosis
- **Full i18n** — 423 strings each in English (default) + Turkish
- **Zero Material Design** — custom `CoffeeColors` / `CoffeeTypography` /
  `CoffeeShapes` design system, 4 selectable palettes (Terracotta / Espresso /
  Matcha / Berry) with light + dark variants, 24 hand-drawn Canvas line-art
  equipment icons, and serif display typography

## Screens

| Brew Calculator | Timer | Brew Log | Learn | Settings |
|---|---|---|---|---|
| Category tabs, auto/manual ratio, strength slider, roast picker, dual-line segmented pills, one-tap save, YouTube links | 72sp hero countdown in 260dp progress ring, per-pour adjustment, merge-pours toggle, vibration + sound, background service, save-to-log dialog with bean picker | Calendar heatmap, streak banner, analytics card, best-recipe suggestion, bean inventory tab | 9-chapter step-map with locked/unlocked/completed states, 30 knowledge cards, extraction calculator, water minerals, taste diagnosis | Palette swatch preview cards, dark/light/system toggle, language switch, data export/import, backup/restore |

## Architecture

MVVM with a single source of truth and unidirectional data flow (single `AppViewModel`, `StateFlow<AppUiState>`).

```
app/src/main/java/co/coffeery/app/
├── data/
│   ├── local/    Room (5 entities, 5 DAOs, Migration v4→v5), PresetLoader
│   ├── model/    Equipment, BrewStepDef, enums (Grind, RoastLevel, etc.)
│   └── repo/     CoffeeRepository (built-ins + custom gear, export/import)
├── service/      TimerService (foreground), TimerStopReceiver
├── ui/
│   ├── theme/    Color (8 palette profiles), Type, Shape, Texture
│   ├── components/ 24 icons, buttons, cards, sliders, segmented controls
│   └── screens/  brew, equipment, recipes, log, learn, drinks, onboarding, root
└── util/         BrewMath (recipe engine), Format
```

## Tech

| | |
|---|---|
| Language | Kotlin 2.3.0 |
| UI | Jetpack Compose 2026.06.01 (Foundation only) |
| Compiler SDK | 36 (min 26 / target 36) |
| Build | Gradle 8.12, AGP 8.13.2, KSP 2.3.9 |
| DB | Room 2.8.4 |
| Lifecycle | 2.11.0 |
| JDK (CI) | 17 Temurin |
| CI/CD | GitHub Actions — debug + release APK on every push |
| APK | ~6 MB (release, R8 minified + resource shrunk) |

## Build

CI builds both debug and release APKs on every push (see
`.github/workflows/build.yml`) and uploads them as artifacts.

Locally (Android SDK + JDK 17):

```bash
./gradlew assembleDebug
```

## License

MIT
