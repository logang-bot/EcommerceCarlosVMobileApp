# Database Schema

Room version: **19**. Supabase integration: Phase 10 (delta sync) + Phase 11 (soft-delete).

All primary keys are client-generated UUIDs (`String`). All timestamp columns store **epoch milliseconds** (`Long` in Room, `bigint` in Supabase). Nullable columns are marked `?`.

---

## Current tables (Room v19)

### Foreign keys

Room enforces one chain, every link `ON DELETE CASCADE`:

```
mercados → clientes → pedidos → { detalle_pedido, pagos }
```

`productos`, `umbrales` and `users` have none, and `detalle_pedido.productoId` is a plain column
rather than a foreign key. Sync must therefore write parents before children — see
`SyncParentResolver` in `docs/features/infrastructure.md`.


### `users`

Stores app user accounts. Managed exclusively by the Superusuario. Populated locally on first login; will sync from Supabase auth in Phase 9.

| Column | Room type | Supabase type | Nullable | Notes |
|--------|-----------|---------------|----------|-------|
| `id` | `String` PK | `uuid` PK | — | Supabase auth UID |
| `email` | `String` | `text` | — | Unique |
| `name` | `String` | `text` | — | Display name |
| `role` | `String` | `text` | — | Enum name: `SUPERUSUARIO` \| `USUARIO` |
| `phone` | `String?` | `text` | ✓ | |
| `photoUrl` | `String?` | `text` | ✓ | Supabase Storage URL |
| `isActive` | `Boolean` | `boolean` | — | Default `true` |
| `createdAt` | `Long` | `bigint` | — | Epoch ms |
| `lastSeenAt` | `Long?` | `bigint` | ✓ | Updated on each login |
| `biometricEnabledAt` | `Long?` | `bigint` | ✓ | Non-null means enrolled; stored locally only (not synced) |

**DAO operations:** `getAll()` flow · `getById()` · `insert(REPLACE)` · `update()` · `deleteById()` · `setActive()` · `setBiometricEnabled()` · `updateProfile(name, email, phone)` · `getBiometricEnabledUser()`

**Supabase notes:** `biometricEnabledAt` is device-local — do not create a column for it in Supabase. Row-level security: only `SUPERUSUARIO` can insert/update/delete; all authenticated users can read.

---

### `mercados`

Shared resource. All users can read and write.

| Column | Room type | Supabase type | Nullable | Notes |
|--------|-----------|---------------|----------|-------|
| `id` | `String` PK | `uuid` PK | — | |
| `name` | `String` | `text` | — | |
| `address` | `String` | `text` | — | |
| `photoUrl` | `String?` | `text` | ✓ | Supabase Storage URL |
| `mapsUrl` | `String?` | `text` | ✓ | Raw Google Maps URL pasted by user |
| `latitude` | `Double?` | `float8` | ✓ | Extracted from `mapsUrl` on save |
| `longitude` | `Double?` | `float8` | ✓ | Extracted from `mapsUrl` on save |
| `createdAt` | `Long` | `bigint` | — | Epoch ms |
| `updatedAt` | `Long` | `bigint` | — | Epoch ms; set by `moddatetime` trigger on every UPDATE. **Used as delta-sync cursor.** *(added v16)* |
| `isDeleted` | `Boolean` | `boolean` | — | Default `false`; set to `true` on soft-delete *(added v17)* |

**DAO operations:** `getAll()` flow (ordered `name ASC`) · `getById()` (suspend) · `getByIdFlow()` (Flow — reactive) · `insert(IGNORE)` returning `Long` · `update()` · `softDeleteById()` · `deleteById()`

**Supabase notes:** Consider a `geography(Point)` column as an alternative to separate lat/lng if you want PostGIS distance queries later. Suggested indexes: `mercados(name)`, `mercados(updated_at)`.

---

### `clientes` *(Room v8 → v13 — Phase 3 + 7)*

Belongs to a `mercados` row. Represents an individual customer at a market stall.

