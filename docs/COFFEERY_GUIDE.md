# Coffeery — The Complete Guide

A privacy-first Android coffee brewing companion. Pick your gear, dial in strength and roast, get an exact recipe, and follow a step-by-step timed brew with per-pour adjustment, background notifications, and full brew journaling. Everything is local by default — no account, no network, no tracking.

**License:** MIT &nbsp;|&nbsp; **Platform:** Android 8+ (API 26) &nbsp;|&nbsp; **Version:** 3.0.1 (build 6)

---

## Before vs After

Coffeery has been massively enhanced from its v3.0.1 baseline across a series of development sprints. The table below captures the scope of the expansion.

| Feature | Before (v3.0.1) | After |
|---|---|---|
| Design System | Basic CoffeeTheme | Full system: 8 palettes × light/dark, vintage accents, grain textures, 4 spring tokens |
| Equipment | 36 brewers | 36 brewers (cleaned duplicates) |
| Learn Content | 9 chapters, 60 cards | 14 chapters, 85 cards |
| Drinks | 25 | 70+ |
| Coffee Varieties | 12 | 22 |
| Pro Tips | 10 | 50 |
| Glossary | 25 terms | 65 terms |
| Brew Issues (Troubleshooter) | 4 | 10 |
| Flavor Notes | 20 | 37 |
| Quiz | None | 12 questions with score tracker |
| Achievements | None | 9 badges |
| Stats Dashboard | None | Canvas charts (equipment, roast, time-of-day, ratio, rating) |
| Brew Comparison | None | Side-by-side diff tool |
| Recipe Editing | None | Tap to load into calculator |
| Photo Capture | None | Camera in brew save dialog |
| PDF Export | None | A4 brew reports |
| Google Sign-In | None | Profile photo, Drive backup/restore |
| Timer Sounds | System beep | Custom chimes (dual-tone for steps, melody for finish) |
| Haptic Feedback | None | Tap/confirm/tick/segment/long-press patterns |
| Onboarding | 4 static slides | 3-step interactive flow |
| Empty States | None | Custom CTAs for recipes, beans |
| Food Pairing | None | 5 pairings in Learn |
| Culture Facts | None | 4 coffee culture traditions |
| Hands-free Mode | None | Proximity sensor dims screen |
| String Resources | 728 per locale | 1,226 per locale (EN/TR parity) |
| Bottom Nav | 6 tabs | 5 tabs (Drinks merged into Recipes) |

---

## Architecture

### Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.0 |
| UI | Jetpack Compose (2024.09 BOM, Foundation only — **zero Material Design**) |
| Database | Room 2.7.2 (5 entities, 5 DAOs, v6 schema) |
| Architecture | MVVM with unidirectional data flow, manual DI (no Hilt/Koin) |
| Image Loading | Coil 2.7.0 (Compose) |
| Google Services | Play Services Auth 21.3.0, Drive 17.0.0, API Client 2.7.2 |
| Lifecycle | Lifecycle 2.10.0 (runtime + ViewModel + Compose) |
| Build | Gradle 8.13, AGP 8.13.2, KSP 2.3.9, JDK 17 Temurin |
| Min / Target / Compile SDK | 26 / 35 / 35 |

### Source Tree

