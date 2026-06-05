# Feature: Mercados

## Status: ✅ Done (Phase 2 + 2f)

---

## Spec summary

Entry point after login (tab 0). Shows all Mercados alphabetically. Each row shows a grid-icon tile, the mercado name, active client count (stubbed at 0 until Phase 3), and an optional status dot when any client inside is in Warning or Critical state. A "Lista Negra" card button at the bottom navigates to the global blacklist. FAB creates a new Mercado.

Long-pressing a mercado row enters **selection mode**: the normal top bar is replaced by a contextual action bar showing a close button, "1 seleccionado", and an "Editar" pill that navigates to the edit form. Tapping any mercado while in selection mode navigates to its detail screen as normal; long-pressing again deselects.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Lista de Mercados | `HomeRoute` → tab 0 | `ui/screen/mercado/MercadosScreen.kt` |
| Detalle de Mercado | `DetalleMercadoRoute(mercadoId)` | `ui/screen/mercado/DetalleMercadoScreen.kt` |
| Crear / Editar Mercado | `CreateMercadoRoute(mercadoId?)` | `ui/screen/mercado/CreateMercadoScreen.kt` |

`CreateMercadoRoute(mercadoId = null)` → create mode. `CreateMercadoRoute(mercadoId = "…")` → edit mode (pre-fills name + address from Room).

---

## UI implementation notes

### MercadosScreen — normal state

- Large `PedidosTopBar` with title "Mercados" and subtitle "%d mercados"
- Top-bar actions: Search icon (→ `BusquedaRoute`), Notifications icon (stub), Profile avatar (→ `PerfilRoute`)
- `MercadoRow`: 44dp grid-icon tile (`surface3` bg, `border` inset, `GridView` icon) + name + "N clientes activos" subtitle + chevron
- Dividers start at `start = 78.dp` to clear the tile
- `ListaNegraButton` at the bottom: card style (54dp height, 14dp corners, `surfaceVariant` bg, 1dp border, 16dp inner padding)
- Extended FAB anchored above the nav bar, label "Mercado"

### MercadosScreen — selection mode (long-press)

- `ContextualActionBar` replaces `PedidosTopBar`:
  - `accentSoft` background, 56dp height, 1dp bottom divider
  - Close (`×`) button → clears selection
  - "1 seleccionado" text (bold)
  - **"Ver detalles"** pill button (info icon, accent background, 40dp height, 12dp corners) → `DetalleMercadoRoute(selectedId)`
- Selected `MercadoRow`:
  - `accentSoft` background (via `drawBehind`)
  - 3dp accent-color left border (via `drawBehind` rect at x=0)
  - Circular check badge (24dp, accent fill, white check icon) replaces chevron
- `SelectionHint` row: info icon + "Mantén pulsado un mercado para ver sus detalles"
- FAB and Lista Negra button are hidden in selection mode

### CreateMercadoScreen

- `PedidosTopBar` with back arrow + Close (×) icon action; title "Nuevo mercado" / "Editar mercado"
- `isEditing` flag flows from `CreateMercadoFormState` (set by ViewModel when `mercadoId != null`)
- **Fields** (gap 18dp, 8dp top / 20dp horizontal / 24dp bottom padding):
  - "Nombre del mercado" (required — asterisk rendered in accent color); word-capitalised; `ImeAction.Next`
  - "Dirección" (optional, multiline min 2 / max 4 lines, sentence-capitalised; `ImeAction.Done`)
  - **Ubicación** — `MapsLinkField` (editable); pin icon, inline text input + "Pegar" chip (reads clipboard) / "Abrir" chip (opens Maps app) when filled. Coordinates extracted from the URL via `extractMapsCoordinates()` and saved to `latitude`/`longitude` on the entity.
  - **Photo picker** (160dp height, 16dp corners):
    - Create mode: `surface2` bg + `border2` inset, centered camera icon + "Agregar foto" label
    - Edit mode: `surface3` bg + `border` inset, `GridView` icon centre, accent camera button (34×34, 11dp corners) bottom-right overlay
    - Photo upload wired in Phase 9 (Supabase storage)
- **Bottom bar**: `HorizontalDivider` + full-width 52dp button labeled "Guardar mercado" / "Guardar cambios"
- Validation: empty name sets `nameError = true` and blocks save
- Delete button **removed** from this screen — moved to `DetalleMercadoScreen`

---

## Data layer

### Domain model

```kotlin
data class Mercado(
    val id: String,
    val name: String,
    val address: String,
    val photoUrl: String? = null,
    val mapsUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long,
)
```

### Room entity — `MercadoEntity`

