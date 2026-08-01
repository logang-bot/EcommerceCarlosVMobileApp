# Infrastructure: Error Manager, Data Synchronizer, Network Queue

Implemented in the session following Phase 9. These three features share a common dependency: **NetworkMonitor**.

---

## NetworkMonitor

**Package:** `data/network/`

Detects internet connectivity using Android's `ConnectivityManager.NetworkCallback`.

```kotlin
interface NetworkMonitor {
    val isOnline: Boolean          // one-shot synchronous check
    val isOnlineFlow: Flow<Boolean> // continuous stream, distinctUntilChanged
}
```

- `NetworkMonitorImpl` registers a `NetworkCallback` and emits `true`/`false` when the active network changes.
- `VALIDATED` capability is checked, so captive portals that don't have internet are treated as offline.
- Provided as a Hilt `@Singleton` via `NetworkModule`.

---

## Centralized Error Manager

**Package:** `data/error/`, `domain/error/`

Three components working together:

### AppError (sealed class)
Typed error hierarchy. Every error carries **two** messages: `message: String` is developer-facing and
goes to the log only (it may hold entity ids, HTTP bodies, stack context), while `@StringRes
userMessageRes: Int` is the short Spanish text the user is allowed to see. Keeping them separate is
what stops technical text reaching the UI — `AppError.Queue("Failed to push PEDIDO(<uuid>)…")` used to
be rendered verbatim in a toast. Plus an optional `cause: Throwable?`.

```
AppError
├── Network   — Supabase / HTTP errors
├── Database  — Room errors
├── Sync      — Data synchronizer failures
├── Queue     — Queue processor failures
└── Unknown   — Anything else
```

### AppErrorLogger (object)
Wraps `android.util.Log` with consistent tags and severity routing:
- `Network`, `Database`, `Unknown` → `Log.e`
- `Sync`, `Queue` → `Log.w`

Callable from anywhere with no dependencies.

### GlobalErrorHandler (singleton)
A `SharedFlow`-based event bus injected by Hilt wherever errors need to be emitted. The flow has `extraBufferCapacity = 8` so rapid bursts don't lose events.

```kotlin
@Inject lateinit var errorHandler: GlobalErrorHandler

// emit from any coroutine
errorHandler.emit(AppError.Network("No internet", e))
// or shorthand
errorHandler.emit(throwable, "context description")
```

`AppViewModel.userErrors` maps this flow to `userMessageRes` and drops repeats of the same message
within 5s; `AppNavigation` collects that and shows a Material3 **Snackbar** (hosted in a `Box` around
the `NavHost` — there is no global `Scaffold`, since each screen builds its own with its own bottom nav
and FABs). Emit one error per logical failure, not per record: `QueueProcessor.flush()` reports once for
the whole flush rather than once per operation, which previously stacked a dozen identical messages.
Screens that already handle errors with their own UI (e.g. `LoginScreen` shows an inline banner) should
**not** re-emit through `GlobalErrorHandler` — they already give the user feedback.

---

## Data Synchronizer (Read Path)

**Package:** `data/sync/`

Keeps the local Room database in sync with Supabase on the **read path** (remote → local). Room is always the single source of truth for the UI; `DataSynchronizer` hydrates Room from Supabase lazily and in the background.

### Lazy, per-screen fetching with a staleness threshold

Data is **not** fetched eagerly on app start. Instead, each repository triggers a sync when a screen first collects its read Flow:

```kotlin
// Inside each repository's primary read Flow method:
dataSynchronizer.triggerSyncIfStale(EntityType.CLIENTE, DataSynchronizer.THRESHOLD_BUSINESS_MS)
return dao.getByMercado(mercadoId).map { ... }
```

`triggerSyncIfStale` is non-blocking — it launches a background coroutine and returns immediately. The UI starts collecting Room data right away; when the sync completes, Room emits the updated rows automatically.

### Staleness thresholds

| Threshold constant | Value | Used for |
|---|---|---|
| `THRESHOLD_MASTER_MS` | 2 hours | `MERCADO`, `PRODUCTO` — master/catalog data, changes infrequently |
| `THRESHOLD_BUSINESS_MS` | 30 minutes | `CLIENTE`, `PEDIDO` — business data, changes more often |

If the last successful sync for an entity was within the threshold, `triggerSyncIfStale` is a no-op. The staleness map is **in-memory only** — it resets on every app start, so the first navigation after login always triggers a fresh fetch regardless of how recently the data was last synced.

