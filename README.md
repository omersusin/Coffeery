<p align="center">
  <img src="assets/logo.svg" width="80" alt="Coffeery logo"><br>
  <img src="https://img.shields.io/github/v/release/omersusin/Coffeery?color=C75B3C" alt="Release">
  <img src="https://img.shields.io/github/downloads/omersusin/Coffeery/total?color=C75B3C" alt="Downloads">
</p>

# Coffeery

A privacy-first Android brewing companion — pick your gear, dial in strength and
roast, get an exact recipe, and follow a step-by-step timed brew with
per-pour adjustment, background notifications, and full brew journaling.
Everything is local: no account, no network, no tracking.

## Highlights

- **36 brewing methods** — 24 standard brewers (V60, Chemex, Kalita, French Press,
  AeroPress, Moka Pot, Cezve, Ibrik, Cold Brew, Siphon, Espresso, Clever, Switch,
  Origami, April, Stagg X, Timemore, Beehouse, Cafec, Phin, Cold Drip, Percolator,
  Batch Brewer, Napoletana) + 12 equipment-free methods (Cowboy Coffee, Cupping,
  Cloth Filter, Sock Coffee, Decoction, Paper Towel, Swedish Egg Coffee, Improvised
  Turkish, Kopi Tubruk, Arabic Qahwa, Cafe de Olla, Mason Jar Cold Brew)
- **Custom gear** — add your own brewer with category defaults, auto-generated
  steps, and a 25-icon picker for visual customization
- **Live ratio recalculation** — three-field input (coffee / ratio / water) with
  instant cross-recalculation in manual mode, or strength-slider auto mode
- **Step-by-step brew timer** — per-method timed steps, pour targets, per-pour
  ±5% water adjustment, merge-pours mode, customizable step durations (bloom/pour/
  steep/drawdown), auto-advance toggle, sound + vibration alerts
- **Background brewing** — foreground service with persistent notification keeps
  the timer running when the app is minimized
- **YouTube tutorials** — every method links to a verified recipe video in both
  English and Turkish (72 total links)
- **Brew journal** — log every brew with rating, tasting notes, grind size, and
  linked coffee bean; 12-week calendar heatmap, streak counter, analytics card
- **Caffeine tracker** — daily intake summary with safe-zone indicator
- **Bean inventory** — track your coffee beans with origin, roaster, roast date
- **Learn** — 60 knowledge cards across 9 chapters with locked/unlocked step-map,
  extraction calculator, water chemistry guide, 25-term glossary, 10 rotating pro tips,
  grind size visual reference, brew troubleshooter, and 20-note flavor wheel
- **Drinks** — 25 coffee drink recipes with full ingredient steps, 12 coffee
  variety profiles with origin and flavor notes
- **Search** — real-time text filter on Equipment, Learn, and Drinks screens
- **Full i18n** — 728 strings each in English (default) + Turkish
- **Zero Material Design** — custom `CoffeeColors` / `CoffeeTypography` /
  `CoffeeShapes` design system, 8 selectable palettes (Terracotta / Espresso /
  Matcha / Berry / Crema / Mocha / Caramel / Hazelnut) with light + dark
  warm gradient backgrounds, 64 hand-drawn Canvas line-art equipment icons,
  and serif display typography

## Screens

<p align="center">
  <img src="assets/screen-calculator.svg" width="180" alt="Calculator">
  <img src="assets/screen-timer.svg" width="180" alt="Timer">
  <img src="assets/screen-log.svg" width="180" alt="Brew Log">
  <img src="assets/screen-learn.svg" width="180" alt="Learn">
</p>

| Brew Calculator | Timer | Brew Log | Learn | Settings |
|---|---|---|---|---|
| Category tabs, auto/manual ratio, strength slider, roast picker, dual-line segmented pills, one-tap save, YouTube links | 72sp hero countdown in 260dp progress ring, per-pour adjustment, merge-pours, customizable step durations, auto-advance, vibration + sound, background service, save-to-log dialog with bean picker | Calendar heatmap, streak banner, analytics card, caffeine tracker, best-recipe suggestion, bean inventory tab, CSV export | 9-chapter step-map, 60 lessons, extraction calculator, water chemistry, 25-term glossary, pro tips, grind visual, troubleshooter, flavor wheel | 8 palette swatch preview cards, warm gradient backgrounds, dark/light/system toggle, language switch, brew customization, data export/import |

## Architecture

MVVM with a single source of truth and unidirectional data flow.

```
app/src/main/java/co/coffeery/app/
├── data/
│   ├── local/    Room (5 entities, 5 DAOs, Migration v4→v6), PresetLoader
│   ├── model/    Equipment, BrewStepDef, enums (Grind, RoastLevel, etc.)
│   └── repo/     CoffeeRepository (built-ins + custom gear, export/import/CSV)
├── service/      TimerService (foreground), TimerStopReceiver
├── ui/
│   ├── theme/    Color (16 palette profiles), Type, Shape, Texture (gradient)
│   ├── components/ 64 icons, buttons, cards, sliders, segmented controls
│   └── screens/  brew, equipment, recipes, log, learn, drinks, onboarding, root
└── util/         BrewMath (recipe engine), Format
```

## Tech

| | |
|---|---|
| Language | Kotlin 2.3.0 |
| UI | Jetpack Compose 2025.12.01 (Foundation only — no Material) |
| SDK | min 26 / target 35 / compile 35 |
| Build | Gradle 8.13, AGP 8.13.2, KSP 2.3.9 |
| DB | Room 2.7.2 (v6 schema) |
| Lifecycle | 2.10.0 |
| JDK (CI) | 17 Temurin |
| CI/CD | GitHub Actions — debug + release APK on every push |
| APK | 4.7 MB (release, R8 minified + resource shrunk) |
| Codebase | 6,300+ lines Kotlin · 1,000+ XML · 1,100+ JSON |
| i18n | 728 strings EN/TR |

## Build

CI builds both debug and release APKs on every push and uploads them as artifacts.

```bash
./gradlew assembleDebug
```

## License

MIT