```
app/src/main/java/co/coffeery/app/
├── CoffeeApp.kt              Application (notification channels)
├── MainActivity.kt            Single Activity (edge-to-edge, splash screen)
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt     Room DB (v6 schema, 5 entities)
│   │   ├── Daos.kt            RecipeDao, SettingsDao, BrewLogDao, BeanDao, CustomEquipmentDao
│   │   ├── Entities.kt        RecipeEntity, SettingsEntity, BrewLogEntity,
│   │   │                         BeanEntity, CustomEquipmentEntity
│   │   └── PresetLoader.kt    Parses equipment_presets.json → Equipment domain models
│   ├── model/
│   │   └── Models.kt          Equipment, BrewStepDef, Grind, RoastLevel,
│   │                             BrewCategory, TempMode, Palette, ThemeMode, StepKind
│   └── repo/
│       └── CoffeeRepository.kt  Single source of truth; export/import/CSV, category defaults
├── service/
│   ├── TimerService.kt        Foreground service for background brewing
│   └── TimerStopReceiver.kt   Broadcast receiver for timer stop action
├── ui/
│   ├── theme/
│   │   ├── Color.kt           8 palettes × 2 modes = 16 CoffeeColors profiles
│   │   ├── Type.kt             Fraunces (serif/display) + Manrope (sans/body) type scale
│   │   ├── Shape.kt           CoffeeShapes (4 corner radii) + CoffeeSpacing (6 size steps)
│   │   ├── Texture.kt         Seeded grain texture rendered with Canvas drawBehind
│   │   ├── Motion.kt          MotionTokens + 4 spring specs (press, cardExpand, page, counter)
│   │   └── Theme.kt           CoffeeTheme composition local (colors, typography, shapes)
│   ├── components/
│   │   ├── Bars.kt             Top bar, screen header
│   │   ├── Buttons.kt          PrimaryButton, SecondaryButton, icon buttons
│   │   ├── Controls.kt         SegmentedControl, Chip, toggle, radio
│   │   ├── EquipmentIcons.kt   64 hand-drawn Canvas line-art equipment silhouettes
│   │   ├── EquipmentName.kt    Localised equipment name resolver
│   │   ├── Icons.kt            Glyph enum + LineIcon (all 100+ line-art glyphs)
│   │   ├── Modal.kt            CoffeeDialog (custom sheet/dialog)
│   │   ├── StrengthSlider.kt   Coffee-strength gradient slider
│   │   ├── Surfaces.kt         CoffeeCard, surface containers
│   │   └── Text.kt             AppText, AppTextField
│   ├── haptic/
│   │   └── AppHaptics.kt       Tap, confirm, reject, tick, segment, long-press feedback
│   └── screens/
│       ├── brew/
│       │   ├── CalculatorScreen.kt   Ratio engine, strength slider, roast offset, quick stats
│       │   └── BrewTimerScreen.kt    Full-screen timer with step ring, per-pour controls
│       ├── equipment/
│       │   ├── EquipmentScreen.kt    Gear browser with Canvas icons, search, quick access
│       │   └── AddEquipmentScreen.kt  Custom gear creation with category defaults
│       ├── recipes/
│       │   └── RecipesScreen.kt      Save/load/edit recipes, featured recent, drinks sub-tab
│       ├── drinks/
│       │   ├── DrinksScreen.kt       70+ coffee drink catalog
│       │   ├── DrinkDetailScreen.kt  Full ingredient steps per drink
│       │   └── DrinkContent.kt       Drink data model
│       ├── log/
│       │   ├── BrewLogScreen.kt      Heatmap calendar, streak banner, timeline, comparison
│       │   ├── BrewStatsSection.kt   6 Canvas chart cards (equipment, roast, time-of-day, etc.)
│       │   ├── AchievementsSection.kt  9 unlockable badges
│       │   ├── BeanListScreen.kt     Bean inventory list
│       │   └── BeanDetailScreen.kt   Single bean detail with stats
│       ├── learn/
│       │   ├── LearnScreen.kt        14-chapter curriculum, daily lesson, quiz, pro tips,
│       │   │                           glossary, flavor wheel, food pairing, culture facts
│       │   ├── LearnContent.kt       85 cards + chapter roster + brew troubleshooter
│       │   ├── LearnTools.kt         Extraction calculator, SCA control chart, water guide
│       │   └── LearnDetailScreen.kt  Full lesson card detail view
│       ├── onboarding/
│       │   └── OnboardingScreen.kt   3-step interactive welcome flow
│       └── root/
│           ├── RootScreen.kt         Scaffold with bottom nav bar
│           ├── Navigation.kt         5 nav tabs + route sealed interface
│           ├── AppViewModel.kt       Global state holder (settings, cloud, data)
│           └── SettingsScreen.kt     Themes, language, timer, data, cloud, about
└── util/
    ├── BrewMath.kt            Recipe engine (ratio calculation, strength-to-ratio, temp offset)
    ├── Format.kt              Numeric/time formatters
    ├── BrewPdfExporter.kt     A4 PDF brew report generation
    └── CloudBackupManager.kt  Google Sign-in + Drive backup/restore (appDataFolder)
```

