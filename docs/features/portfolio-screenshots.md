# Portfolio screenshots

`PortfolioScreenshotTest` is an instrumented suite whose only job is to produce clean, repeatable
screenshots of the app's key screens, in light and dark, for the portfolio. It asserts nothing
beyond "the flow could be walked". It needs a device or emulator, but **no backend**: it never
talks to Supabase and does not read the Supabase secrets in `local.properties`.

## Running it

```bash
./gradlew :app:connectedStagingDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.restrusher.ecomercecarlosv.screenshots.PortfolioScreenshotTest

# a single flow
./gradlew :app:connectedStagingDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.restrusher.ecomercecarlosv.screenshots.PortfolioScreenshotTest#cliente_y_pedidos
```

The PNGs end up in **`screenshots/` at the project root**, flat:

```
screenshots/
  light_00_login.png   dark_00_login.png
  light_01_mercados.png …
```

AGP first pulls them from the device into
`app/build/outputs/connected_android_test_additional_output/stagingDebugAndroidTest/connected/<device>/`.
The `copyPortfolioScreenshots` task in `app/build.gradle.kts` then copies them to `screenshots/`.
Every `connected*AndroidTest` task is finalized by it, so it runs after each device run, including
failed ones. It can also be run on its own: `./gradlew :app:copyPortfolioScreenshots`.

- Files are overwritten, never deleted: running one flow refreshes only that flow's shots. Delete old
  `*_FAILED_*.png` files by hand.
- Runs on different devices write to the same file names, so the last run wins.
- `screenshots/` is git-ignored and survives `./gradlew clean`.

- **Device:** API 29+ (the night-mode switch uses `cmd uimode night`). Use a phone-sized emulator
  in portrait. Its resolution is the screenshots' resolution.
- **Clean status bar:** SystemUI demo mode is switched on for the run (12:00, full battery and
  signal, no notification icons) and off again afterwards. The device's original night mode is
  restored too.
- **Installed app:** like every `connected*` task, AGP installs the debug build over whatever
  `.staging` build is on the device and uninstalls it at the end. Your own install's data is gone
  after a run, but the suite itself never reads or writes it: the database is in memory.

## The shots

| File | Screen | How the test gets there |
|---|---|---|
| `00_login` | Login (password form) | Signed out |
| `01_mercados` | Home · Mercados tab | Start |
| `02_catalogo` | Home · Productos tab | Bottom bar |
| `03_reporte` | Home · Reporte tab | Bottom bar |
| `04_detalle_mercado` | Detalle de mercado | Long-press *Mercado Rodríguez* → *Ver detalles* |
| `05_clientes` | Clientes of a mercado | Tap *Mercado Rodríguez* |
| `06_detalle_cliente` | Detalle de cliente | Tap *Rosa Quispe* |
| `07_detalle_pedido` | Detalle de pedido | Tap her partially paid pedido (2 days ago) |
| `08_nuevo_pedido` | Creación de pedido | *Mercado Rodríguez* → *Rosa Quispe* → *Nuevo Pedido* → tap two productos |
| `09_busqueda` | Búsqueda | Search icon → type "an" |
| `10_lista_negra` | Lista negra | *Lista Negra* button under the mercados |
| `11_perfil` | Perfil | Avatar "CV" |
| `12_sincronizacion` | Sincronización with 3 pending writes | Cloud icon |

Every test launches a fresh `MainActivity` twice: once with `ThemeMode.LIGHT` and system night
mode off, then with `ThemeMode.DARK` and night mode on. Both are set because `ClienteRow` reads
`isSystemInDarkTheme()` directly instead of the app's theme. After switching night mode the suite
waits until the app process has the new configuration, so the activity isn't recreated mid-flow.

Flows only move forward (no Back presses): each starts from Home and taps its way to its screens,
so `nuevo_pedido` is its own test rather than a Back step at the end of `cliente_y_pedidos`.

**When a flow fails,** it saves `<theme>_FAILED_<test>.png` next to the other shots before the
error is reported, showing the screen it was stuck on.

## How it avoids the backend

The suite runs under `HiltTestRunner`, which makes `HiltTestApplication` the application. That
skips `PedidosApp.onCreate`, so DataSynchronizer, QueueProcessor and SyncWorker never start. The
runner installs a test WorkManager instead. Four production modules are replaced with
`@TestInstallIn`:

| Production module | Replacement | Effect |
|---|---|---|
| `RepositoryModule` | `TestRepositoryModule` | Same real repositories; `SessionManager` → `ScreenshotSessionManager` |
| `NetworkModule` | `TestNetworkModule` | `ScreenshotNetworkMonitor` is always online, so no offline UI shows |
| `DatabaseModule` | `TestDatabaseModule` | In-memory Room, seeded per test by `DemoDataSeeder` |
| `SupabaseModule` | `TestSupabaseModule` | Client pointed at `https://screenshots.invalid` with an in-memory session |

`ScreenshotSessionManager` reports the startup check as done, holding the demo user, so
`AppNavigation` goes straight from Login to Home. It answers `ensureValidSession()` with `OFFLINE`,
so every `DataSynchronizer.triggerSyncIfStale` returns silently: no request and no error snackbar.

`RepositoryModule` binds the session manager alongside all the repositories, so
`TestRepositoryModule` rebinds them all. **When a binding is added to `RepositoryModule`, add it to
`TestRepositoryModule` too**, or the androidTest build fails with a missing binding.

Screens left out on purpose: `GestionUsuarios` and `Depuracion` start a remote call from their
ViewModel's `init`. Against the `.invalid` host it fails fast, but the spinner would show in the shot.

## Demo data

All under `screenshots/data/`, with `photoUrl = null` everywhere so Coil never fetches anything:

- `DemoCatalog` — the signed-in SUPERUSUARIO *Carlos Vargas*, three more users (for Perfil's
  team count), umbrales (Bs. 500 / 30 days), 4 mercados and 8 productos.
- `DemoClientes` — 11 active clientes over the 4 mercados and 2 blacklisted ones.
- `DemoPedidos` — pedidos shaped so the Clientes list shows all three statuses: Rosa is
  ADVERTENCIA (recent partial payment), Juan is CRITICO by age (45 days), Teresa is CRITICO by
  amount (above Bs. 500). Rosa also has paid and pending pedidos and a saldo extra.
- Dates are relative to `DemoClock.now`, one instant per run, so the date a test looks for on
  screen always matches the date that was seeded.

## Changing the shots

The flows tap visible text and content descriptions through `util/ScreenRobot`; there are no
`testTag`s. Taps resolve labels through `R.string`, so renaming a string doesn't break a flow. A
change to the demo names does, because the flows look those up as text. To add a shot, add a step to
an existing flow or a new `@Test` using `signedIn { … capture("13_name") }`. Keep the numeric prefix
so the files sort in presentation order.

Three buttons had no accessible label and gained one, which TalkBack needed anyway and which gives
the flows something to tap:

- the search and sync icons on the Mercados top bar (`R.string.common_buscar`, `R.string.sinc_title`);
- the *Nuevo Pedido* FAB on Detalle de cliente (`R.string.pedidos_nuevo` on its icon).

**Extended FABs can't be found by their text.** Material3 1.4's `ExtendedFloatingActionButton`
wraps the text in `clearAndSetSemantics {}` and expects the icon's `contentDescription` to label the
button. A flow taps one with `tapDescription(...)`, and the icon needs a description. The FABs on
Clientes, Mercados, Catálogo and Gestión de usuarios still pass `contentDescription = null`.
