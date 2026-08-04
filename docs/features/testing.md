# Testing

Until Phase 17 the repo had no test coverage — only the two Android Studio templates. This document
describes the strategy, the conventions, and what is covered today.

Two kinds of tests, and nothing else:

- **Unit tests** (`app/src/test/`) — pure logic and use cases, run on the JVM. No emulator.
- **Integration tests** — Room DAOs and repository implementations against an in-memory database,
  also under `app/src/test/` via Robolectric so they stay in the same Gradle task. Only Room
  **migration** tests go to `app/src/androidTest/`, because `MigrationTestHelper` loads the exported
  schemas from the test APK's assets and needs real SQLite file upgrades.

---

## Running them

The `staging` flavor is `isDefault`, so there is no `testDebugUnitTest` task. The everyday command is:

```bash
./gradlew :app:testStagingDebugUnitTest
```

```bash
# a single class, or a single method, while iterating
./gradlew :app:testStagingDebugUnitTest --tests "*MapsUrlParserTest"
./gradlew :app:testStagingDebugUnitTest --tests "*ClienteMapperTest.fromDto*"

# fast compile-only feedback
./gradlew :app:compileStagingDebugUnitTestKotlin

# both flavors — pre-merge only; they differ solely in applicationIdSuffix and two BuildConfig fields
./gradlew :app:testStagingDebugUnitTest :app:testProductionDebugUnitTest
```

HTML report: `app/build/reports/tests/testStagingDebugUnitTest/index.html`.

The migration suite is instrumented and needs a **device or emulator**:

```bash
./gradlew :app:connectedStagingDebugAndroidTest

# one class while iterating
./gradlew :app:connectedStagingDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.restrusher.ecomercecarlosv.data.local.MigrationTest
```

Report: `app/build/reports/androidTests/connected/debug/flavors/staging/index.html`. It does **not**
run in CI — there is no `connectedCheck` job — so it is a pre-merge step you run by hand whenever
`AppDatabase.kt` changes.

---

## Layout

```
app/src/test/java/com/restrusher/ecomercecarlosv/
  fixtures/          test-data builders, shared by every suite
    PedidoFixtures.kt    pedido(), detallePedido()          — domain models
    UserFixtures.kt      appUser(), cartItem()
    EntityFixtures.kt    clienteEntity(), pedidoEntity(), … — Room entities
    DtoFixtures.kt       clienteDto(), pedidoDto(), …       — Supabase DTOs
  support/
    MainDispatcherRule.kt         swaps Dispatchers.Main for a TestDispatcher
    RoomTestDatabase.kt           in-memory AppDatabase + foreign-key seeding
  fakes/             hand-written test doubles
    FakePedidoRepository.kt       records created pedidos and registered pagos
    FakeUserRepository.kt         biometric enrolment + syncFromRemote
    FakeSessionManager.kt         scripted SessionResult, records verifiedUserId
    FakeDeviceDataCleaner.kt      pending write count, wipe call counts
    FakeRefreshRepositories.kt    Cliente + Mercado, scripted refresh(), blacklist log
    FakeUmbralesRepository.kt     configurable thresholds
    FakeSyncOperationDao.kt       in-memory queue table
  data/mapper/       one test file per mapper
  domain/model/      computed-property tests
  domain/usecase/    one test file per use case
  domain/util/
  ui/screen/<feature>/

app/src/androidTest/java/com/restrusher/ecomercecarlosv/
  data/local/
    MigrationTest.kt         the 4→19 sweep + the ALL_MIGRATIONS gapless guard
    MigrationDataTest.kt     the two migrations that move data, not just schema
  ExampleInstrumentedTest.kt
```

**Fakes record, they don't assert.** Each exposes plain counters and lists (`refreshCount`,
`created`, `biometricUpdates`) that the test reads afterwards. Scripted answers are plain `var`s
(`refreshResult`, `sessionResult`, `pendingWrites`) set before the call.

Test sources mirror the main source package structure, so a test sits in the same package as its
subject and can reach internal members without ceremony.

---

## Conventions

**One test class per subject**, named `<ClassUnderTest>Test.kt`. The ≤200-line file rule from
`creating-files-or-classes` applies — split by behaviour cluster rather than growing one suite.