| Column | Room type | Supabase type | Nullable | Notes |
|--------|-----------|---------------|----------|-------|
| `id` | `String` PK | `uuid` PK | — | |
| `mercadoId` | `String` FK | `uuid` FK → `mercados.id` | — | `ON DELETE CASCADE` |
| `name` | `String` | `text` | — | |
| `description` | `String` | `text` | — | Stall description e.g. "Puesto 14 · verduras" |
| `photoUrl` | `String?` | `text` | ✓ | Supabase Storage URL |
| `phones` | `String` | `text` | — | Pipe-separated list: `"0414-123\|0424-456"`. Empty string if none. |
| `primaryPhoneIndex` | `Int` | `int4` | — | 0-based index into `phones` list identifying the primary contact number; default `0` *(added v13)* |
| `mapsUrl` | `String?` | `text` | ✓ | URL that opens the device map app — no lat/lng stored |
| `isBlacklisted` | `Boolean` | `boolean` | — | Default `false` |
| `blacklistReason` | `String?` | `text` | ✓ | Set when `isBlacklisted = true` |
| `blacklistedAt` | `Long?` | `bigint` | ✓ | Epoch ms |
| `blacklistBalance` | `Double` | `float8` | — | Owed balance recorded at time of blacklisting; default `0.0` *(added v8)* |
| `blacklistIsManualAmount` | `Boolean` | `boolean` | — | `true` when amount was entered manually (MANUAL mode); `false` = AUTO *(added v12)* |
| `createdAt` | `Long` | `bigint` | — | Epoch ms |
| `updatedAt` | `Long` | `bigint` | — | Epoch ms; set by `moddatetime` trigger. **Used as delta-sync cursor.** *(added v16)* |
| `isDeleted` | `Boolean` | `boolean` | — | Default `false`; set to `true` on soft-delete *(added v17)* |

**DAO operations:** `getByMercado(mercadoId)` flow (non-blacklisted, non-deleted, name ASC) · `getAll()` flow (non-blacklisted, non-deleted) · `getAllIncludingBlacklisted()` flow (non-deleted) · `getBlacklisted()` flow (blacklisted only, non-deleted, `blacklistedAt DESC`) · `getByIdFlow(id)` (non-deleted Flow) · `getById()` · `insert(IGNORE)` returning `Long` · `update()` · `softDeleteById()` · `deleteById()` · `blacklist(id, reason, balance, at, isManualAmount)` · `unblacklist(id)` (resets all blacklist fields incl. `blacklistIsManualAmount`)

**Indexes:** `clientes(mercadoId)`, `clientes(name)`, `clientes(isBlacklisted)`, `clientes(updated_at)`.

> `status` and `balance` are computed from `pedidos` in Phase 4 — do not store them. Location is stored only as `mapsUrl`; no lat/lng columns.

---

### `productos` *(Room v7 — Phase 6)*

Global product catalogue, shared across all users.

| Column | Room type | Supabase type | Nullable | Notes |
|--------|-----------|---------------|----------|-------|
| `id` | `String` PK | `uuid` PK | — | |
| `name` | `String` | `text` | — | |
| `description` | `String?` | `text` | ✓ | Variant, size, or note |
| `price` | `Double` | `float8` | — | Current price |
| `photoUrl` | `String?` | `text` | ✓ | Supabase Storage URL |
| `isActive` | `Boolean` | `boolean` | — | Default `true`; catalogue visibility toggle |
| `createdAt` | `Long` | `bigint` | — | Epoch ms |
| `updatedAt` | `Long` | `bigint` | — | Epoch ms; set by `moddatetime` trigger. **Used as delta-sync cursor.** *(added v16)* |
| `isDeleted` | `Boolean` | `boolean` | — | Default `false`; set to `true` on soft-delete *(added v17)* |

**DAO operations:** `getAll()` flow (active AND non-deleted, name ASC) · `getById()` (non-deleted) · `insert(REPLACE)` · `update()` · `softDeleteById()` · `deleteById()`

> `ProductoDao` safely keeps `REPLACE` — `productos` has no inbound FK CASCADE references.

**Indexes:** `productos(name)`, `productos(updated_at)`.

**Domain model:** `Producto.kt` — fields match Room columns 1:1.  
**DTO:** `ProductoDto.kt` — snake_case field names for Supabase.  
**Repository:** `ProductoRepository` interface + `ProductoRepositoryImpl`.

> `kind` (glyph key: bottle/bag/box/can/jar/block) from the design is intentionally omitted — not surfaced in the create form. Can be added in a later phase if needed.

---

## Implemented tables (Phase 4)

---

### `pedidos` *(Room v9 — Phase 4)*

A delivery order. Belongs to a `clientes` row.

| Column | Supabase type | Nullable | Notes |
|--------|---------------|----------|-------|
| `id` | `uuid` PK | — | |
| `cliente_id` | `uuid` FK → `clientes.id` | — | `ON DELETE CASCADE` |
| `status` | `text` | — | `pending` \| `partial` \| `paid` |
| `total` | `float8` | — | Sum of line items |
| `paid` | `float8` | — | Amount paid so far; default `0` |
| `notes` | `text` | ✓ | Optional delivery note |
| `created_at` | `bigint` | — | Epoch ms |
| `paid_at` | `bigint` | ✓ | Epoch ms; set on every payment (partial or full) — always reflects the most recent payment date |
| `is_saldo_extra` | `boolean` | — | `true` for manual balance entries (Saldo Extra) — no line items; default `false` *(added v10)* |
| `updated_at` | `bigint` | — | Epoch ms; set by `moddatetime` trigger. **Used as delta-sync cursor.** *(added v16)* |
| `is_deleted` | `boolean` | — | Default `false`; set to `true` on soft-delete *(added v17)* |