### Connectivity restore

When the device goes from offline to online, `lastSyncedAt` is cleared entirely. The next navigation to any screen will fetch fresh data from Supabase as if no previous sync had occurred.

### Concurrency

Multiple simultaneous calls to `triggerSyncIfStale` for the same entity are safe — the timestamp is stamped before the network call starts, so any concurrent call sees the entity as "fresh" and exits immediately. Different entities sync concurrently without blocking each other.

### Entity syncers

| Syncer | Table | Strategy |
|--------|-------|----------|
| `MercadoSyncer` | `mercados` | IGNORE + UPDATE |
| `ClienteSyncer` | `clientes` | IGNORE + UPDATE; preserves local-only fields (`primaryPhoneIndex`, `blacklistBalance`, `blacklistIsManualAmount`) from existing Room row |
| `ProductoSyncer` | `productos` | INSERT OR REPLACE |
| `PedidoSyncer` | `pedidos` + `detalle_pedido` | INSERT IGNORE; two fetch methods (`fetchAllPedidoPages`, `fetchAllDetallPages`) |

**Full-fetch batching** (Phase 10c): when `since == 0L` all syncers use a `while(true)` loop with `supabase.from(...).select { filter { eq("is_deleted", false) }; range(offset, offset + 999) }` and `BATCH_SIZE = 1000`. The loop breaks when `page.size < BATCH_SIZE`, accumulating results via `buildList { addAll(page) }`. This bypasses PostgREST's single-call 1 000-row cap. Delta fetches (`since > 0L`) are unaffected — they fetch all rows with `updated_at > since` including soft-deleted ones.

**Soft-delete propagation** (Phase 11): delta syncs include soft-deleted rows (because `UPDATE SET is_deleted = true` bumps `updated_at` via trigger). Each syncer checks `dto.isDeleted`: if `true`, the entity is hard-deleted from Room (`dao.deleteById(dto.id)`); otherwise the normal upsert path runs. `PedidoSyncer` collects non-deleted IDs into `upsertedIds` and only fetches `detalle_pedido` for those — soft-deleted pedidos have their Room rows (and cascaded detalles) removed immediately.

Each sync call has a 10-second timeout. On timeout or failure the timestamp is removed so the next navigation retries. Errors are routed to `GlobalErrorHandler`.

### StateFlow
`DataSynchronizer.isSyncing: StateFlow<Boolean>` is `true` whenever at least one entity sync is in flight. Available for future UI indicators.

---

## Network Request Queue (Write Path)

**Package:** `data/queue/`, `data/local/entity/SyncOperationEntity`

Queues all user mutations (create, update, delete) and pushes them to Supabase in the background, without blocking the UI.

### Core principle

Every write is **local-first**:

```
User action
  ↓
Room write (instant — UI reflects the change immediately)
  ↓
SyncOperationEntity enqueued in sync_operations table
  ↓
QueueProcessor picks it up (background):
  · For MERCADO/CLIENTE/PRODUCTO with content:// photoUrl → uploads to Storage first,
    updates Room with https:// URL, then pushes DTO to Supabase
  · For all other ops → pushes DTO to Supabase directly
```

The user never waits for the network. The UI is always driven by Room. Photos are stored as local `content://` URIs in Room until `QueueProcessor` uploads them — Coil renders local URIs instantly.

### Room table: `sync_operations` (Room v15)

| Column | Type | Notes |
|--------|------|-------|
| `id` | `INTEGER` PK autoGenerate | Monotonically increasing — used as flush trigger |
| `entityType` | `TEXT` | `MERCADO` · `CLIENTE` · `PRODUCTO` · `PEDIDO` |
| `entityId` | `TEXT` | UUID of the affected entity |
| `operation` | `TEXT` | `UPSERT` · `DELETE` |
| `createdAt` | `INTEGER` | Epoch ms — used for deduplication ordering |
| `retryCount` | `INTEGER` | Incremented on each failed push attempt within a flush run |
| `entityLabel` | `TEXT` | Human-readable label shown in the Sincronización screen (e.g. `"Bs. 120,00"` for a pedido, `"Mercado de Coche"` for a mercado). Added in MIGRATION_14_15. |

### Write path (repositories)

Every repository write enqueues a `SyncOperationEntity` **after** the local Room write succeeds:

```kotlin
// Example — what every repository write does internally:
suspend fun save(entity: MyEntity) {
    dao.insert(entity)                     // 1. local write (fast)
    syncOperationDao.enqueue(             // 2. queue for later push
        SyncOperationEntity(
            entityType = EntityType.MY_ENTITY,
            entityId   = entity.id,
            operation  = SyncOp.UPSERT,
        )
    )
}
```

Affected repositories: `MercadoRepositoryImpl`, `ClienteRepositoryImpl`, `ProductoRepositoryImpl`, `PedidoRepositoryImpl`.

### QueueProcessor — flush triggers

`QueueProcessor` runs two permanent coroutines in the app's `CoroutineScope`:

**Trigger 1 — connectivity restore:**
Collects `NetworkMonitor.isOnlineFlow`. Calls `flush()` every time the device goes from offline to online.

**Trigger 2 — new entry enqueued (and startup orphan recovery):**
Observes `MAX(id)` from `sync_operations` via `observeLatestEnqueuedId(): Flow<Long>`. Since `id` is auto-incremented, every new insert produces a strictly higher `MAX(id)`. When `distinctUntilChanged` detects a new value and the device is online, `flush()` is called immediately. It also calls `SyncWorker.schedule()` unconditionally so a WorkManager job is always queued as a fallback in case the app dies before the in-process flush completes.

Room `Flow` queries **emit the current DB value immediately when collection starts**, not only on subsequent changes. This means Trigger 2 also acts as an **orphan recovery mechanism on every app launch**: if rows survived a previous force-stop, `MAX(id) > 0` fires the moment `QueueProcessor.start()` begins collecting, flushing them in-process right away (if online) or queuing a WorkManager job (if offline).

> **Why `MAX(id)` and not `COUNT(*)`?**
> If a deletion and insertion happen at the same time, Room may coalesce both changes into a single table notification. `COUNT(*)` could return the same number before and after (e.g. 3→2→3 coalesced to 3→3), and `distinctUntilChanged` would suppress it, missing the new row. `MAX(id)` always increases on a new insert regardless of concurrent deletions, so it can never miss an enqueue event.

### flush() algorithm

`flush()` returns `Boolean` — `true` if at least one op failed, `false` if everything was pushed successfully (or the queue was empty). `SyncWorker` uses this return value to decide between `Result.success()` and `Result.retry()`.

1. Load **all** pending entries from `sync_operations` (no retry-count filter — WorkManager backoff is the retry gate, not a per-row counter).
2. Deduplicate by `(entityType, entityId)`:
   - `DELETE` always wins over `UPSERT` for the same entity.
   - Multiple `UPSERT` entries collapse into one (latest wins by `createdAt`).
3. For each deduplicated operation:
   - **UPSERT**: read the current entity state from Room → push DTO to Supabase via `upsert()`.
   - **DELETE**: call `supabase.from(table).update({ set("is_deleted", true) }) { filter { eq("id", entityId) } }`. This is a soft-delete — the Supabase row is not removed. The `set_updated_at_ms()` trigger fires automatically, bumping `updated_at`. Other devices pick up the deletion in their next delta sync.
4. On success: delete all raw queue entries for that entity.
5. On failure: increment `retryCount` for all raw entries for that entity (observability only — not used to gate retries), set `anyFailed = true`, continue to next entity.
6. Return `anyFailed`.

**MERCADO / CLIENTE / PRODUCTO UPSERT — photo upload**: before pushing the entity DTO to Supabase, `QueueProcessor` checks `entity.photoUrl?.startsWith("content://")`. If `true`, it calls `storageService.uploadPhoto(bucket, entityId, Uri.parse(photoUrl))`, updates the Room entity with the resulting `https://` URL, then proceeds with the Supabase upsert. If the upload throws (network failure), the outer `runCatching` catches it and `anyFailed = true`, leaving the op in the queue for the next retry cycle.

This makes the photo upload path fully offline-first: the UI writes the local `content://` URI to Room immediately (no blocking network call on save), and the upload happens here in the background. On reconnect, any entity with a pending `content://` photoUrl is automatically uploaded as part of normal queue processing.

Buckets used: `"mercado-photos"`, `"cliente-photos"`, `"producto-photos"`.

**PEDIDO UPSERT** also pushes the current `detalle_pedido` rows: it deletes all remote detalles for the pedido and re-inserts the current Room set. This ensures line-item edits are always consistent.