**Method names are sentences** in backticks, `subject — condition — expected outcome`, with Spanish
domain vocabulary preserved where the production code uses it:

```kotlin
@Test
fun `pending — overpaid — clamps to zero instead of going negative`() { }

@Test
fun `fromDto — no existing row — local-only fields fall back to their defaults`() { }
```

⚠️ **`androidTest` cannot use those sentence names.** `minSdk = 24` means DEX format < 040, which
rejects spaces in method names — `dexBuilderStagingDebugAndroidTest` fails the *build* with
`Space characters in SimpleName … are not allowed prior to DEX version 040`. Instrumented tests
therefore use `subject_condition_expectedOutcome` instead (`migrate_fromV4_reachesTheCurrentSchema`).
Raising `minSdk` to 30 would lift this; until then it is a hard build-time constraint, not a style
preference.

**Fixtures are default-argument builder functions**, not `Builder` classes. A test overrides only
the fields it cares about, so the assertion reads as the whole scenario:

```kotlin
pedido(total = 100.0, paid = 150.0).pending
```

These deliberately exceed the 3-parameter limit in `creating-methods-or-functions`. That rule targets
production API surface; fixture builders are the standard exception and should not be flagged.

**Fixtures set every optional field to a distinct non-null value.** A mapper round-trip that silently
drops a field then fails, instead of coincidentally matching a default. When a round-trip is expected
to lose a field, the test says so explicitly:

```kotlin
// updatedAt and isDeleted are sync bookkeeping, absent from the domain model.
assertEquals(entity.copy(updatedAt = 0L), result)
```

**Assert on `@StringRes` ints, never on Spanish text.** `AppError` splits the developer-facing
`message` from the user-visible `userMessageRes` (see `docs/features/infrastructure.md`); tests must
bind to the resource id so copy edits don't break them.

**Literal strings in tests are fine.** The `writing-a-string-variable-or-text-in-a-code-file` skill
exempts technical identifiers and test data — fixture names, URLs and field names stay inline.

---

## Tooling

| Concern | Choice | Why |
|---|---|---|
| Assertions | JUnit 4 + `kotlin-test` | `assertEquals(expected, actual, delta)` covers the `Double` math that dominates this codebase. No Truth/AssertK. |
| Test doubles | Hand-written fakes by default | The 7 `domain/repository/` interfaces are small and mostly `Flow`-returning. A fake backed by a `MutableStateFlow` lets a test mutate state and have collectors react; mock stubbing forces a re-stub per transition. Fakes also break as **compile** errors when an interface changes. |
| Mocking | MockK, sparingly | Only where interaction order *is* the rule (`ForgetEnrolledUserUseCase`) or the collaborator is un-fakeable (`QueueProcessor`'s `SupabaseClient`/`Uri`, `SessionManagerImpl`'s supabase-kt `Auth`). |
| Room | Robolectric, in `src/test` | Keeps DAO and repository tests in `testStagingDebugUnitTest`, so CI runs the whole suite on `ubuntu-latest` with no emulator. |
| Room migrations | `room-testing` in `src/androidTest` | The one exception. Every `MigrationTestHelper` constructor takes an `Instrumentation` and reads schemas via `assets`, so it cannot run under Robolectric without shipping the schemas in the app APK. `androidTestImplementation`, plus `androidx.test:runner` and `kotlin-test` which were unit-test-only before. |
| Flows | Turbine | 29 ViewModels expose `StateFlow`; `testIn(backgroundScope)` gives real emission-sequence assertions. |
| Hilt | Not used in tests | Every layer is constructor-injected, so plain constructor calls give the same coverage far faster. KSP already fails the build on a missing binding. |

Dependencies are added at the phase that first needs them, so a broken dependency can never be
blamed on the wrong phase. Phase 1 needed only `junit` + `kotlin-test`; Phase 2 added
`kotlinx-coroutines-test` and `mockk`.

⚠️ **`coroutines = "1.10.1"` in `libs.versions.toml` is not a free choice.** The app declares no
coroutines dependency of its own — it arrives transitively, and several transitive requests
(1.7.3, 1.8.1, 1.9.0) all resolve up to 1.10.1. `kotlinx-coroutines-test` is pinned to that exact
version deliberately. If the two ever diverge, `Dispatchers.setMain` silently no-ops and every
ViewModel test misleads instead of failing. Re-check after any dependency bump:

```bash
./gradlew :app:dependencies --configuration stagingDebugRuntimeClasspath | grep coroutines-core
```

---

## Coverage today

**336 JVM tests across 36 files**, plus **18 instrumented migration tests** in 2 files. The two
numbers stay separate because they run in different Gradle tasks and only the JVM figure could run
without a device. Phases 1–6b complete: pure logic, all use cases, the extracted cliente status rule,
6 ViewModels, Room/repository integration, the sync/queue/session internals, and every schema
migration. Phase 17h then closed the two findings the earlier phases had recorded but not fixed.

### Phase 1 — pure business logic (100 tests)

No coroutines, no Android framework, no test doubles.

| Suite | Tests | Covers |
|---|---|---|
| `domain/util/MapsUrlParserTest` | 14 | All four URL patterns, precedence, lat/lng range guard, boundaries |
| `domain/model/PedidoTest` | 5 | `pending` clamping |
| `domain/model/DetallePedidoTest` | 5 | `subtotal`, `isPriceModified` |
| `ui/screen/perfil/CambiarContrasenaUiStateTest` | 17 | Password rules, confirmation, self-vs-other, `targetFirstName` |
| `ui/screen/lista_negra/AgregarListaNegraUiStateTest` | 10 | `autoAmount`, AUTO/MANUAL `effectiveAmount`, `canConfirm` |
| `data/mapper/*Test` (8 files) | 49 | Entity ↔ domain ↔ DTO round-trips for all 8 mappers |

### Phase 2 — use cases (58 tests)

All 8 `domain/usecase/` classes, driven through fakes.

| Suite | Tests | Covers |
|---|---|---|
| `CreatePedidoUseCaseTest` | 12 | Total from the cart, PAID/PARTIAL/PENDING derivation, `itemCount`, one detalle per line |
| `RegistrarPagoUseCaseTest` | 10 | `coerceAtMost` clamping, status recompute, the `Pago` row that gets written |
| `CreateSaldoExtraUseCaseTest` | 7 | PENDING + `isSaldoExtra`, no line items, caller-supplied date |
| `BiometricLoginUseCaseTest` | 9 | All four `SessionResult` branches, is-active checks, enrolment revocation |
| `ResolveDeviceHandoverUseCaseTest` | 6 | Same/no/different owner, the queued-writes threshold, missing previous user |
| `RefreshDataUseCaseTest` | 9 | Boolean AND aggregation, and that `async` fan-out never short-circuits |
| `ForgetEnrolledUserUseCaseTest` | 5 | The load-bearing call order, via `coVerifyOrder` |

### Phase 3 — cliente status (16 tests)

`computeStatus` + `isOlderThan` were byte-identical private methods in `ClientesViewModel` and
`DetalleClienteViewModel`. Both now delegate to `domain/usecase/CalcularEstadoClienteUseCase`,
tested once in `CalcularEstadoClienteUseCaseTest`.

The use case also absorbs the `statusBalance` filter that both ViewModels computed inline right
before calling, so the whole rule lives in one place. `now` is a parameter defaulting to
`System.currentTimeMillis()` — production callers take the default, tests pass a fixed instant and
assert the day threshold exactly instead of approximately.

Covered: both threshold boundaries (`> montoMaximo` and strictly-older-than `diasMaximos`, so
exactly-at-threshold is ADVERTENCIA), debt summed across pedidos, configurable umbrales changing the
verdict, and the two exclusions that are easy to get wrong — an old **PENDING** pedido and an old
**saldo extra** both leave a client AL_DIA, because only `PARTIAL && !isSaldoExtra` counts.
`docs/features/clientes.md` records a bug that already happened in exactly this filter.

### Phase 4 — ViewModels (57 tests)

6 of the 29, chosen for derived state and `combine` pipelines rather than coverage percentage.
`MercadosViewModelTest` arrived later, in Phase 17h, alongside the status-rule unification.