**DAO operations:** `getByCliente(clienteId)` flow (non-deleted) · `getByClienteWithLines(clienteId)` flow (non-deleted, `@Transaction`) · `getByIdFlow(id)` flow (non-deleted) · `getById(id)` (non-deleted) · `getAllUnpaid()` flow (non-deleted) · `getAll()` flow (non-deleted) · `insert(IGNORE)` · `updateStatus(id, status, paid, paidAt)` · `updateDate(id, createdAt)` · `softDeleteById(id)` · `deleteById(id)` · `markAllPaidForCliente(clienteId, paidAt)` (non-deleted only) · `unpaidForCliente(clienteId)` (the rows `markAllPaidForCliente` is about to settle — same predicate, read first so each can be enqueued for sync)

**Suggested indexes:** `pedidos(cliente_id)`, `pedidos(status)`, `pedidos(created_at DESC)`, `pedidos(updated_at)`.

---

### `detalle_pedido` *(Room v9 — Phase 4)*

Line items inside a `pedidos` row.

| Column | Supabase type | Nullable | Notes |
|--------|---------------|----------|-------|
| `id` | `uuid` PK | — | |
| `pedido_id` | `uuid` FK → `pedidos.id` | — | `ON DELETE CASCADE` |
| `producto_id` | `uuid` FK → `productos.id` | — | `ON DELETE RESTRICT` |
| `product_name` | `text` | — | **Denormalized snapshot** of `productos.name` at time of order — see note below |
| `quantity` | `int4` | — | |
| `unit_price` | `float8` | — | Snapshot of price at time of order |
| `catalog_price` | `float8` | — | Catalogue price at time of order — used to show amber "price modified" indicator |
| `notes` | `text` | ✓ | Per-item note |

**Suggested index:** `detalle_pedido(pedido_id)`.

> **Denormalized `product_name`:** `unit_price` and `product_name` are both snapshots — they must not reference live `productos` data so historical orders remain accurate after a price or name change. `product_name` also enables the expandable product list in `PedidoRow` without joining the catalogue, and it simplifies Supabase cloud sync (no cross-table join needed to render order history).

---

### `pagos` *(Room v18 — payment ledger)*

An immutable record of one payment event against a `pedidos` row. Introduced because `pedidos.paid`/`pedidos.paid_at` only ever store a single running total and the date of the *most recent* payment — there was no way to reconstruct "the client paid Bs. 20 on June 1 and Bs. 15 on June 10" from that alone. `pagos` fixes this by recording every payment as its own row, never updated or deleted after insert.

| Column | Room type | Supabase type | Nullable | Notes |
|--------|-----------|---------------|----------|-------|
| `id` | `String` PK | `uuid` PK | — | |
| `pedido_id` | `String` FK → `pedidos.id` | `uuid` FK → `pedidos.id` | — | `ON DELETE CASCADE` |
| `amount` | `Double` | `float8` | — | Amount paid in this single event |
| `paid_at` | `Long` | `bigint` | — | Epoch ms; includes time-of-day, since more than one payment can be made on the same date |

**DAO operations:** `getByPedidoFlow(pedidoId)` flow, `getByPedido(pedidoId)` (suspend list, used by the sync push path) · `getByClienteFlow(clienteId)` flow (joins `pedidos` on `pedidoId` to scope by client, used by the report) · `insert(pago)` · `insertAll(pagos)` (sync pull) · `deleteByPedido(pedidoId)` (sync pull re-fetch)