### Database Schema

| Table | Purpose |
|---|---|
| `recipes` | Saved brew recipes (name, equipment, strength, roast, ratio, cups) |
| `settings` | Single-row app preferences (theme, palette, language, timer, notifications) |
| `brew_logs` | Immutable brew history (equipment, ratio, dose, water, grind, temp, rating, notes, photo, bean) |
| `beans` | Coffee bean inventory (origin, roaster, roast date, process, varietal, altitude, SCA score) |
| `custom_equipment` | User-created brewing gear (category, ratio, temp, grind, cup size) |

### APK Size

| Build Type | Size | Notes |
|---|---|---|
| Debug | ~13 MB | Unminified, fixed debug keystore |
| Release | ~4.7 MB | R8 minification + resource shrinking + ProGuard |

---

## Design System

Coffeery uses a fully custom design system — no Material Design, no third-party component library. Every color, shape, motion value, and texture is defined in `ui/theme/`.

### CoffeeColors — 8 Palettes × 2 Modes

Each palette defines 15 semantic color slots including background gradient stop, surface elevation, outline, text primary/secondary, accent/soft/vintage, crema light/dark for the strength slider, and an `isDark` flag.

| Palette | Character |
|---|---|
| **Terracotta** | Warm orange-red accent on cream — the default, classic Coffeery look |
| **Espresso** | Deep brown on warm beige, inspired by dark roast |
| **Matcha** | Forest green accent on cool sage background |
| **Berry** | Plum-magenta on soft lavender-rose |
| **Crema** | Golden caramel on warm cream |
| **Mocha** | Rich chocolate brown on latte beige |
| **Caramel** | Amber-orange on warm buttercream |
| **Hazelnut** | Warm nutty brown on oatmeal |

Each is defined for both light and dark modes, yielding 16 distinct `CoffeeColors` instances. All colors are **warm-tinted** — backgrounds use cream/paper tones instead of pure white, and dark backgrounds use deep warm charcoal instead of pure black.

Helper methods:
- `coffeeFor(strength: Float)` — lerps between `cremaLight` and `cremaDark` to color the strength slider fill as strength increases
- `coffeeTextFor(strength: Float)` — ensures text contrast on the strength slider at any fill level

### CoffeeTypography

Two font families driving an 8-style editorial type scale:

| Style | Font | Weight | Size | Usage |
|---|---|---|---|---|
| `display` | **Fraunces** (serif) | Bold | 36sp / 40sp | Screen titles, hero numbers |
| `title` | Fraunces (serif) | Bold | 20sp / 26sp | Section headers |
| `headline` | **Manrope** (sans) | SemiBold | 16sp / 22sp | Card titles, item headers |
| `body` | Manrope (sans) | Normal | 14sp / 20sp | Paragraph text |
| `bodyStrong` | Manrope (sans) | SemiBold | 14sp / 20sp | Emphasised body |
| `label` | Manrope (sans) | Medium | 11sp / 15sp | Chips, tags, overlines (tracking 0.5) |
| `caption` | Manrope (sans) | Normal | 11sp / 15sp | Secondary labels |
| `number` | Fraunces (serif) | Bold | 30sp / 32sp | Recipe read-outs (tracking -0.8) |