| Suite | Tests | Covers |
|---|---|---|
| `ClientesViewModelTest` | 14 | Search, all four sort modes, the balance rule, `canWrite`, refresh success/failure |
| `DetalleClienteViewModelTest` | 11 | Regular-vs-extra balance split, status filters, the unblacklist sheet, liquidation saldo extra |
| `AgregarListaNegraViewModelTest` | 8 | AUTO-on-first-load default, mode surviving later emissions, confirm in both modes |
| `CambiarContrasenaViewModelTest` | 8 | Profile load (self vs other), and that an invalid form never reaches the admin service |
| `SincronizacionViewModelTest` | 7 | `SyncIconState` derivation, queue item mapping, retry |
| `MercadosViewModelTest` | 9 | The unified dot rule: both thresholds honouring `Umbrales`, the old-PENDING and old-saldo-extra exclusions the previous rule got wrong, blacklisted clients excluded from the count and the dot |

**Robolectric arrived a phase early.** 13 of the 29 ViewModels read their arguments through
`savedStateHandle.toRoute<SomeRoute>()`, and that does not work on a plain JVM test: it returns a
route with **null** fields rather than throwing, because `isReturnDefaultValues = true` stubs the
underlying `Bundle`. Any such ViewModel therefore needs `@RunWith(RobolectricTestRunner::class)`.
`SincronizacionViewModelTest` takes no route and stays a plain JVM test.

Robolectric must be **4.16 or newer** — 4.14/4.15 cap at `maxSdkVersion=35` and fail with
`Package targetSdkVersion=36 > maxSdkVersion=35` before any test runs.

**Reading state without fighting the pipeline.** Three patterns, picked per ViewModel:
- `uiState.test { awaitItem() }` (Turbine) for `WhileSubscribed` state, which needs a live collector.
- `expectMostRecentItem()` after an action that emits intermediate states — `onRefresh` toggles
  `isRefreshing` around the call, so `awaitItem()` returns the in-flight state, not the settled one.
- plain `uiState.value` for `DetalleClienteViewModel`, whose state is `SharingStarted.Eagerly` and
  so is populated without a collector. Side-effect tests there assert on the fake, not the flow.

⚠️ **Fixture time matters once a ViewModel is in play.** `pedido()` defaults to `createdAt = 0L`.
The status use case is called by the ViewModel *without* a `now` override, so an epoch-0 pedido is
always "older than diasMaximos" and every client comes out CRITICO. Tests that care about status
must build debts with `createdAt = System.currentTimeMillis()`.

### Phase 5 — Room and repository integration (56 tests)

Real Room, in memory, under Robolectric. `DataSynchronizer` is mocked; everything else is the
production code path.

| Suite | Tests | Covers |
|---|---|---|
| `PedidoDaoTest` | 12 | Soft-delete filters, `IGNORE` insert, `@Relation` lines, `markAllPaidForCliente`, FK cascade |
| `ClienteDaoTest` | 10 | Blacklist round-trip, the `-1L` insert return that drives upsert, mercado cascade |
| `SyncOperationDaoTest` | 11 | FIFO replay, retry counters, dedup (DELETE survives), `observeLatestEnqueuedId` |
| `PedidoRepositoryImplTest` | 16 | `updateLines` status recompute + pago delta, initial pago, the enqueue contract — including `markAllPaidForCliente`, one upsert per settled pedido |
| `ClienteRepositoryImplTest` | 7 | insert-or-update upsert, blacklist/unblacklist, soft delete + queue labels |

`support/RoomTestDatabase.kt` builds the in-memory database and seeds the mercado/cliente rows the
pedido foreign keys require. **Foreign keys are enforced exactly as on device** — a pedido cannot be
inserted without its cliente. Fixtures must line up: a `detallePedidoEntity` whose `pedidoId` does
not match a real pedido fails on a constraint, not on the behaviour under test.

The central assertion across the repository suites is the offline-first pairing: every mutating call
writes locally **and** leaves exactly one `sync_operations` row, with the right entity type,
operation and label.

### Phase 6 — queue, sync and session internals (49 tests)

The heaviest untested logic in the app.

| Suite | Tests | Covers |
|---|---|---|
| `QueueProcessorGateTest` | 10 | Empty queue, all four `SessionResult` branches, the ownership gate — all DEFERRED without burning a retry |
| `QueueProcessorFlushTest` | 13 | Dedup collapsing, `forEachMatching` over the whole group, FAILED vs COMPLETED, `lastSuccessfulFlushAt`, mutex serialisation |
| `DataSynchronizerTest` | 15 | Staleness-before-session ordering, delta cursor, silent retry, cursor rollback, timeouts, thresholds |
| `SessionManagerImplTest` | 11 | Device ownership (DataStore), `canRestoreSession`, three `ensureValidSession` branches |