### SyncWorker — background safety net with exponential backoff

**Package:** `data/queue/SyncWorker`

A `@HiltWorker` (WorkManager) that acts as the safety net for the write queue. It covers the scenario where the app is killed with pending rows in the queue — `QueueProcessor`'s coroutines died, but the Room rows survived.

```kotlin
override suspend fun doWork(): Result {
    val anyFailed = queueProcessor.flush()
    return if (anyFailed) Result.retry() else Result.success()
}
```

- If `flush()` pushed every pending op successfully → `Result.success()`. The worker is done; the next write will schedule a new one.
- If any op failed → `Result.retry()`. WorkManager automatically reschedules the worker with **exponential backoff**: 30 s → 60 s → 120 s → … capped at WorkManager's internal maximum (~5 hours). No manual retry logic is needed.

```kotlin
fun schedule(workManager: WorkManager) {
    val request = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        )
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()
    workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
}
```

Key properties of `schedule()`:

| Property | Value | Effect |
|---|---|---|
| `OneTimeWorkRequest` | — | Worker fires once; a new job is only created by the next write |
| `NetworkType.CONNECTED` | — | Worker won't run without internet; WorkManager watches `ConnectivityManager.NetworkCallback` and fires automatically on reconnect |
| `BackoffPolicy.EXPONENTIAL, 30s` | — | On `Result.retry()`: 30 s → 60 s → 120 s → … up to ~5 h |
| `ExistingWorkPolicy.KEEP` | — | If a worker is already pending or in backoff, a new `schedule()` call is silently ignored |

`schedule()` is called from two places:
1. **`QueueProcessor.start()`** — called every time `observeLatestEnqueuedId()` emits (new write or startup with orphaned rows). This is the primary scheduling path. With `KEEP`, it is a no-op when a worker is already pending.
2. **`PedidosApp.onCreate()`** — narrow insurance against a race condition: if the app is killed in the brief window between `onCreate()` returning and `QueueProcessor`'s coroutines starting, WorkManager already has a job queued. In practice this window is milliseconds and the scenario is extremely unlikely, but the call costs nothing. With `KEEP`, it is a no-op in all normal cases where `QueueProcessor` has already scheduled a job.

> The primary orphan recovery is handled by `QueueProcessor` itself (Trigger 2 above). `SyncWorker.schedule()` in `onCreate()` is purely a last-resort belt-and-suspenders measure, not the main mechanism.

This means a WorkManager job is **always scheduled** whenever there is a pending op in the queue, regardless of whether the in-process flush succeeded or the app was killed mid-session.

### Retry lifecycle

| Event | What happens |
|---|---|
| Push fails | `retryCount++` (observability), `anyFailed = true`, continue to next entity |
| `flush()` returns `true` (any failure) | `SyncWorker` returns `Result.retry()` → WorkManager reschedules with backoff |
| WorkManager backoff schedule | 30 s → 60 s → 120 s → 240 s → … (capped at ~5 h) |
| Connectivity restored (in-process) | `QueueProcessor.isOnlineFlow` triggers `flush()` immediately regardless of backoff |
| New write while worker is in backoff | `QueueProcessor` calls `flush()` in-process immediately (app alive); WorkManager KEEP ensures a job is also queued for the app-killed case |
| App killed with pending rows | Room rows survive; `SyncWorker` picks them up when device reconnects or on next app launch |

---

## End-to-End Examples

### Scenario 1: Normal write while online

```
User taps "Guardar" on a new cliente
  ↓ ClienteRepositoryImpl.save()
    1. dao.insert(clienteEntity)           → Room updated instantly
    2. syncOperationDao.enqueue(UPSERT)    → new row in sync_operations (id=42)
  ↓ observeLatestEnqueuedId() emits 42 (was 41)
  ↓ networkMonitor.isOnline == true → flush() called immediately
    - Reads row id=42: CLIENTE UPSERT <uuid>
    - Reads ClienteEntity from Room → builds ClienteDto
    - supabase.from("clientes").upsert(dto) → ✅
    - Deletes row id=42 from sync_operations
```

The Supabase push happens in the background within milliseconds. The user already sees the new cliente in the list because the UI reads from Room.

### Scenario 2: Write while offline

