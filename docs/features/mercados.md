# Feature: Mercados

## Status: ✅ Done (Phase 2 + 2f)

---

## Spec summary

Entry point after login (tab 0). Shows all Mercados alphabetically. Each row shows a grid-icon tile, the mercado name, live active client count, and an optional colored dot when any client inside is in Warning (amber) or Critical (red) state. A "Lista Negra" card button at the bottom navigates to the global blacklist. FAB creates a new Mercado.

The dot means **exactly** what the client's own badge means — both go through `CalcularEstadoClienteUseCase` with the configured `Umbrales`, so only partially-paid regular pedidos colour a mercado. An untouched PENDING order and a saldo extra do not, and raising the thresholds in Ajustes affects the dashboard. Until Phase 17h this screen had its own rule that counted every unpaid pedido against a hardcoded 200,0 / 30 días, so a mercado could show red while every client inside it showed AL_DIA. See `docs/features/clientes.md` for the rule itself.

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

### Role-based access (INVITADO restrictions)

`MercadosUiState` carries `canWrite: Boolean` (computed as `user?.role != UserRole.INVITADO`).
`DetalleMercadoUiState` also carries `canWrite: Boolean`.

| Element | Who sees it |
|---------|-------------|
| FAB "Mercado" (create) | SUPERUSUARIO + USUARIO only |
| Edit (pencil) icon in `DetalleMercadoScreen` | SUPERUSUARIO + USUARIO only |
| Danger zone / Delete button in `DetalleMercadoScreen` | SUPERUSUARIO + USUARIO only |

The FAB is already hidden during selection mode; `canWrite = false` adds a second gate so INVITADO users never see the create button.

---

### MercadosScreen — normal state

- Large `PedidosTopBar` with title "Mercados" and subtitle "%d mercados"
- **Logo mark** (leading): 34dp `CircleShape`, white background, 1dp `border2` inset, `img_logo.png` `ContentScale.Crop` — rendered via the `leading` parameter of `PedidosTopBar`. Defined as private `LogoMark()` composable in `MercadosScreen.kt`.
- Top-bar actions: Search icon (→ `BusquedaRoute`), Notifications icon (stub), Profile avatar (→ `PerfilRoute`)
- `MercadoRow`: 44dp grid-icon tile (`surface3` bg, `border` inset, `GridView` icon) + name + live "N clientes activos" subtitle (with a 6dp colored dot before the text when any client is ADVERTENCIA/CRITICO) + chevron
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
    - Both camera and gallery picks are stored in `cacheDir/images/` via `createCameraImageUri()` / `copyImageToCache()` (`ui/common/PhotoUtils.kt`) — survives app restarts
    - `photoUrl` stored as a local FileProvider URI string; Supabase Storage upload wired in Phase 9
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

| Method | Returns | Notes |
|--------|---------|-------|
| `getAll()` | `Flow<List<MercadoEntity>>` ordered by `name ASC` | |
| `getById(id)` | `MercadoEntity?` (suspend) | One-shot |
| `getByIdFlow(id)` | `Flow<MercadoEntity?>` | Reactive — used by `DetalleMercadoViewModel` |
| `insert(entity)` | `Long` — `OnConflictStrategy.IGNORE` | Returns `-1` on PK conflict |
| `update(entity)` | `Unit` | |
| `deleteById(id)` | `Unit` | |

> `insert` uses `IGNORE` (not `REPLACE`) — see `docs/db-schema.md → Data integrity`. The repository falls through to `update()` when `-1L` is returned.

### Repository — `MercadoRepository`

```kotlin
fun getAll(): Flow<List<Mercado>>
fun getByIdFlow(id: String): Flow<Mercado?>   // reactive — for detail screen
suspend fun getById(id: String): Mercado?
suspend fun save(mercado: Mercado)             // IGNORE + update fallthrough
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
| `domain/usecase/RefreshMercadoDataUseCase.kt` | Refreshes mercados + clientes + pedidos in parallel; returns `Boolean` success. The dashboard needs all three current to render per-market warnings correctly. |
| `ui/screen/mercado/MercadosUiState.kt` | `mercados`, `stats: Map<String, MercadoStat>`, `isLoading`, `currentUserInitials`, `currentUserPhotoUrl`, `selectedMercadoId`; `MercadoStat(activeClientCount, hasWarning, hasCritical)` |
| `ui/screen/mercado/MercadosViewModel.kt` | Nested `combine` over mercados + all clients + all unpaid pedidos + umbrales + session + selection; `buildStats()` computes per-mercado status by delegating to `CalcularEstadoClienteUseCase`; threads `currentUserPhotoUrl` from session; pull-to-refresh delegates to `RefreshMercadoDataUseCase` |
| `ui/screen/mercado/MercadosScreen.kt` | List, contextual action bar, selection visual state; `MercadoTile` shows photo via `PhotoThumbnail`, falls back to `GridView` icon; `MercadoStatRow` shows count + colored status dot; top-bar `ProfileAvatar` passes `currentUserPhotoUrl` |
| `ui/screen/mercado/DetalleMercadoUiState.kt` | `mercado`, `isLoading` |
| `ui/screen/mercado/DetalleMercadoViewModel.kt` | Reactive `stateIn` over `MercadoRepository.getByIdFlow(mercadoId)` — screen auto-updates after edits; `onDelete()` removes from Room and pops back |
| `ui/screen/mercado/DetalleMercadoScreen.kt` | Header, stats, maps link (UBICACIÓN always visible — `MapsLinkField` handles blank values), meta rows, delete button |
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
- **Stats row**: 3 equal `StatCard` cards (surface-2 bg, 14dp corners, border inset) — Clientes / Al día / En riesgo (still hardcoded 0; real values can be wired using `clienteRepository.getByMercado()` + pedidos aggregate)
- **Ubicación section** (always visible): section label + `MapsLinkField` (read-only, "Abrir" chip when filled, placeholder when blank)
- **Meta rows**: `SettingRow` for "Clientes activos" (chevron, navigates to clientes list in Phase 3) + "Creado" date (`SimpleDateFormat` formatted)
- **Danger zone**: "Eliminar mercado" (50dp, 13dp corners, `redTint` bg, 22% red border) + disclaimer text — calls `DetalleMercadoViewModel.onDelete()` which deletes and pops back

---

## Data layer