**Driving `QueueProcessor` without Supabase.** `supabase.from()` is an extension function and cannot
be stubbed usefully, so the tests queue operations for entities that no longer exist in Room —
`upsert()` bails on a missing row (`?: return`) before any network call. That exercises dedup, queue
bookkeeping and outcome reporting for real. Failures come from a throwing `StorageService` (the
`content://` photo-upload path), which is the one failure that also stops short of the network.
**The actual Supabase push is therefore not covered** and needs an integration test.

**`supabase.auth` is stubbed via `mockkStatic("io.github.jan.supabase.auth.AuthKt")`.** It is an
extension property, and `SessionManagerImpl`'s `init` collects `auth.sessionStatus` — so without
that stub the class cannot even be constructed. The accessor class name came from the stack trace
of the failure. `ensureValidSession`'s `NotAuthenticated` branch is not covered: it performs a real
token refresh. If that branch ever needs coverage, the honest fix is a small `AuthGateway` seam
rather than deeper mocking.

⚠️ **Never pass `runTest`'s own scope as an `appScope`.** `DataSynchronizer.isSyncing` is a
`stateIn(Eagerly)` that never completes, so `runTest` waits on it forever — the test hangs rather
than fails. Use `backgroundScope`, and wrap it in an `UnconfinedTestDispatcher(testScheduler)` when
the class under test uses fire-and-forget `appScope.launch` (as `triggerSyncIfStale` does), or the
launched work never runs and the assertion silently sees one call too few.

### Phase 6b — Room migrations (18 tests, `androidTest`)

The last structural gap. 15 migrations from v4 to v19 had never been executed by anything but a real
upgrade on a real device, and the app has no `fallbackToDestructiveMigration` — so a bad migration
is a crash on launch for every existing install, with no fallback.

| Suite | Tests | Covers |
|---|---|---|
| `MigrationTest` | 16 | The sweep: start versions `[4..15, 17, 18]` each migrated to 19 and validated against `19.json`; plus two guards over `ALL_MIGRATIONS` itself |
| `MigrationDataTest` | 2 | The two migrations that move data — `MIGRATION_15_16`'s `updatedAt` backfill and `MIGRATION_18_19`'s seeded global umbrales row |

**One test method per start version, not a loop.** A failure names the version that broke rather
than producing one opaque red test. `runMigrationsAndValidate(…, validateDroppedTables = true)` also
catches a migration that leaves an orphan table behind.

**`ALL_MIGRATIONS` is now the single list**, hoisted from `DatabaseModule.kt` into `AppDatabase.kt`.
`DatabaseModule` builds the database with `addMigrations(*ALL_MIGRATIONS)` and `MigrationTest`
replays the same array, so the tested chain and the shipped chain cannot drift. The guard test
asserts it covers every consecutive pair from 4 to the declared version, and that no entry skips or
reverses a version.

⚠️ **`@Database` is not runtime-retained**, so the test cannot read `version` back by reflection —
that returns `null` and NPEs. The version is now a `const val DATABASE_VERSION` that both the
annotation and the tests read. Bumping it without writing a migration fails the guard test instead
of crashing on the next upgrade, which is the whole point of the guard.

**Verified non-vacuous.** Removing `MIGRATION_16_17` from `ALL_MIGRATIONS` turns the guard red with
`No migration covers [(16, 17)]` and fails all 12 sweeps that cross that boundary, while the v17 and
v18 starts stay green — exactly the expected blast radius. Worth repeating after changing this suite.

### Behaviours these tests pinned

Five real behaviours were undocumented before being covered. The tests assert what the code
**does**, naming the surprise, rather than asserting what it arguably should do:

- **`extractMapsCoordinates` rejects whole-number coordinates.** All four patterns require `\.\d+`,
  so `?q=19,-99` returns `null` while `?q=19.0,-99.0` parses. Likely unintended, but it is the
  shipped behaviour.