> **No `updated_at`/`is_deleted` columns, no independent sync entity.** A row is created once and never touched again, so its own `paid_at` already serves as the natural "this is new" signal, and Postgres `ON DELETE CASCADE` from `pedidos` already removes a pedido's payments if it's ever hard-deleted. Rather than invent a whole new independently-synced entity (its own `EntityType`, syncer, staleness threshold — with its own FK-ordering hazard, since a payment for a pedido not yet pulled locally would fail Room's FK constraint), `pagos` is synced **bundled with its parent pedido**, exactly like `detalle_pedido`: `PedidoSyncer` pulls a pedido's payments together with its line items, and the `QueueProcessor`'s `PEDIDO` push branch deletes-and-reinserts the pedido's full local payment list on every push. This is slightly more bandwidth than a dedicated delta-synced ledger would use, but for the handful of payments a single pedido typically accumulates it's negligible, and it inherits `detalle_pedido`'s already-proven ordering guarantees for free instead of introducing a new one.
>
> **Every pedido creation with an initial payment gets an initial `pagos` row automatically.** `PedidoRepositoryImpl.create()` inserts one when `pedido.paid > 0` (covers "pay in full/partially at order time" from `CreacionPedidoScreen`). Payments made later through `DetallePedidoScreen` ("Marcar pagado" / "Pago parcial") go through `RegistrarPagoUseCase`, which always inserts a `pagos` row alongside the `pedidos.paid`/`status` update.
>
> **`EditarPedidoScreen` edits log the delta, not the new total.** `EditarPedidoViewModel.onSave()` passes the pedido's *new* absolute `paid` value (the `PagoSheet` there is pre-filled with the current status/amount and lets the user pick a new one), not an incremental amount. `PedidoRepositoryImpl.updateLines` reads the pedido's `paid` *before* applying the update, computes `delta = newPaid - oldPaid`, and inserts a `pagos` row for `delta` only when `delta > 0` (dated with the same `paidAt` the caller already computes for the edit). A decrease (correction downward, e.g. fixing a typo of 80→50) inserts nothing — there's no "negative payment" concept in an insert-only ledger, and nothing needs undoing since the erroneous row was never created in the first place.
>
> **Residual limitation:** this can't distinguish "the client paid more money right now" from "I mis-entered the amount earlier and I'm correcting it, no new money changed hands today." Both look identical to `updateLines` (paid went up during a save), so both get a `pagos` row dated *today*. For a genuine new payment this is exactly right; for a backdated correction, the ledger will show two payments on two different dates when in reality there was one payment on an earlier date that got corrected later — the *total* stays accurate, but the per-payment date breakdown can be wrong. Telling these apart would require the edit UI to explicitly ask which one is meant, which hasn't been built.

---

### `saldo_extra` *(implemented as flag on `pedidos` — Room v10)*

Saldo Extra entries are stored directly in the `pedidos` table with `isSaldoExtra = true` and no corresponding `detalle_pedido` rows. No separate table is needed.

- `notes` on the pedido stores the description.
- `total` stores the amount; `paid = 0`, `status = PENDING`.
- `PedidoRow` renders these with an amber Tag icon and a "Manual" badge instead of the normal PayChip.

---

### `umbrales` *(Room v19 — Phase 12)*

Singleton config row — decides when a client's status becomes `CRITICO`. Previously local-only (`SharedPreferences` via `UmbralesManager`), which meant every device/user had its own thresholds with no way to keep them consistent across a team. Now synced like every other shared table, editable only by `SUPERUSUARIO`.

| Column | Room type | Supabase type | Nullable | Notes |
|--------|-----------|---------------|----------|-------|
| `id` | `String` PK | `text` PK | — | Always `"global"` — there is exactly one row (`UmbralesEntity.SINGLETON_ID`) |
| `montoMaximo` | `Double` | `float8` | — | Balance threshold; default `200.0` |
| `diasMaximos` | `Int` | `int4` | — | Days-unpaid threshold; default `30` |
| `updatedAt` | `Long` | `bigint` | — | Epoch ms; set by `set_updated_at_ms()` trigger. **Used as delta-sync cursor.** |

**DAO operations:** `getFlow()` flow (single row) · `get()` (suspend) · `insert(REPLACE)` (safe — no inbound FK references)

**No `isDeleted` column and no delete path.** The row is never deleted, only updated — there's no "remove the thresholds" concept in the UI, so `UmbralesRepositoryImpl` never enqueues a `DELETE` sync operation and `QueueProcessor`/`UmbralesSyncer` have no delete branch for this entity (same reasoning `pagos` uses to skip `isDeleted`, see above).

**Sync notes:** `UmbralesSyncer` always fetches the single row by `id = 'global'` rather than paging or filtering by `since` — there's nothing to page. `DataSynchronizer` treats it as master data (`THRESHOLD_MASTER_MS`, 2h staleness). `UmbralesRepositoryImpl.save()` writes to Room and enqueues an `UPSERT` under `EntityType.UMBRALES`, exactly like every other entity.

**Domain model:** `Umbrales.kt` — unchanged, just `montoMaximo`/`diasMaximos` (sync fields stay out of the domain model, same as `Producto`).

---

### `sync_operations` *(Room v14 + v15 — Phase 9b)*

Write-queue table. Every user mutation (create / update / delete) appends a row here immediately after the local Room write. `QueueProcessor` reads this table and pushes the corresponding Supabase call in the background. Rows are deleted only after a successful push — never before.

| Column | Room type | Notes |
|--------|-----------|-------|
| `id` | `Long` PK autoGenerate | Monotonically increasing — used as the flush trigger via `MAX(id)` |
| `entityType` | `String` | `MERCADO` · `CLIENTE` · `PRODUCTO` · `PEDIDO` |
| `entityId` | `String` | UUID of the affected entity |
| `operation` | `String` | `UPSERT` · `DELETE` |
| `createdAt` | `Long` | Epoch ms — used for deduplication ordering (latest UPSERT wins) |
| `retryCount` | `Int` | Incremented on each failed push attempt; used for observability and drives the `SyncIconState.ERROR` state |
| `entityLabel` | `String` | Human-readable label shown in the Sincronización screen (e.g. `"Bs. 120,00"` for a pedido, `"Mercado de Coche"` for a mercado). Added in MIGRATION_14_15. |

**DAO operations:** `enqueue(entity)` · `getPending()` · `observeAll(): Flow<List>` · `observeLatestEnqueuedId(): Flow<Long>` · `delete(id)` · `incrementRetry(id)`

**Migrations:**
- `MIGRATION_13_14` — creates the table (all columns except `entityLabel`)
- `MIGRATION_14_15` — `ALTER TABLE sync_operations ADD COLUMN entityLabel TEXT NOT NULL DEFAULT ''`

**Not synced to Supabase** — this table is device-local only. It is never read-synced from the server.

---

## Relationship diagram

```
users (standalone — auth layer)
  No FK references from any other table. Editing user info
  never affects mercados, clientes, pedidos, or productos.

mercados
└── clientes  (FK mercadoId → mercados.id  ON DELETE CASCADE)
    ├── pedidos  (FK clienteId → clientes.id  ON DELETE CASCADE)
    │   ├── detalle_pedido  (FK pedidoId → pedidos.id  ON DELETE CASCADE)
    │   └── pagos  (FK pedidoId → pedidos.id  ON DELETE CASCADE)
    └── saldo_extra  (stored as pedido rows with isSaldoExtra = true)

productos (standalone catalogue)

umbrales (standalone singleton config — no FK, always exactly one row)
```

> **Data integrity — three critical rules:**
>
> **1. Never use `fallbackToDestructiveMigration`.** It was removed from `DatabaseModule`. Room will now throw an `IllegalStateException` if a migration is missing, forcing an explicit migration to be written. The destructive fallback drops *all* tables (not just changed ones) when a schema version bump has no matching migration — irreversible data loss.
>
> **2. Never use `@Insert(onConflict = REPLACE)` on any parent table that has child tables with `ON DELETE CASCADE`.** Room 2.7+ enables `PRAGMA foreign_keys = ON` by default. With FK enforcement active, `INSERT OR REPLACE` first DELETEs the existing row — firing the cascade — then inserts the new one. Updating a mercado this way would silently wipe all its clientes and pedidos. The safe upsert pattern for all parent DAOs (`mercados`, `clientes`) is:
> ```kotlin
> // DAO
> @Insert(onConflict = OnConflictStrategy.IGNORE)
> suspend fun insert(entity: Entity): Long   // returns -1 on PK conflict
>
> @Update
> suspend fun update(entity: Entity)
>
> // Repository
> override suspend fun save(item: DomainModel) {
>     val entity = Mapper.toEntity(item)
>     if (dao.insert(entity) == -1L) dao.update(entity)
> }
> ```
> `PedidoDao` and `DetallePedidoDao` also use `IGNORE` (purely defensive — these are insert-only, IDs are fresh UUIDs). `UserDao` and `ProductoDao` may safely keep `REPLACE` because neither table has inbound FK CASCADE references.
>
> **3. Never delete an exported schema from `app/schemas/`.** They are the only record of what each version looked like, and `MigrationTestHelper` needs the JSON to create a database at that version. `16.json` was never exported and its absence is permanent — no migration test can start at v16. Commit the new `<version>.json` in the same change that bumps `DATABASE_VERSION`.

---

## Supabase setup checklist (Phase 9)

SQL files live in `docs/sql/`. Run them in the Supabase SQL editor in this order:

- [ ] `docs/sql/schema.sql` — creates all tables with FK constraints and indexes
- [ ] `docs/sql/rls.sql` — enables RLS and adds policies per table
- [ ] `docs/sql/storage.sql` — creates `mercado-photos`, `cliente-photos`, `producto-photos`, `user-photos` buckets + policies
- [ ] Fill in `local.properties` with real Supabase URLs and keys (staging + production)
- [ ] `biometric_enabled_at` column intentionally absent from `users` table — device-local only ✅
- [ ] Wire `SyncerRegistry` per `docs/features/mercados-supabase-todos.md` (Phase 10)

## Image storage architecture

`photoUrl` fields on `mercados`, `clientes`, `productos`, and `users` store **Supabase Storage public URLs** (`https://…`).

**Upload flow (on save):** Each create/edit ViewModel calls `StorageService.uploadPhoto(bucket, entityId, localUri)` before writing to Room. Storage path is always `{entityId}/photo.jpg` within the entity's bucket; `upsert = true` so re-editing replaces the file without leaving orphans. If the device is offline the upload fails silently and the local `content://` URI is stored instead — the image still works on that device; cross-device display degrades gracefully until the entity is re-saved while online.

**Display (everywhere):** `PhotoThumbnail` uses Coil 3 (`coil-compose` + `coil-network-okhttp`). Coil handles both local `content://` URIs and remote `https://` URLs transparently. Its built-in disk cache means each remote image is downloaded only once per device; subsequent views are served from disk without a network round-trip.

**Bucket → entity mapping:**

| Bucket | Entity |
|--------|--------|
| `mercado-photos` | `mercados` |
| `cliente-photos` | `clientes` |
| `producto-photos` | `productos` |
| `user-photos` | `users` |

---

## Staging environment changes (Phase 10 — delta sync)

Run the following SQL in the **staging** Supabase SQL editor after the base schema is applied.

### 1. Enable `moddatetime` extension (one-time)

```sql
CREATE EXTENSION IF NOT EXISTS moddatetime;
```

### 2. Add `updated_at` column to each table

```sql
-- mercados
ALTER TABLE mercados ADD COLUMN IF NOT EXISTS updated_at BIGINT NOT NULL DEFAULT 0;
UPDATE mercados SET updated_at = created_at WHERE updated_at = 0;

-- clientes
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS updated_at BIGINT NOT NULL DEFAULT 0;
UPDATE clientes SET updated_at = created_at WHERE updated_at = 0;

-- productos
ALTER TABLE productos ADD COLUMN IF NOT EXISTS updated_at BIGINT NOT NULL DEFAULT 0;
UPDATE productos SET updated_at = created_at WHERE updated_at = 0;

-- pedidos
ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS updated_at BIGINT NOT NULL DEFAULT 0;
UPDATE pedidos SET updated_at = created_at WHERE updated_at = 0;
```

> **Note:** `detalle_pedido` intentionally has no `updated_at`. Line items are treated as a block: when a `pedidos` row changes, all its `detalle_pedido` rows are deleted and re-fetched.

### 3. Auto-update `updated_at` on every row change

Because `updated_at` stores epoch milliseconds (not a Postgres `timestamptz`), we use a custom trigger function instead of `moddatetime`:

```sql
CREATE OR REPLACE FUNCTION set_updated_at_ms()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at := (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT;
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_mercados_updated_at
  BEFORE UPDATE ON mercados
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

CREATE TRIGGER trg_clientes_updated_at
  BEFORE UPDATE ON clientes
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

CREATE TRIGGER trg_productos_updated_at
  BEFORE UPDATE ON productos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

CREATE TRIGGER trg_pedidos_updated_at
  BEFORE UPDATE ON pedidos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();
```

### 4. Add indexes for delta-sync queries

```sql
CREATE INDEX IF NOT EXISTS idx_mercados_updated_at  ON mercados (updated_at);
CREATE INDEX IF NOT EXISTS idx_clientes_updated_at  ON clientes (updated_at);
CREATE INDEX IF NOT EXISTS idx_productos_updated_at ON productos (updated_at);
CREATE INDEX IF NOT EXISTS idx_pedidos_updated_at   ON pedidos  (updated_at);
```

### 5. Add `is_deleted` column to each table (Phase 11)

```sql
ALTER TABLE mercados  ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE clientes  ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE productos ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE pedidos   ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false;
```

The existing `set_updated_at_ms()` trigger fires on any UPDATE (including `SET is_deleted = true`), so `updated_at` is bumped automatically — deleted rows appear in the next delta sync on other devices with `is_deleted = true`. `mercados` and `clientes` tombstones are **soft**-deleted in Room (not hard-deleted), so the row survives to satisfy the foreign keys of any children that are still live; `productos` and `pedidos` have no live children to protect and are removed outright.

### Room migration reference

All migrations live in `AppDatabase.kt` as top-level `val`s and are collected into a single
`ALL_MIGRATIONS` array there. `DatabaseModule` builds the database with `addMigrations(*ALL_MIGRATIONS)`
and `MigrationTest` (`app/src/androidTest/.../data/local/`) replays the same array, so the tested
chain and the shipped chain cannot drift apart — never inline migrations into `addMigrations(...)`
again. The declared version is `const val DATABASE_VERSION`, which a guard test uses to assert the
array covers every consecutive pair from 4 upward.

**Adding a migration:** write the `MIGRATION_X_Y` val, add it to `ALL_MIGRATIONS`, bump
`DATABASE_VERSION`, commit the newly exported `app/schemas/<Y>.json`, then run
`./gradlew :app:connectedStagingDebugAndroidTest` (needs a device or emulator; it is not in CI).
See `docs/features/testing.md`.

`MIGRATION_15_16` in `AppDatabase.kt` adds `updatedAt INTEGER NOT NULL DEFAULT 0` to the four Room entity tables and backfills `updatedAt = createdAt`. The Supabase column is named `updated_at` (snake_case); the DTO field is `@SerialName("updated_at") val updatedAt: Long`.

`MIGRATION_16_17` in `AppDatabase.kt` adds `isDeleted INTEGER NOT NULL DEFAULT 0` to `mercados`, `clientes`, `productos`, and `pedidos`. The Supabase column is named `is_deleted` (snake_case); the DTO field is `@SerialName("is_deleted") val isDeleted: Boolean = false`.

`MIGRATION_17_18` in `AppDatabase.kt` creates the `pagos` table (`id`, `pedidoId` FK CASCADE, `amount`, `paidAt`) with an index on `pedidoId`. No backfill needed — it's a brand-new table with no historical data (past payments were never recorded individually, only as the cumulative `pedidos.paid`/`paid_at`).

`MIGRATION_18_19` in `AppDatabase.kt` creates the `umbrales` table (`id`, `montoMaximo`, `diasMaximos`, `updatedAt`) and seeds it with a single row (`id = 'global'`, `montoMaximo = 200.0`, `diasMaximos = 30`) matching the old `SharedPreferences` defaults, so existing installs keep their current thresholds until the next sync pulls the real (possibly superuser-edited) values from Supabase.

### 6. `umbrales` table (Phase 12 — was local-only, now synced)

Existing environments predate `docs/sql/schema.sql`'s `umbrales` table, so run this once against each already-provisioned project (staging now, production whenever it's created and hasn't had `schema.sql` re-run):

```sql
CREATE TABLE IF NOT EXISTS umbrales (
    id            text    PRIMARY KEY DEFAULT 'global',
    monto_maximo  float8  NOT NULL DEFAULT 200,
    dias_maximos  int4    NOT NULL DEFAULT 30,
    updated_at    bigint  NOT NULL DEFAULT 0
);

ALTER TABLE umbrales ENABLE ROW LEVEL SECURITY;

CREATE TRIGGER trg_umbrales_updated_at
    BEFORE UPDATE ON umbrales
    FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

INSERT INTO umbrales (id, monto_maximo, dias_maximos, updated_at)
VALUES ('global', 200, 30, 0)
ON CONFLICT (id) DO NOTHING;

CREATE POLICY "umbrales_select_authenticated"
    ON umbrales FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "umbrales_insert_superusuario"
    ON umbrales FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() = 'SUPERUSUARIO');

CREATE POLICY "umbrales_update_superusuario"
    ON umbrales FOR UPDATE
    TO authenticated
    USING (get_my_role() = 'SUPERUSUARIO')
    WITH CHECK (get_my_role() = 'SUPERUSUARIO');
```

This assumes `set_updated_at_ms()` and `get_my_role()` already exist (they do on staging — both were created by the original `schema.sql`/`rls.sql` run). If a value was already saved on-device before this migration ships, opening **Mi Perfil → Umbrales de estado → Guardar umbrales** once (as a `SUPERUSUARIO`, while online) pushes it up and populates the row for every other device.

### 7. Fire the `updated_at` trigger on INSERT too (fixes the sync FK failures)

The Phase 10 triggers were `BEFORE UPDATE` only, while `updated_at` defaults to `0`. A freshly
**inserted** row therefore kept `updated_at = 0` and was invisible to every
`updated_at > since` delta. A newly created cliente stayed invisible while its pedido became visible
the moment anything touched it — the child arrived with no parent and Room raised
`FOREIGN KEY constraint failed`, which aborted the whole pull and surfaced as "no se pudo actualizar".

Run in **staging first**, then production:

```sql
DROP TRIGGER IF EXISTS trg_mercados_updated_at  ON mercados;
DROP TRIGGER IF EXISTS trg_clientes_updated_at  ON clientes;
DROP TRIGGER IF EXISTS trg_productos_updated_at ON productos;
DROP TRIGGER IF EXISTS trg_pedidos_updated_at   ON pedidos;
DROP TRIGGER IF EXISTS trg_umbrales_updated_at  ON umbrales;

CREATE TRIGGER trg_mercados_updated_at
  BEFORE INSERT OR UPDATE ON mercados
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

CREATE TRIGGER trg_clientes_updated_at
  BEFORE INSERT OR UPDATE ON clientes
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

CREATE TRIGGER trg_productos_updated_at
  BEFORE INSERT OR UPDATE ON productos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

CREATE TRIGGER trg_pedidos_updated_at
  BEFORE INSERT OR UPDATE ON pedidos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

CREATE TRIGGER trg_umbrales_updated_at
  BEFORE INSERT OR UPDATE ON umbrales
  FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();

-- Backfill rows inserted while the trigger was UPDATE-only. A no-op UPDATE now
-- fires the trigger and fills updated_at.
UPDATE mercados  SET updated_at = updated_at WHERE updated_at = 0;
UPDATE clientes  SET updated_at = updated_at WHERE updated_at = 0;
UPDATE productos SET updated_at = updated_at WHERE updated_at = 0;
UPDATE pedidos   SET updated_at = updated_at WHERE updated_at = 0;
```

The backfill makes every device pull those rows once on its next delta — a correct, one-off cost.

Verify all five now fire on insert (expect `INSERT OR UPDATE` for each):

```sql
SELECT event_object_table, string_agg(event_manipulation, ' OR ' ORDER BY event_manipulation)
FROM information_schema.triggers
WHERE trigger_name LIKE 'trg_%_updated_at'
GROUP BY event_object_table;
```

**Diagnostic — find the rows that were crashing the app.** These are live children whose parent is
soft-deleted; the app now recovers them automatically, so this is only for visibility:

```sql
SELECT p.id AS pedido_id, p.cliente_id
FROM pedidos p JOIN clientes c ON c.id = p.cliente_id
WHERE p.is_deleted = false AND c.is_deleted = true;

SELECT c.id AS cliente_id, c.mercado_id
FROM clientes c JOIN mercados m ON m.id = c.mercado_id
WHERE c.is_deleted = false AND m.is_deleted = true;
```

### 8. Cascade the soft-delete to children

Deleting is a flag flip, not a row delete, so the `ON DELETE CASCADE` declared on the foreign keys
never fires. `QueueProcessor.delete` updates only the target row, so a deleted mercado left its
clientes and their pedidos live — still surfacing in **Búsqueda** and **Reporte**, which read
clientes without joining to mercados.

Doing this in Postgres rather than the client means it holds no matter what flips the flag: any
device, an Edge Function, or a manual dashboard edit. Run in **staging first**, then production:

```sql
CREATE OR REPLACE FUNCTION cascade_soft_delete_clientes()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.is_deleted AND NOT OLD.is_deleted THEN
        UPDATE clientes SET is_deleted = true
        WHERE mercado_id = NEW.id AND is_deleted = false;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_mercados_cascade_soft_delete ON mercados;
CREATE TRIGGER trg_mercados_cascade_soft_delete
    AFTER UPDATE ON mercados
    FOR EACH ROW EXECUTE FUNCTION cascade_soft_delete_clientes();

CREATE OR REPLACE FUNCTION cascade_soft_delete_pedidos()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.is_deleted AND NOT OLD.is_deleted THEN
        UPDATE pedidos SET is_deleted = true
        WHERE cliente_id = NEW.id AND is_deleted = false;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_clientes_cascade_soft_delete ON clientes;
CREATE TRIGGER trg_clientes_cascade_soft_delete
    AFTER UPDATE ON clientes
    FOR EACH ROW EXECUTE FUNCTION cascade_soft_delete_pedidos();
```

The two **chain**: flipping a mercado flips its clientes, which fires the clientes trigger and flips
their pedidos. `set_updated_at_ms()` fires on each of those UPDATEs, so every affected row becomes
visible to the next delta sync on every device. That propagation is the actual mechanism — the local
cascade in `MercadoRepositoryImpl.delete` / `ClienteRepositoryImpl.delete` only spares the
originating device the wait, and the app still enqueues exactly **one** sync op (the parent's) rather
than one per child.

> **This is one-way.** Restoring a parent does not restore its children — the guard only matches a
> `false → true` transition. To bring a whole subtree back by hand:
>
> ```sql
> UPDATE mercados SET is_deleted = false WHERE id = '<mercado-id>';
> UPDATE clientes SET is_deleted = false WHERE mercado_id = '<mercado-id>';
> UPDATE pedidos  SET is_deleted = false
> WHERE cliente_id IN (SELECT id FROM clientes WHERE mercado_id = '<mercado-id>');
> ```
>
> Run them in that order and every device picks the rows back up on its next delta.

This does **not** retro-fix subtrees deleted before the trigger existed; those children are still
live. Find them with the two diagnostic queries in §7 above, and soft-delete them with the same
`UPDATE ... SET is_deleted = true` shape if you want them cleaned up.