Fraunces and Manrope are bundled as `.otf` files in `res/font/` so the typeface renders identically on all devices.

### CoffeeShapes

| Token | Radius |
|---|---|
| `small` | 10dp |
| `medium` | 18dp |
| `large` | 26dp |
| `pill` | 50% (fully rounded) |

Spacing scale (via `CoffeeSpacing`): `4 | 8 | 12 | 16 | 24 | 32 dp`.

### CoffeeMotion

Four spring specifications tuned for different interaction types:

| Spring | Damping | Stiffness | Purpose |
|---|---|---|---|
| `press` | MediumBouncy | High | Button press/focus animations |
| `cardExpand` | LowBouncy | MediumLow | Expanding cards, dialogs |
| `page` | NoBouncy | Medium | Page transitions |
| `counter` | MediumBouncy | Medium | Timer digit transitions |

Duration constants: `quick` (120ms), `normal` (220ms), `slow` (420ms). Easing curves: `standard` (fast-out-slow-in) and `emphasized` (exaggerated deceleration).

### Grain Texture

A subtle seeded-random noise overlay rendered via `Modifier.coffeeBackground()`. It draws 200–800 semi-transparent black circles (alpha ~0.02–0.06) with a fixed seed (42) so the pattern is deterministic and does not shimmer between recompositions. This adds a tactile, paper-like quality to all backgrounds.

### Vintage Accent Colors

Every palette includes `accentVintage` — a slightly darker, desaturated variant of the main accent. This is used for secondary accents, dividers, and "aged" touches that reinforce the artisanal coffee-shop feel.

### Custom Components

All UI primitives are built from scratch on Compose Foundation:
- `CoffeeCard` — elevated surface container with themed border
- `AppText` / `AppTextField` — themed text and input fields
- `PrimaryButton` / `SecondaryButton` — two action button variants
- `SegmentedControl` — 2–5 segment pill picker
- `Chip` — toggleable filter chips
- `StrengthSlider` — gradient-filled coffee-strength control
- `LineIcon` / `Glyph` — 100+ hand-drawn line-art icons (64 equipment silhouettes + 40+ system glyphs)
- `CoffeeDialog` — custom modal dialog sheet
- `ScreenHeader` — reusable top-bar header component

---

## Features by Screen

### Brew (Calculator)

The heart of the app. A **3-field ratio engine** where any two inputs calculate the third:

- **Auto mode:** Adjust coffee dose and strength slider → water and ratio are computed automatically
- **Manual mode:** Enter any two of {coffee, water, ratio} → the third field is calculated on the fly
- **Strength slider:** Drives ratio based on equipment defaults; the slider track fills with a coffee-colored gradient that darkens as strength increases
- **Roast offset:** Select light/medium/dark — applies temperature correction (+2°C / 0 / −3°C) and grind shift (−1 / 0 / +1 steps)
- **Quick stats cards:** Target water temperature, recommended grind size, brew time estimate, cup count
- **Ratio mode toggle:** Switch between auto (strength slider) and manual (direct ratio input)
- **YouTube links:** Each method has verified tutorial video links in English and Turkish (72 total)
- **Start Brew button:** Passes the configured recipe to the timer screen

### Brew Timer

The timer screen is a full-screen immersive experience:

- **Drift-free timing:** Uses `System.currentTimeMillis()` for absolute time, never accumulates rounding errors
- **Hero countdown:** Large Fraunces 72sp display in the centre of a 260dp Canvas progress ring
- **Per-method step routines:** Each equipment definition has a sequence of `BrewStepDef` (rinse, bloom, pour, swirl, steep, plunge, drawdown, etc.) with per-step durations and water targets
- **Per-pour water adjustment:** ±5% water volume per pour phase
- **Merge-pours mode:** Combine consecutive pour steps for simpler recipes
- **Custom step durations:** Configurable bloom/pour/steep/drawdown times in Settings
- **Auto-advance toggle:** Timer can automatically move to the next step
- **Sound + vibration:** Step-change dual-tone chime, brew-complete melody, haptic feedback
- **Background service:** Foreground notification keeps the timer running when minimised
- **Hands-free proximity mode:** When enabled, the device's proximity sensor dims the screen to save battery
- **Save to log:** Complete brew save dialog with rating, tasting notes, grind size, bean picker, and photo capture