- **`PedidoMapper.fromDto` resets `itemCount` to 0.** The Supabase payload has no such column, so a
  pedido arriving from delta sync reports no line count until its `detalle_pedido` rows are read.
- **`ClienteMapper.fromDto` only preserves local-only fields when handed an `existing` row.**
  `primaryPhoneIndex`, `blacklistBalance` and `blacklistIsManualAmount` reset to `0 / 0.0 / false`
  otherwise — the merge contract every syncer must honour (`ClienteSyncer` does).
- **The two payment paths disagree about overpayment.** `CreatePedidoUseCase` stores
  `paid = initialPayment` *unclamped*, so a pedido can be created with `paid > total`;
  `RegistrarPagoUseCase` applies `coerceAtMost(total)`. `Pedido.pending` clamps at zero either way,
  so nothing renders negative — but the stored `paid` differs depending on which path ran.
- **An empty cart produces a PAID pedido.** `initialPayment >= total` is `0.0 >= 0.0`, so a pedido
  with no items and no payment is created PAID with a `paidAt` stamp. The UI blocks empty carts,
  which is the only thing preventing it.

### Also fixed

`ExampleInstrumentedTest` asserted `packageName == "com.restrusher.ecomercecarlosv"`, which the
staging flavor's `applicationIdSuffix = ".staging"` made unpassable. It had never run — there is no
`connectedCheck` in CI. Now asserts by prefix. `ExampleUnitTest` was deleted.

---

## Resolved findings

Two behaviours the tests surfaced and deliberately recorded rather than changed, because each
needed a product decision. **Both were fixed in Phase 17h** — kept here because the reasoning is
worth keeping, and because each is now pinned by a test.

---

### Resolved — `markAllPaidForCliente` never synced

`PedidoRepositoryImpl.markAllPaidForCliente()` was the **only** mutating repository method that did
not enqueue a sync operation. "Marcar todo como pagado" when un-blacklisting a client settled their
pedidos on the device and the server never heard about it — the pedidos stayed unpaid remotely, and
the two halves of one operation disagreed, since the saldo extra created alongside it *did* sync.

It is a bulk `UPDATE` over N rows, so there was no single entity id to queue. The fix reads the
affected rows **before** the update — after it, the `status != 'PAID'` predicate matches nothing —
via the new `PedidoDao.unpaidForCliente`, then enqueues one `UPSERT` per settled pedido.
`QueueProcessor.deduplicate` keys on `(entityType, entityId)`, so the rows stay distinct and each
pedido pushes exactly once. `PedidoRepositoryImplTest` now asserts the pairing instead of
documenting its absence, including that an already-PAID pedido is not queued — the queue read has to
use the update's own predicate or it reports rows it never touched.

### Resolved — a third status rule in `MercadosViewModel`

Phase 3 unified the two cliente screens; a **third**, materially different copy lived on in
`MercadosViewModel.buildStats()`. It counted **all** unpaid pedidos (`balance = pedidos.sumOf { it.pending }`)
where the cliente screens count only `PARTIAL && !isSaldoExtra`, and it hardcoded `200.0` and `30`
days instead of reading `Umbrales`. A mercado could show a red dot while every client inside it
showed AL_DIA, and a superuser raising the thresholds had no effect on the dashboard.

`buildStats` now delegates to `CalcularEstadoClienteUseCase` with real `Umbrales`, so there is one
rule in the app. **This changed what the dashboard shows**: an untouched PENDING order or a saldo
extra no longer colours a mercado, so dots are rarer — previously any client with an open order lit
their mercado amber, including an order placed that morning. `now` is captured once per emission
rather than per cliente, so every client in one refresh is judged against the same instant and the
suite can assert the day threshold exactly.

---


## TODO

Roughly in value order. Each item ends with `testStagingDebugUnitTest` green before the next starts.

### Integration tests against staging Supabase

The two paths mocking genuinely cannot reach. Both are currently unverified by anything but manual
QA.

- [ ] `QueueProcessor`'s actual push — `upsert`/`delete` against real tables, the `content://`
      photo-upload rewrite, and the pedido path that replaces remote `detalle_pedido` and `pagos`
      rows. `supabase.from()` is an extension function; the unit tests deliberately stop before it
