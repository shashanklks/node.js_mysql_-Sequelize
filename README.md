# Khatabook clone

A Khatabook style credit ledger: an Android app in Kotlin and Jetpack Compose,
backed by the Node.js + MySQL + Sequelize API in this repository.

```
.
├── src/        Express API (auth, parties, ledger entries, reports)
├── android/    Android app (Kotlin, Jetpack Compose, Retrofit)
├── test/       Unit tests for the ledger maths
└── docs/API.md Endpoint reference
```

## The ledger rule

Every screen follows one convention:

```
balance = SUM(you gave) - SUM(you got)

balance > 0  ->  "You will get"   (green)
balance < 0  ->  "You will give"  (red)
balance = 0  ->  settled
```

`YOU GAVE` is credit handed out (goods sold on udhaar, cash lent). `YOU GOT` is
money received. A supplier book works the same way with the signs reversed
naturally, so no separate maths is needed for the second tab.

## User flow

```
Splash → Language → Mobile number → OTP → Name & business
   → Home (Customers / Suppliers tabs, totals, search)
       → Add customer/supplier → Ledger
       → Ledger (running balance, entry history)
            → YOU GAVE ₹ / YOU GOT ₹ → amount, note, date → Save
   → Reports (cashbook, date ranges, party filter)
   → Profile & settings (name, business, API server, logout)
```

## Running the API

Requires MySQL running locally.

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS khatabook;"
npm install
npm run dev          # nodemon on the port from .env (3002 by default)
npm test             # ledger maths unit tests
```

Configuration lives in `.env`:

| Variable | Meaning |
| --- | --- |
| `PORT` | HTTP port (default 3002) |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | MySQL connection |
| `DB_ALTER` | `true` lets Sequelize alter tables on boot during development |
| `JWT_SECRET` / `JWT_EXPIRES_IN` | Token signing |
| `OTP_TTL_MINUTES` | How long a login code stays valid |

Tables are created by `sequelize.sync()` on start, so no migration step is
needed for a fresh database.

### OTP in development

There is no SMS gateway. Outside `NODE_ENV=production` the generated code is
returned in the `send-otp` response and printed to the server log, and the app
pre-fills it. Wire a real provider into `sendOtp` in
`src/controllers/authController.js` before shipping.

## Running the app

```bash
cd android
./gradlew assembleDebug      # or open the android/ folder in Android Studio
./gradlew installDebug       # with an emulator or device attached
./gradlew testDebugUnitTest  # keypad input rules
```

The app talks to `http://10.0.2.2:3002/` by default, which is the host machine
as seen from the Android emulator. On a physical phone, open **Profile &
settings → API server** and enter your machine's LAN address, for example
`http://192.168.1.5:3002/`. Cleartext HTTP is allowed so a local development
server works without TLS.

Requirements: Android Studio Koala or newer, JDK 17, compileSdk 34, minSdk 24.

## How the app is put together

| Layer | Where |
| --- | --- |
| Design tokens | `ui/theme/` — `Palette.kt` holds the semantic colours, `Color.kt` the raw values |
| Shared components | `ui/common/` — keypad, chart, money text, cards, segmented tabs |
| Screens (Compose) | `android/app/src/main/java/com/khatabook/clone/ui/` |
| ViewModels | one per screen, alongside the screen, state held in `mutableStateOf` |
| Navigation | `navigation/NavGraph.kt`, one `NavHost` with typed routes |
| Networking | `data/remote/` — Retrofit + Gson, one envelope type for every response |
| Storage | `data/local/SessionStore.kt` — DataStore for token, language, API host |
| Wiring | `ServiceLocator` in `KhatabookApp.kt`, no DI framework |

### Theming

Screens never name a colour directly. They read `KhataTheme.colors`, a
`KhataPalette` provided through a `CompositionLocal`, which is what lets the
whole app switch to dark without a single screen changing:

```kotlin
val palette = KhataTheme.colors
Text(text = rupees(balance), color = palette.money(balance))
```

`palette.money(amount)` is the one place the green/red/settled rule lives, so
the convention cannot drift between screens.

### Interaction decisions worth knowing

- **The amount screen carries its own keypad** (`ui/common/AmountKeypad.kt`)
  instead of the system IME: bigger targets, no layout jump, and +50/+100/
  +500/+1000 shortcuts. Its input rules are pure functions with unit tests.
- **Date is two taps at most** — Today and Yesterday are chips, the picker is
  the fallback, and the picker will not accept a future date.
- **Every party row has a one-tap "got paid" button**, because collecting a
  payment is the most common thing that happens at a counter.
- **Ledger entries group under sticky date headers**, and the balance lives in
  the header rather than a card below it.
- **Reports draw a paired daily bar chart** on a Canvas; days with no trading
  still draw a faint tick so a gap reads as "nothing happened", not missing
  data.

The API base URL and auth token are read through `ApiClient`, so changing the
server in settings or logging out takes effect without restarting the app.

See `docs/API.md` for the endpoint reference.