| Column | Type | Notes |
|--------|------|-------|
| `id` | `String` (PK) | UUID generated client-side |
| `name` | `String` | |
| `address` | `String` | |
| `photoUrl` | `String?` | |
| `mapsUrl` | `String?` | Raw Google Maps URL pasted by user |
| `latitude` | `Double?` | Extracted from `mapsUrl` by `extractMapsCoordinates()` |
| `longitude` | `Double?` | Extracted from `mapsUrl` by `extractMapsCoordinates()` |
| `createdAt` | `Long` | Epoch millis |

Room schema version: **5** (migrated from 4 via `MIGRATION_4_5` — `ALTER TABLE mercados ADD COLUMN` for each new field).

### DAO — `MercadoDao`

| Method | Returns |
|--------|---------|
| `getAll()` | `Flow<List<MercadoEntity>>` ordered by `name ASC` |
| `getById(id)` | `MercadoEntity?` |
| `insert(entity)` | `Unit` — `OnConflictStrategy.REPLACE` (handles both create and update) |
| `update(entity)` | `Unit` |
| `deleteById(id)` | `Unit` |

### Repository — `MercadoRepository`

```kotlin
fun getAll(): Flow<List<Mercado>>
suspend fun getById(id: String): Mercado?
suspend fun save(mercado: Mercado)   // insert with REPLACE — covers create + update
suspend fun delete(id: String)
```

### Remote DTO — `MercadoDto`

```kotlin
@Serializable
data class MercadoDto(
    val id: String,
    val name: String,
    val address: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("maps_url") val mapsUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: Long,
)
```

---

## Files

| File | Description |
|------|-------------|
| `domain/model/Mercado.kt` | Domain model |
| `domain/repository/MercadoRepository.kt` | Repository interface |
| `data/local/entity/MercadoEntity.kt` | Room entity |
| `data/local/dao/MercadoDao.kt` | DAO |
| `data/mapper/MercadoMapper.kt` | Entity ↔ Domain mapper |
| `data/remote/dto/MercadoDto.kt` | Supabase DTO (`@SerialName` for snake_case) |
| `data/repository/impl/MercadoRepositoryImpl.kt` | Repository implementation |
| `ui/screen/mercado/MercadosUiState.kt` | `mercados`, `isLoading`, `currentUserInitials`, `selectedMercadoId` |
| `ui/screen/mercado/MercadosViewModel.kt` | Combines mercado list + session + selection state; `onMercadoLongPress()`, `clearSelection()` |
| `ui/screen/mercado/MercadosScreen.kt` | List, contextual action bar, selection visual state |
| `ui/screen/mercado/DetalleMercadoUiState.kt` | `mercado`, `isLoading` |
| `ui/screen/mercado/DetalleMercadoViewModel.kt` | Loads mercado by id; `onDelete()` removes from Room and pops back |
| `ui/screen/mercado/DetalleMercadoScreen.kt` | Header, stats, maps link, meta rows, delete button |
| `ui/screen/mercado/CreateMercadoFormState.kt` | `name`, `address`, `mapsUrl`, `photoUri`, `nameError`, `isLoading`, `isEditing` |
| `ui/screen/mercado/CreateMercadoViewModel.kt` | Create + edit; loads existing mercado when `mercadoId != null`; extracts coordinates from mapsUrl on save |
| `ui/screen/mercado/CreateMercadoScreen.kt` | Name + address + Ubicación (MapsLinkField) + photo picker (160dp, camera+gallery) form, sticky save bar |
| `domain/util/MapsUrlParser.kt` | `extractMapsCoordinates(url)` — parses lat/lng from common Google Maps URL formats |
| `ui/common/MapsLinkField.kt` | Reusable composable — editable (paste chip) or read-only (open chip) |

---

### DetalleMercadoScreen

Entry point after long-pressing a mercado row. Shows data + allows edit and delete.

- `PedidosTopBar` with back + Edit (pencil) icon action → `CreateMercadoRoute(mercadoId)`
- **Header**: 60×60 `GridView` icon tile (surface-3 bg, border inset) + mercado name + address
- **Stats row**: 3 equal `StatCard` cards (surface-2 bg, 14dp corners, border inset) — Clientes / Al día / En riesgo (all stubbed at 0 until Phase 3)
- **Ubicación section** (shown only when `mapsUrl` is non-blank): section label + `MapsLinkField` (read-only, "Abrir" chip)
- **Meta rows**: `SettingRow` for "Clientes activos" (chevron, navigates to clientes list in Phase 3) + "Creado" date (`SimpleDateFormat` formatted)
- **Danger zone**: "Eliminar mercado" (50dp, 13dp corners, `redTint` bg, 22% red border) + disclaimer text — calls `DetalleMercadoViewModel.onDelete()` which deletes and pops back

---

## Data layer
