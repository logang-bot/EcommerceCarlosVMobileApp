# Database Schema

Room version: **12**. Supabase integration: Phase 9.

All primary keys are client-generated UUIDs (`String`). All timestamp columns store **epoch milliseconds** (`Long` in Room, `bigint` in Supabase). Nullable columns are marked `?`.

---

## Current tables (Room v12)

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

**DAO operations:** `getAll()` flow (ordered `name ASC`) · `getById()` (suspend) · `getByIdFlow()` (Flow — reactive) · `insert(IGNORE)` returning `Long` · `update()` · `deleteById()`

**Supabase notes:** Consider a `geography(Point)` column as an alternative to separate lat/lng if you want PostGIS distance queries later. Suggested index: `mercados(name)`.

---

### `clientes` *(Room v8 → v10 — Phase 3 + 7)*

Belongs to a `mercados` row. Represents an individual customer at a market stall.

| Column | Room type | Supabase type | Nullable | Notes |
|--------|-----------|---------------|----------|-------|
| `id` | `String` PK | `uuid` PK | — | |
| `mercadoId` | `String` FK | `uuid` FK → `mercados.id` | — | `ON DELETE CASCADE` |
| `name` | `String` | `text` | — | |
| `description` | `String` | `text` | — | Stall description e.g. "Puesto 14 · verduras" |
| `photoUrl` | `String?` | `text` | ✓ | Supabase Storage URL |
| `phones` | `String` | `text` | — | Pipe-separated list: `"0414-123\|0424-456"`. Empty string if none. |
| `mapsUrl` | `String?` | `text` | ✓ | URL that opens the device map app — no lat/lng stored |
| `isBlacklisted` | `Boolean` | `boolean` | — | Default `false` |
| `blacklistReason` | `String?` | `text` | ✓ | Set when `isBlacklisted = true` |
| `blacklistedAt` | `Long?` | `bigint` | ✓ | Epoch ms |
| `blacklistBalance` | `Double` | `float8` | — | Owed balance recorded at time of blacklisting; default `0.0` *(added v8)* |
| `blacklistIsManualAmount` | `Boolean` | `boolean` | — | `true` when amount was entered manually (MANUAL mode); `false` = AUTO *(added v12)* |
| `createdAt` | `Long` | `bigint` | — | Epoch ms |

**DAO operations:** `getByMercado(mercadoId)` flow (non-blacklisted, name ASC) · `getAll()` flow (non-blacklisted) · `getBlacklisted()` flow (blacklisted only, `blacklistedAt DESC`) · `getById()` · `insert(IGNORE)` returning `Long` · `update()` · `deleteById()` · `blacklist(id, reason, balance, at, isManualAmount)` · `unblacklist(id)` (resets all blacklist fields incl. `blacklistIsManualAmount`)

**Indexes:** `clientes(mercadoId)`, `clientes(name)`, `clientes(isBlacklisted)`.

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
| `isActive` | `Boolean` | `boolean` | — | Default `true`; soft-delete |
| `createdAt` | `Long` | `bigint` | — | Epoch ms |

**DAO operations:** `getAll()` flow (active only, name ASC) · `getById()` · `insert(REPLACE)` · `update()` · `deleteById()`

> `ProductoDao` safely keeps `REPLACE` — `productos` has no inbound FK CASCADE references.

**Index:** `productos(name)`.

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

**DAO operations:** `getByCliente(clienteId)` flow · `getByClienteWithLines(clienteId)` flow (`@Transaction`, returns `PedidoWithLines`) · `getByIdFlow(id)` flow · `getById(id)` · `getAllUnpaid()` flow · `insert(IGNORE)` · `updateStatus(id, status, paid, paidAt)` · `updateDate(id, createdAt)` · `deleteById(id)`

**Suggested indexes:** `pedidos(cliente_id)`, `pedidos(status)`, `pedidos(created_at DESC)`.

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

### `saldo_extra` *(implemented as flag on `pedidos` — Room v10)*

Saldo Extra entries are stored directly in the `pedidos` table with `isSaldoExtra = true` and no corresponding `detalle_pedido` rows. No separate table is needed.

- `notes` on the pedido stores the description.
- `total` stores the amount; `paid = 0`, `status = PENDING`.
- `PedidoRow` renders these with an amber Tag icon and a "Manual" badge instead of the normal PayChip.

---

## Relationship diagram

```
users (standalone — auth layer)
  No FK references from any other table. Editing user info
  never affects mercados, clientes, pedidos, or productos.

mercados
└── clientes  (FK mercadoId → mercados.id  ON DELETE CASCADE)
    ├── pedidos  (FK clienteId → clientes.id  ON DELETE CASCADE)
    │   └── detalle_pedido  (FK pedidoId → pedidos.id  ON DELETE CASCADE)
    └── saldo_extra  (stored as pedido rows with isSaldoExtra = true)

productos (standalone catalogue)
```

> **Data integrity — two critical rules:**
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

---

## Supabase setup checklist (Phase 9)

- [ ] Enable UUID extension: `CREATE EXTENSION IF NOT EXISTS "pgcrypto";`
- [ ] Create tables in dependency order: `users` → `mercados` → `clientes` → `productos` → `pedidos` → `detalle_pedido` → `saldo_extra`
- [ ] Add `ON DELETE CASCADE` on all FK columns listed above
- [ ] Enable Row Level Security on all tables
- [ ] RLS policies:
  - `users`: SELECT for authenticated; INSERT/UPDATE/DELETE restricted to `SUPERUSUARIO` role
  - all others: SELECT/INSERT/UPDATE/DELETE for authenticated users (shared resource)
- [ ] Create Supabase Storage buckets: `mercado-photos`, `cliente-photos`, `producto-photos`
- [ ] Add `biometricEnabledAt` to `local.properties` exclusion — this column must NOT exist in Supabase
- [ ] Wire `SyncerRegistry` per `docs/features/mercados-supabase-todos.md`
