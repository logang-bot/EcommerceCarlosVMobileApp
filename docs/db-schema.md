# Database Schema

Room version: **5**. Supabase integration: Phase 9.

All primary keys are client-generated UUIDs (`String`). All timestamp columns store **epoch milliseconds** (`Long` in Room, `bigint` in Supabase). Nullable columns are marked `?`.

---

## Current tables (Room v5)

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

**DAO operations:** `getAll()` flow (ordered `name ASC`) · `getById()` · `insert(REPLACE)` · `update()` · `deleteById()`

**Supabase notes:** Consider a `geography(Point)` column as an alternative to separate lat/lng if you want PostGIS distance queries later. Suggested index: `mercados(name)`.

---

## Planned tables (Phase 3+)

These are not yet implemented in Room. Defined here based on the app spec so the Supabase schema can be set up in advance.

---

### `clientes` *(Phase 3)*

Belongs to a `mercados` row. Represents an individual customer at a market stall.

| Column | Supabase type | Nullable | Notes |
|--------|---------------|----------|-------|
| `id` | `uuid` PK | — | |
| `mercado_id` | `uuid` FK → `mercados.id` | — | `ON DELETE CASCADE` |
| `name` | `text` | — | |
| `description` | `text` | — | Stall description e.g. "Puesto 14 · verduras" |
| `photo_url` | `text` | ✓ | Supabase Storage URL |
| `maps_url` | `text` | ✓ | Location within the mercado |
| `latitude` | `float8` | ✓ | Extracted from `maps_url` |
| `longitude` | `float8` | ✓ | Extracted from `maps_url` |
| `status` | `text` | — | Derived: `ok` \| `warn` \| `crit` — computed, not stored |
| `balance` | `float8` | — | Running total owed — computed from pedidos, not stored |
| `is_blacklisted` | `boolean` | — | Default `false` |
| `blacklist_reason` | `text` | ✓ | Set when `is_blacklisted = true` |
| `blacklisted_at` | `bigint` | ✓ | Epoch ms |
| `created_at` | `bigint` | — | Epoch ms |

**Suggested indexes:** `clientes(mercado_id)`, `clientes(name)`, `clientes(is_blacklisted)`.

> `status` and `balance` are computed in the app from the `pedidos` rows — do not store them as columns.

---

### `pedidos` *(Phase 4)*

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
| `paid_at` | `bigint` | ✓ | Epoch ms; set when `status = paid` |

**Suggested indexes:** `pedidos(cliente_id)`, `pedidos(status)`, `pedidos(created_at DESC)`.

---

### `detalle_pedido` *(Phase 4)*

Line items inside a `pedidos` row.

| Column | Supabase type | Nullable | Notes |
|--------|---------------|----------|-------|
| `id` | `uuid` PK | — | |
| `pedido_id` | `uuid` FK → `pedidos.id` | — | `ON DELETE CASCADE` |
| `producto_id` | `uuid` FK → `productos.id` | — | `ON DELETE RESTRICT` |
| `quantity` | `int4` | — | |
| `unit_price` | `float8` | — | Snapshot of price at time of order |
| `notes` | `text` | ✓ | Per-item note |

**Suggested index:** `detalle_pedido(pedido_id)`.

> `unit_price` is a snapshot — it must not reference the live `productos.price` so historical orders remain accurate after a price change.

---

### `productos` *(Phase 6)*

Global product catalogue, shared across all users.

| Column | Supabase type | Nullable | Notes |
|--------|---------------|----------|-------|
| `id` | `uuid` PK | — | |
| `name` | `text` | — | |
| `description` | `text` | ✓ | Variant, size, or note |
| `price` | `float8` | — | Current price |
| `kind` | `text` | — | Glyph key: `bottle` \| `bag` \| `box` \| `can` \| `jar` \| `block` |
| `photo_url` | `text` | ✓ | Supabase Storage URL |
| `is_active` | `boolean` | — | Default `true`; soft-delete |
| `created_at` | `bigint` | — | Epoch ms |

**Suggested index:** `productos(name)`.

---

### `saldo_extra` *(Phase 3)*

A manual balance entry attached to a `clientes` row (not linked to any specific pedido).

| Column | Supabase type | Nullable | Notes |
|--------|---------------|----------|-------|
| `id` | `uuid` PK | — | |
| `cliente_id` | `uuid` FK → `clientes.id` | — | `ON DELETE CASCADE` |
| `amount` | `float8` | — | Positive = debt added |
| `description` | `text` | — | Reason for the extra balance |
| `created_at` | `bigint` | — | Epoch ms |

**Suggested index:** `saldo_extra(cliente_id)`.

---

## Relationship diagram

```
users (standalone — auth layer)

mercados
└── clientes
    ├── pedidos
    │   └── detalle_pedido → productos
    └── saldo_extra

productos (standalone catalogue)
```

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