```
User taps "Guardar" — no internet
  ↓ ClienteRepositoryImpl.save()
    1. dao.insert(clienteEntity)           → Room updated instantly
    2. syncOperationDao.enqueue(UPSERT)    → new row in sync_operations (id=42)
  ↓ observeLatestEnqueuedId() emits 42
  ↓ networkMonitor.isOnline == false → flush() NOT called

[user keeps using the app normally — data visible from Room]

Device goes back online
  ↓ isOnlineFlow emits true → flush() called
    - Finds row id=42: CLIENTE UPSERT <uuid>
    - Pushes to Supabase → ✅
    - Deletes row id=42
```

### Scenario 3: App killed with pending entries

```
User creates a pedido while offline → row in sync_operations
observeLatestEnqueuedId() emits → SyncWorker.schedule() called (WorkManager job queued)
User force-kills the app
  [QueueProcessor coroutines are dead, but Room row and WorkManager job survive]

Device goes online → WorkManager fires SyncWorker (CONNECTED constraint satisfied)
  ↓ queueProcessor.flush()
    - Finds the pending PEDIDO UPSERT row
    - Pushes pedido + detalles to Supabase → ✅ → returns false
  ↓ Result.success() — worker is done
```

### Scenario 4: Supabase is temporarily down

```
User edits a mercado → row enqueued (id=7)
  ↓ SyncWorker.schedule() called (WorkManager job queued)
  ↓ networkMonitor.isOnline == true → flush() called in-process
    - Supabase 503 → retryCount=1 → returns true (anyFailed)

SyncWorker doWork() runs:
  ↓ flush() → Supabase 503 → retryCount=2 → returns true
  ↓ Result.retry() → WorkManager backoff: wait 30 s

30 s later — WorkManager retries:
  ↓ flush() → Supabase 503 → retryCount=3 → returns true
  ↓ Result.retry() → WorkManager backoff: wait 60 s

60 s later — WorkManager retries:
  ↓ flush() → Supabase is healthy → push succeeds → row deleted ✅ → returns false
  ↓ Result.success() — worker is done
```

### Scenario 5 (updated): New write while worker is in backoff

```
User edits cliente A → row enqueued (id=10)
  ↓ SyncWorker.schedule() → KEEP: worker already in 60 s backoff, ignored
  ↓ networkMonitor.isOnline == true → flush() called in-process immediately
    - Supabase recovers → push succeeds → row deleted ✅

WorkManager backoff eventually fires → flush() finds empty queue → returns false → Result.success()
```

The in-process path always bypasses WorkManager backoff while the app is alive.

### Scenario 6: Edit → delete same entity before flush

```
User creates cliente A → UPSERT enqueued (id=10)
User immediately deletes cliente A → DELETE enqueued (id=11)

flush() deduplicates (entityType=CLIENTE, entityId=<A>):
  - Sees both UPSERT and DELETE
  - DELETE wins → only one Supabase call: delete the cliente
  - Both rows 10 and 11 deleted from sync_operations
```

---

---

## Local Notifications (SyncNotifier)

**Package:** `data/queue/SyncNotifier`

Shows a system notification for each `SyncWorker` run so the user knows their data is being sent even when the app is in the background.

### Channel setup

`createChannel()` is called once in `PedidosApp.onCreate()`. It registers a `NotificationChannelCompat` with:
- ID: `sync_channel`
- Importance: `IMPORTANCE_LOW` — no sound, no vibration, appears silently in the notification shade.
- The call is idempotent (Android ignores it if the channel already exists).

### Three notification states

All three share the same `NOTIFICATION_ID = 1001`, so each replaces the previous one instead of stacking.

| Method | Title string | Notable flags |
|---|---|---|
| `notifyStarted()` | `sync_notification_started_title` | `setProgress(0,0,true)` (indeterminate bar) + `setOngoing(true)` (not dismissible) |
| `notifySuccess()` | `sync_notification_success_title` | `setAutoCancel(true)` (dismissed on tap) |
| `notifyFailure()` | `sync_notification_failure_title` | `setAutoCancel(true)` + body text via `sync_notification_failure_body` |

`SyncWorker` calls them in sequence: `notifyStarted()` at the top of `doWork()`, then either `notifySuccess()` or `notifyFailure()` depending on whether `flush()` returned `true`.

### Permission guard

`show()` has two silent bail-outs before posting:
1. **Android 13+ (TIRAMISU):** checks `POST_NOTIFICATIONS` at runtime — skips the notification if not granted.
2. **`areNotificationsEnabled()`:** covers the case where the user disabled notifications for the app globally in system settings.