- [ ] `SessionManagerImpl.ensureValidSession`'s `NotAuthenticated` branch — the real token refresh,
      including that a 5xx maps to `OFFLINE` and **not** `REVOKED` (a server hiccup must never sign
      the user out). If this is wanted as a unit test instead, the honest route is extracting a
      small `AuthGateway` interface rather than mocking supabase-kt more deeply
- [ ] Sync round-trip: create on device A → appears on device B (already listed in
      `docs/features/mercados-supabase-todos.md`)

### Phase 7 — Compose UI

- [ ] Only if a concrete regression needs guarding. Lowest value per hour here: the screens are thin
      and the logic they render is already covered. Would need `createAndroidComposeRule` and
      probably `hilt-android-testing`

### Coverage gaps worth closing at some point

- [ ] The remaining 24 ViewModels — most are thin; add tests when one grows real derived state
- [ ] `CambiarContrasenaViewModel.onSave`'s two paths (self via Supabase auth, other via the admin
      Edge Function), including the `AuthErrorCode` → `@StringRes` mapping. Belongs with the
      integration tests above rather than behind mocks
- [ ] `CleanupRepositoryImpl` (192 lines) — two-phase export-then-delete, CSV quoting, XLSX writing.
      Untouched by every phase so far
- [ ] The 5 `data/sync/impl/*Syncer.kt` classes — delta cursor handling, `isDeleted` hard-delete,
      and `ClienteSyncer`'s preservation of local-only fields
- [ ] `ui/screen/reporte/html/*` — `buildReporteHtml` / `buildDiarioHtml` / `buildPorClienteHtml`
      are pure String builders over UI state and would be cheap to cover
### Deliberately not doing — CI

- [ ] Wire `./gradlew :app:testStagingDebugUnitTest` into `.github/workflows/` so the suite gates a
      release. **Deferred by choice, not an oversight** — the tests are for local development for
      now. Worth knowing what is being traded away: `release.yml` is the only workflow and it goes
      straight from checkout to assemble to signing to Telegram delivery without running a test, so
      a release can ship with the suite red. Revisit if that ever actually happens. The migration
      suite is a separate question — it needs an emulator and could not join CI without a
      `connectedCheck` job on a macOS or KVM runner.

### Known obstacles

- ~~**`Dispatchers.Main`**~~ — resolved in Phase 4. `support/MainDispatcherRule.kt` swaps in an
  `UnconfinedTestDispatcher`; every ViewModel suite applies it as a `@get:Rule`.
- **`app/schemas/16.json` was never committed** (checked git history — not a deletion; it is absent
  even at `3aa55c7`, the commit that introduced `version = 16`), though `MIGRATION_15_16` is live in
  `AppDatabase.kt`. **Still open.** No migration test may *start* at v16, which is why the sweep runs
  `[4..15, 17, 18]`. `createDatabase(15)` → `runMigrationsAndValidate(19)` still runs 15→16→17→18→19
  and validates against `19.json`, so the chain stays covered; only the intermediate v16 shape goes
  unvalidated. **Never delete an exported schema.**
- **`androidTest` method names cannot contain spaces** while `minSdk = 24` — see **Conventions**.
- ~~**`schemas` is not registered as an androidTest asset dir**~~ — resolved in Phase 6b;
  `app/build.gradle.kts` now has
  `sourceSets.getByName("androidTest").assets.srcDirs("$projectDir/schemas")`.
- ~~**`ALL_MIGRATIONS` is inlined in `DatabaseModule.kt`**~~ — resolved in Phase 6b; hoisted into
  `AppDatabase.kt` and consumed by both the production builder and `MigrationTest`.
- **`CreatePedidoUseCase` imports `ui.screen.pedido.CartItem`** — the domain layer depending on the
  UI layer. Confirmed harmless for tests (the fixture just builds a `CartItem`), but it is still
  worth a 3-file cleanup.
- **Time and ids are not injected.** The pedido use cases call `System.currentTimeMillis()` and
  `UUID.randomUUID()` directly. Phase 2 worked around it by asserting *relations* rather than
  values — `paidAt == createdAt` when PAID, `paidAt == null` otherwise, `paidAt > 0`, and that
  generated ids are distinct from one another. That reads fine and needed no production change, so
  injecting a clock stays optional rather than blocking.