### Gear (Equipment)

- **36 brewing methods** — 24 standard devices (V60, Chemex, Kalita, French Press, AeroPress, Moka Pot, Cezve/Ibrik, Cold Brew, Siphon, Espresso, Clever, Hario Switch, Origami, April, Stagg X, Timemore B75, Beehouse, Cafec Flower, Phin, Cold Drip, Percolator, Batch Brewer, Napoletana) + 12 equipment-free methods (Cowboy Coffee, Cupping, Cloth Filter, Sock Coffee, Decoction, Paper Towel, Swedish Egg Coffee, Improvised Turkish, Kopi Tubruk, Arabic Qahwa, Cafe de Olla, Mason Jar Cold Brew)
- **Hand-drawn Canvas icons:** 64 unique line-art silhouettes rendered procedurally — no PNGs, perfectly crisp at all densities
- **Category tabs:** Pour-Over, Immersion, Pressure, Other
- **Real-time search:** Text filter across equipment names and tags
- **Quick access:** Long-press or tap-star to pin favourites to the top
- **Custom gear:** Add your own brewer — choose a category for sensible defaults (ratio range, temp, grind, cup size), steps are auto-generated from the category template, and pick from 25 icon shapes

### Recipes

- **Save recipes:** One-tap save from the calculator with a custom name
- **Load into calculator:** Tap any saved recipe to populate the calculator fields instantly
- **Edit/delete:** Long-press for context actions
- **Featured recent:** Top row shows the most recently used recipe for quick restart
- **Drinks sub-tab:** 70+ coffee drink recipes (milk-based: Latte, Cappuccino, Flat White, Cortado, Macchiato, Mocha, Affogato, Americano, Red Eye, Espresso Con Panna, Freddo Espresso, Pumpkin Spice Latte; regional: Cà Phê Trứng, Cà Phê Sữa Đá, Greek Frappé, Irish Coffee, Café Bombón, Cuban Cortadito, Viennese, Eiskaffee, Kopi Joss, Espresso Yen, Mirra, Mazagran) with detailed ingredient lists and numbered steps
- **Empty state CTA:** When no recipes are saved, a contextual empty state prompts recipe creation

### Log

The most feature-rich screen — a complete coffee journal:

- **Heatmap calendar:** 12-week GitHub-style contribution heatmap showing brew frequency with colour intensity
- **Streak banner:** Consecutive brewing day counter with motivational messaging
- **Brew journal timeline:** Chronological list of every brew, each expandable to show full details
- **Stats dashboard:** 6 Canvas-rendered chart cards:
  - Total brews & weekly average
  - Equipment breakdown (horizontal bar chart)
  - Roast preference distribution
  - Average rating gauge
  - Time-of-day heatmap
  - Favourite ratio card
- **Achievements:** 9 unlockable badges — First Brew, 7-Day Streak, 30-Day Streak, Gear Master (10+ brewers), Bean Explorer (5+ beans), Perfect Score (5-star brew), Roast Explorer (all 3 roasts), Early Bird (5+ pre-7am brews), Scholar (10+ chapters completed). Locked badges show as "???" with a subdued description.
- **Brew comparison:** Select any two brews for a side-by-side diff view showing differences in equipment, ratio, grind, temp, time, and rating
- **PDF export:** Generate an A4 brew report with equipment, dose, water, ratio, grind, temp, time, rating, tasting notes, and bean info — shareable via the system share sheet
- **Share card:** Text-based brew summary for social sharing
- **Bean inventory:** Track coffee beans with origin, roaster, roast date, process method, varietal, altitude, flavour notes, and SCA score; freshness counter relative to roast date
- **CSV export:** Full brew log export as comma-separated values
- **Photo capture:** Attach a photo to any brew log via the save dialog