The `@SuppressLint("MissingPermission")` annotation is present because the lint rule can't see that the check is already done inside `show()`.

### Permission request

`POST_NOTIFICATIONS` is declared in `AndroidManifest.xml` and requested at runtime **immediately after a successful login** in `LoginScreen`. A `rememberLauncherForActivityResult(RequestPermission)` launcher wraps the `onLoginSuccess` callback:

```kotlin
val handleLoginSuccess: () -> Unit = {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    onLoginSuccess()  // navigates away — permission dialog appears on top of the next screen
}
```

The system dialog fires as an overlay on the destination screen. If the user denies it, `SyncNotifier.show()` silently skips future notifications — no crash, no retry loop.

---

## Sincronización Screen

**Package:** `ui/screen/sincronizacion/`  
**Route:** `SincronizacionRoute` (from `AppRoutes.kt`)  
**Entry point:** `SyncBarIcon` in the MercadosScreen top bar — tap to navigate.

### Three states

| State | Condition | Icon | Dot badge |
|-------|-----------|------|-----------|
| `SYNCED` | `sync_operations` table is empty | `CloudDone` (onSurface) | none |
| `PENDING` | Table has rows, all `retryCount == 0` | `CloudUpload` (onSurface) | blue |
| `ERROR` | Any row has `retryCount > 0` | `CloudOff` (amberText) | amber |

### How `syncIconState` is derived

`SincronizacionViewModel` and `MercadosViewModel` both observe `syncOperationDao.observeAll()`:

```kotlin
val iconState = when {
    allOps.isEmpty()              -> SyncIconState.SYNCED
    allOps.any { it.retryCount > 0 } -> SyncIconState.ERROR
    else                          -> SyncIconState.PENDING
}
```

The same state drives both the `SyncBarIcon` badge in the top bar and the full Sincronización screen layout.

### Retry button

The ERROR state shows a "Reintentar envío" button in `SyncBanner`. It calls `SincronizacionViewModel.onRetry()`, which calls `QueueProcessor.triggerFlush()`. `triggerFlush()` checks `networkMonitor.isOnline` and, if true, calls `flush()` in the app's `CoroutineScope`. If any ops are still failing, `retryCount` remains > 0 and the screen stays in ERROR state. Once all ops succeed, the table empties and the screen transitions to SYNCED.

### `lastSuccessfulFlushAt`

`QueueProcessor` exposes a `StateFlow<Long?>` that is set to `System.currentTimeMillis()` at the end of every `flush()` call where `!anyFailed` and the queue was not empty. The SYNCED state chip reads this to show "Última sincronización · hace X min".

---

## Wiring Summary

```
PedidosApp.onCreate()
  ├── syncNotifier.createChannel()  ← registers the notification channel (idempotent)
  ├── dataSynchronizer.start()      ← registers connectivity-restore listener (clears staleness map)
  ├── queueProcessor.start()        ← two flush triggers (connectivity + new entry) + schedules WM job on each enqueue
  └── SyncWorker.schedule()         ← safety net: ensures a WM job exists to push any rows that survived a previous force-stop (rows are only deleted after a successful Supabase push, never before)

Repository.getXxx() [any primary read Flow method]
  └── dataSynchronizer.triggerSyncIfStale(entityType, threshold)
        └── if stale + online → background coroutine → syncer.sync() → Room updated → Flow emits

MercadosScreen (top bar)
  └── SyncBarIcon(state = state.syncIconState, onClick = onSyncClick)
        └── onSyncClick → navController.navigate(SincronizacionRoute)

AppNavigation
  ├── LaunchedEffect(Unit)
  │     └── appViewModel.userErrors.collect → snackbarHostState.showSnackbar(getString(res))
  └── HomeRoute → HomeScreen(onSyncClick = { navController.navigate(SincronizacionRoute) })
```

---

## Adding a New Entity to Sync

1. Add `fromDto` and `toDto` methods to the entity's `Mapper` object.
2. Create `data/sync/impl/YourEntitySyncer.kt` implementing `EntitySyncer`.
3. Register it in `DataSynchronizer` (add to the sync chain, respecting FK order).
4. Add `EntityType.YOUR_ENTITY` constant in `SyncOperationEntity.kt`.
5. Update `QueueProcessor.upsert()` and `delete()` with the new entity type and table name.
6. Update the repository to call `syncOperationDao.enqueue(...)` after each write.
