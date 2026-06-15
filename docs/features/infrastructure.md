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
Typed error hierarchy. All errors carry a `message: String` and an optional `cause: Throwable?`.

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

`AppNavigation` collects this flow and shows a `Toast` for every emitted error. Screens that already handle errors with their own UI (e.g. `LoginScreen` shows an inline banner) should **not** re-emit through `GlobalErrorHandler` — they already give the user feedback.

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
| `PedidoSyncer` | `pedidos` + `detalle_pedido` | INSERT IGNORE |

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
QueueProcessor picks it up and pushes to Supabase (background)
```

The user never waits for the network. The UI is always driven by Room.

### Room table: `sync_operations` (Room v14)

| Column | Type | Notes |
|--------|------|-------|
| `id` | `INTEGER` PK autoGenerate | Monotonically increasing — used as flush trigger |
| `entityType` | `TEXT` | `MERCADO` · `CLIENTE` · `PRODUCTO` · `PEDIDO` |
| `entityId` | `TEXT` | UUID of the affected entity |
| `operation` | `TEXT` | `UPSERT` · `DELETE` |
| `createdAt` | `INTEGER` | Epoch ms — used for deduplication ordering |
| `retryCount` | `INTEGER` | Incremented on each failed push attempt within a flush run |

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

**Trigger 2 — new entry enqueued while already online:**
Observes `MAX(id)` from `sync_operations` via `observeLatestEnqueuedId(): Flow<Long>`. Since `id` is auto-incremented, every new insert produces a strictly higher `MAX(id)`. When `distinctUntilChanged` detects a new value and the device is online, `flush()` is called immediately.

> **Why `MAX(id)` and not `COUNT(*)`?**
> If a deletion and insertion happen at the same time, Room may coalesce both changes into a single table notification. `COUNT(*)` could return the same number before and after (e.g. 3→2→3 coalesced to 3→3), and `distinctUntilChanged` would suppress it, missing the new row. `MAX(id)` always increases on a new insert regardless of concurrent deletions, so it can never miss an enqueue event.

### flush() algorithm

1. Load all pending entries from `sync_operations` where `retryCount < 3`.
2. Deduplicate by `(entityType, entityId)`:
   - `DELETE` always wins over `UPSERT` for the same entity.
   - Multiple `UPSERT` entries collapse into one (latest wins by `createdAt`).
3. For each deduplicated operation:
   - **UPSERT**: read the current entity state from Room → push DTO to Supabase via `upsert()`.
   - **DELETE**: call `supabase.from(table).delete { filter { eq("id", entityId) } }`.
4. On success: delete all raw queue entries for that entity.
5. On failure: increment `retryCount` for all raw entries for that entity, wait 2 seconds, continue to next entity.

**PEDIDO UPSERT** also pushes the current `detalle_pedido` rows: it deletes all remote detalles for the pedido and re-inserts the current Room set. This ensures line-item edits are always consistent.

### SyncWorker — periodic background safety net

**Package:** `data/queue/SyncWorker`

A `@HiltWorker` (WorkManager) that runs every **15 minutes** whenever the device has network connectivity. It acts as a safety net for two scenarios:

1. The app was killed with pending rows in the queue — `QueueProcessor`'s coroutines died but the Room rows survived.
2. All in-session flush attempts exhausted their 3-try limit — rows are stuck until the next worker run resets them.

```kotlin
override suspend fun doWork(): Result {
    syncOperationDao.resetAllRetryCount()  // give all rows a fresh start
    queueProcessor.flush()
    return Result.success()
}
```

`resetAllRetryCount()` sets every row's `retryCount` back to 0 before flushing. This means rows are **never permanently abandoned** — they get up to 3 attempts per flush session, and a fresh 3 attempts on every subsequent WorkManager run.

Scheduled in `PedidosApp.onCreate()` with `ExistingPeriodicWorkPolicy.KEEP` (won't re-schedule if already enqueued).

### Retry lifecycle

| Event | What happens |
|---|---|
| Push fails | `retryCount++`, 2s delay, continue with next entity |
| `retryCount` reaches 3 | Row skipped by `getPending()` for this flush run; logged to Logcat via `QueueProcessor` tag |
| Next connectivity restore | `flush()` runs again (rows with `retryCount < 3` only) |
| Next `SyncWorker` run (≤15 min) | `resetAllRetryCount()` → all rows eligible again → `flush()` |
| App killed | Room rows survive; `SyncWorker` picks them up within 15 min |

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
User force-kills the app
  [QueueProcessor coroutines are dead, but Room row survives]

15 minutes later — WorkManager SyncWorker fires (device is online)
  ↓ syncOperationDao.resetAllRetryCount()
  ↓ queueProcessor.flush()
    - Finds the pending PEDIDO UPSERT row
    - Pushes pedido + detalles to Supabase → ✅
    - Deletes row
```

### Scenario 4: Supabase is temporarily down

```
User edits a mercado → row enqueued (id=7)
flush() runs:
  - Attempt 1: Supabase 503 → retryCount=1, wait 2s
  - Attempt 2: Supabase 503 → retryCount=2, wait 2s
  - Attempt 3: Supabase 503 → retryCount=3, wait 2s
  - Row now has retryCount=3 → skipped by getPending()

[Supabase comes back 10 minutes later]

SyncWorker fires:
  ↓ resetAllRetryCount() → row id=7 retryCount reset to 0
  ↓ flush():
    - Supabase is healthy → push succeeds → row deleted ✅
```

### Scenario 5: Edit → delete same entity before flush

```
User creates cliente A → UPSERT enqueued (id=10)
User immediately deletes cliente A → DELETE enqueued (id=11)

flush() deduplicates (entityType=CLIENTE, entityId=<A>):
  - Sees both UPSERT and DELETE
  - DELETE wins → only one Supabase call: delete the cliente
  - Both rows 10 and 11 deleted from sync_operations
```

---

## Wiring Summary

```
PedidosApp.onCreate()
  ├── dataSynchronizer.start()    ← registers connectivity-restore listener (clears staleness map)
  ├── queueProcessor.start()     ← two flush triggers (connectivity + new entry)
  └── SyncWorker.schedule()      ← WorkManager periodic 15-min background flush

Repository.getXxx() [any primary read Flow method]
  └── dataSynchronizer.triggerSyncIfStale(entityType, threshold)
        └── if stale + online → background coroutine → syncer.sync() → Room updated → Flow emits

AppNavigation
  └── LaunchedEffect(Unit)
        └── errorHandler.errors.collect → Toast.makeText(...)
```

---

## Adding a New Entity to Sync

1. Add `fromDto` and `toDto` methods to the entity's `Mapper` object.
2. Create `data/sync/impl/YourEntitySyncer.kt` implementing `EntitySyncer`.
3. Register it in `DataSynchronizer` (add to the sync chain, respecting FK order).
4. Add `EntityType.YOUR_ENTITY` constant in `SyncOperationEntity.kt`.
5. Update `QueueProcessor.upsert()` and `delete()` with the new entity type and table name.
6. Update the repository to call `syncOperationDao.enqueue(...)` after each write.