### Learn

The educational hub of the app, structured as a curriculum:

- **14-chapter curriculum:**
  1. Coffee Basics
  2. Grinding
  3. Water Chemistry
  4. Extraction Theory
  5. Brewing Methods
  6. Milk & Drinks
  7. Tasting & Sensory
  8. Caffeine
  9. Equipment Deep-Dive
  10. Advanced Water Chemistry
  11. Sustainability & Ethics
  12. Espresso Fundamentals
  13. Coffee Processing
  14. Coffee Origins
- **85 knowledge cards:** Multi-paragraph lessons with chapter headers; cards are grouped by chapter and displayed in a locked/unlocked step-map
- **Daily lesson:** A rotating "lesson of the day" shown first for bite-sized learning
- **Real-time search:** Filter across all card titles and body text
- **Interactive tools:**
  - **Extraction Calculator:** Input coffee dose, water volume, and TDS reading → computes extraction yield with colour-coded feedback (green = ideal 18–22%, amber = borderline, red = out of range)
  - **SCA Brew Control Chart:** Canvas-rendered chart with extraction % on X-axis, TDS % on Y-axis, ideal zone rectangle highlighted, and a dot showing the current brew's position
  - **Water Chemistry Guide:** Reference table showing ideal ranges for TDS (75–250 ppm), hardness (50–175 ppm), alkalinity (40–70 ppm), and pH (6.5–7.5) with target values
- **12-question quiz:** Random question drawn from a pool; immediate correct/incorrect feedback; score tracker for the session
- **50 pro tips:** Rotating quick tips covering technique, equipment, and troubleshooting
- **65-term glossary:** Searchable coffee terminology dictionary
- **37-note flavour wheel:** Fruity, Berry, Nutty, Spicy, Bright, Smooth, Bold, Citrus, Stone Fruit, Tropical, Floral, Jasmine, Rose, Chamomile, Lavender, Sweet, Chocolate, Caramel, Honey, Brown Sugar, Nutty/Spice, Almond, Cinnamon, Clove, Nutmeg, Earthy, Woody, Tobacco, Leather, Mushroom, Tropical Fruit, Red Berry, Hazelnut, Milk Chocolate, Dark Cocoa, Caramelized, Maple
- **10 brew issues (troubleshooter):** "How was your cup?" interactive flow — select sour/bitter/astringent/weak/strong/balanced/dry/hollow/ferment/baggy for corrective brewing advice
- **5 food pairings:** Ethiopian Yirgacheffe + Lemon Blueberry Scone, Sumatra Dark Roast + Dark Chocolate Brownie, Kenya AA + Lemon Bar, Guatemala Medium Roast + Cinnamon Coffee Cake, Espresso + Vanilla Ice Cream (Affogato)
- **4 culture facts:** Turkish coffee fortune telling (tasseography), Greek frappé accident of 1957, Turkish coffee matrimonial salt custom, cappuccino's Capuchin friar origin

### Settings

Organised into logical sections:

- **Appearance:** 8 palette swatch preview cards with live preview; Dark/Light/System theme toggle
- **Language:** English / Turkish switch (applied immediately)
- **Timer Settings:**
  - Custom step durations: bloom (default 40s), pour (45s), steep (240s), drawdown (55s)
  - Auto-advance toggle
  - Timer display mode: countdown / elapsed
  - Sound on/off
  - Vibration on/off
  - Show next step preview
  - Merge consecutive pours
  - Hands-free proximity mode
- **My Data:**
  - Export all data as JSON (recipes, brew logs, beans, custom equipment, settings)
  - Import from JSON file
  - CSV export (brew logs only)
  - Clear all data with confirmation
- **Cloud Backup (Google):**
  - Sign in with Google (profile photo appears in app header)
  - Backup to Google Drive (appDataFolder — invisible to user, only accessible by Coffeery)
  - Restore from Drive
  - Auto-backup performed on sign-in
- **Notifications:** Brew complete alert toggle, step change alert toggle
- **About:** Version info, app description, MIT license notice

---

## Timer — Technical Details

The brew timer is engineered for precision and reliability:

- **Drift-free time source:** Uses `System.currentTimeMillis()` snapshots rather than accumulating `delay()` calls. The displayed time is always derived from `targetTimestamp - currentTimeMillis()`, making it immune to coroutine scheduling jitter.
- **Foreground service:** `TimerService` runs with a persistent notification (channel `coffeery_timer`, IMPORTANCE_LOW) showing current equipment, step name, remaining time, and step progress (e.g., "Step 3/6"). The service auto-starts when the timer begins and stops when brewing completes or is cancelled.
- **Step progress ring:** A Canvas-drawn circular progress indicator surrounding the central timer display. Each step's elapsed/total duration drives the arc angle; the ring resets between steps.
- **Per-pour water adjustment:** During pour steps, ±5% buttons let the user increase or decrease the water volume for that specific pour. The remaining steps recalculate their water targets proportionally.
- **Merge-pours mode:** When enabled, consecutive pour steps are combined into a single step with summed duration and total water target, reducing complexity for methods with many small pours.
- **Hands-free proximity mode:** Uses the device's proximity sensor (if available). When the user's hand approaches the sensor (such as during a pour), the screen dims to conserve battery. The timer continues running in the foreground service regardless.
- **Custom chimes:**
  - **Step change:** A short dual-tone chime signals the transition to the next step
  - **Brew complete:** A longer melody plays when all steps are finished
- **Haptic feedback:**
  - **Step transition:** Dual-tap pattern — a quick double vibration
  - **Brew complete:** Success pattern — a longer, more elaborate haptic sequence
- **Customisable step durations:** Bloom, pour, steep, and drawdown durations can all be adjusted in Settings. New values apply to the next brew session.
- **Save-to-log dialog:** When the timer finishes (or the user stops early), a dialog appears to save the session with an optional rating (1–5 stars), tasting notes, grind size, bean picker (linking to bean inventory), and photo capture.

---

## Google Cloud Integration

While Coffeery is privacy-first and fully functional offline, optional Google integration enables convenience features:

### Google Sign-In

- Uses `GoogleSignInClient` from Play Services Auth (`com.google.android.gms:play-services-auth:21.3.0`)
- **Silent sign-in:** Attempted on Settings screen open; if a previous session exists, re-authentication happens transparently
- **Interactive sign-in:** Falls back to the standard Google account picker if silent sign-in fails
- **Profile photo:** The signed-in user's Google profile photo is displayed in the app header (RootScreen) using Coil image loading
- **Sign-out:** Clears the credential state and removes the profile photo

### Google Drive Backup/Restore

- Uses the Drive v3 API via `com.google.apis:google-api-services-drive:v3-rev20240521`
- **appDataFolder:** Backups are stored in the Drive appDataFolder — a hidden, per-app storage area that the user cannot see in their Drive file listing. This prevents accidental deletion and keeps the backup invisible to other apps.
- **Backup format:** A JSON file containing all Room data (recipes, brew logs, beans, custom equipment, settings) serialised by `CoffeeRepository.exportAllToJson()`
- **Auto-backup on sign-in:** The first action after successful Google sign-in is a full backup upload
- **Restore:** Downloads the latest backup file from appDataFolder and deserialises it into Room via `CoffeeRepository.importFromJson()`
- **Error handling:** Toast messages for Play Services unavailability, network errors, and auth failures

---

## Content Stats

| Category | Count |
|---|---|
| Kotlin source files | 53 |
| Kotlin lines of code | ~10,000 |
| XML resource files | 11 (strings × 2 locales, themes, colors, splash, drawable, file paths) |
| XML lines | ~2,750 |
| JSON data lines | 1,667 |
| String resources per locale | 1,226 (EN = TR, full parity) |
| Total string resources | 2,452 |
| Built-in equipment presets | 36 |
| Equipment icons (Canvas) | 64 |
| System glyphs (Glyph enum) | 40+ |
| Learn chapters | 14 |
| Learn cards (lessons) | 85 |
| Quiz questions | 12 |
| Pro tips | 50 |
| Glossary terms | 65 |
| Flavour wheel notes | 37 |
| Brew issue remedies | 10 |
| Drink recipes | 70+ |
| Coffee variety profiles | 22 |
| Food pairings | 5 |
| Culture facts | 4 |
| Achievements | 9 |
| Stats chart types | 6 |
| Colour palettes | 8 (× 2 modes = 16 profiles) |
| Fonts | 2 (Fraunces, Manrope) |
| Spring animation specs | 4 |
| APK (debug) | ~13 MB |
| APK (release) | ~4.7 MB |

---

## Build & Release

### GitHub Actions CI/CD

Automated builds run on **every push** to any branch via `.github/workflows/build.yml`:

1. **Checkout** the repository
2. **Set up JDK 17** (Eclipse Temurin distribution)
3. **Set up Gradle 8.13** using `gradle/actions/setup-gradle@v4` — generates a clean wrapper to avoid checksum issues
4. **Build debug APK:** `./gradlew assembleDebug --stacktrace --no-daemon`
5. **Build release APK:** `./gradlew assembleRelease --stacktrace --no-daemon`
6. **Upload artifacts:**
   - `coffeery-debug-apk` — debug APK
   - `coffeery-release-apk` — minified, resource-shrunk release APK
   - `coffeery-release-mapping` — ProGuard mapping file for deobfuscation

### Signing

- **Debug:** Fixed `debug.keystore` (password: `android`, alias: `androiddebugkey`) — ensures consistent SHA-1 fingerprint across CI and local builds, necessary for Google Sign-In to work in debug
- **Release:** `coffeery.keystore` in the app module root

### ProGuard & Minification

Release builds apply:
- `isMinifyEnabled = true` (R8 full mode)
- `isShrinkResources = true`
- Default ProGuard rules: `proguard-android-optimize.txt`
- Custom rules: `proguard-rules.pro` (41 lines) — keeps Room entities, Google API client classes, and Compose types

### Local Build

```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK (requires keystore)
```

---

## Roadmap / Future

Potential additions under consideration:

- **BLE scale integration:** Connect Bluetooth scales (Acaia, Timemore, Felicita) to auto-log water weight during pours and display real-time flow-rate graphs
- **Coffee shop finder:** Map-based discovery of specialty coffee shops using OpenStreetMap data or a user-contributed directory
- **Wear OS companion:** Timer control and step notifications on Wear OS smartwatches via the Horologist library
- **Home screen widget:** Quick-start widget for favourite recipes and a "brew streak" counter
- **Community recipe sharing:** Opt-in recipe sharing via short codes or QR codes (no account required — code is the data)
- **TDS meter integration:** Bluetooth TDS meters for automatic extraction yield calculation fed directly into the SCA control chart
- **Robusta & Liberica profiles:** Expand the coffee variety database to include lesser-known species with their unique processing and flavour profiles
- **Brew log search & filtering:** Advanced query by date range, equipment, rating, bean, or flavour tags
- **Notification reminders:** Customisable "brew time" reminders (e.g., daily at 8 AM for your morning pour-over)

---

*Coffeery is developed and maintained as an open-source project under the MIT license. Contributions, bug reports, and feature requests are welcome.*
