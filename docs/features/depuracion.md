# Feature: Depuración / Mantenimiento

## Status: ✅ Done (Phase 12)

Superusuario-only feature for archiving and permanently removing old pedidos from Supabase. Accessible from **Perfil → Mantenimiento → Depuración de datos**.

---

## Purpose

Pedidos accumulate indefinitely. Once a billing period is closed and archived, the operator can export the records to a file and hard-delete them from the cloud database to keep Supabase lean. Mercados, clientes, and productos are **not** affected — only pedidos (and their line items via cascade).

---

## Two-phase flow

The feature enforces that Phase 2 (delete) only runs if Phase 1 (export) succeeds.

| Phase | Action | Rollback if it fails |
|-------|--------|----------------------|
| 1 — Export | Fetch all pedidos older than the cutoff from Supabase; write to file | Nothing deleted yet; DB is intact |
| 2 — Delete | Hard-DELETE from Supabase (`detalle_pedido` cascades); hard-DELETE from Room | Export file already exists; user can retry delete manually |

---

## Screens

All implemented in a single `DepuracionScreen.kt` composable that renders different content based on `DepuracionPhase`.

### Config (`DepuracionPhase.CONFIG`)

- Red warning banner: "Acción permanente — no se pueden recuperar"
- `PhaseSteps` indicator (steps 1 and 2)
- **Qué depurar** section:
  - Registros: Pedidos (fixed — not editable)
  - Anteriores a: tappable date row → `DatePickerDialog`
- **Formato de respaldo** section: XLSX / CSV chip toggle
- Count pill: shows how many pedidos match the cutoff (loaded from `PedidoDao.countByCreatedAtBefore`); red if > 0, green if 0
- **Exportar y eliminar** button: red, disabled when count = 0

### Confirm dialog (`showConfirmDialog = true`)

- `AlertDialog` with trash icon in red tint
- Shows title with pedido count
- Requires user to type `ELIMINAR` (case-insensitive) before the confirm button activates

### Export progress (`DepuracionPhase.EXPORTING`)

- Back button hidden; `BackHandler` blocks system back
- Spinning sync icon in green tint
- Animated `LinearProgressIndicator` (accent color)
- Counter: `currentCount / totalCount`

### Delete progress (`DepuracionPhase.DELETING`)

- Same as export but red tint/color
- Back button hidden; `BackHandler` blocks system back

### Error (`DepuracionPhase.ERROR`)

- `PhaseSteps` with step 1 shown as failed (red)
- Green "0 pedidos eliminados" safety pill
- Shows optional error message from exception
- Two buttons: **Cancelar** (back) + **Reintentar** (re-runs full two-phase flow from export)

### Done (`DepuracionPhase.DONE`)

- Green check icon
- File card: name, size (`formatFileSize`), share button (opens `ACTION_SEND` chooser via FileProvider URI)
- **Listo** button navigates back to Perfil

---

## State machine

```
DepuracionUiState.phase:

CONFIG ──(confirm + ELIMINAR typed)──► EXPORTING
EXPORTING ──(export ok)──► DELETING
EXPORTING ──(export fails)──► ERROR
DELETING ──(delete ok)──► DONE
DELETING ──(delete fails)──► ERROR
ERROR ──(retry)──► EXPORTING (from start)
```

---

## Files

| File | Purpose |
|------|---------|
| `ui/screen/depuracion/DepuracionUiState.kt` | State class + `ExportFormat` enum + `DepuracionPhase` enum |
| `ui/screen/depuracion/DepuracionViewModel.kt` | Hilt ViewModel; `loadCount()`, `startDepuracion()`, date/format/confirm handlers |
| `ui/screen/depuracion/DepuracionScreen.kt` | All 6 screen states in one composable; `PhaseSteps`, `ConfigRow`, `FormatChip`, `SectionLabel` private helpers |
| `domain/repository/CleanupRepository.kt` | Interface: `countPedidosOlderThan`, `exportPedidosToFile`, `deletePedidosFromCloud` |
| `data/repository/impl/CleanupRepositoryImpl.kt` | Impl; Supabase range-fetch, CSV + XLSX writers, Room cleanup |
| `di/RepositoryModule.kt` | `@Binds CleanupRepository → CleanupRepositoryImpl` |

---

## Data layer

### `CleanupRepository` interface

```kotlin
interface CleanupRepository {
    suspend fun countPedidosOlderThan(cutoffMs: Long): Int
    suspend fun exportPedidosToFile(
        cutoffMs: Long,
        useXlsx: Boolean,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Triple<String, Long, Uri>   // (fileName, fileSizeBytes, uri)
    suspend fun deletePedidosFromCloud(
        cutoffMs: Long,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Int                          // returns count deleted
}
```

### New `PedidoDao` queries

```kotlin
@Query("SELECT COUNT(*) FROM pedidos WHERE createdAt < :cutoff")
suspend fun countByCreatedAtBefore(cutoff: Long): Int

@Query("DELETE FROM pedidos WHERE createdAt < :cutoff")
suspend fun deleteByCreatedAtBefore(cutoff: Long)
```

### Export logic (`CleanupRepositoryImpl`)

- Fetches pedidos from **Supabase** in pages of 1 000 using `range(offset, offset + 999)` filtered by `lt("created_at", cutoffMs)`.
- **CSV**: BOM-free UTF-8, comma-separated, headers on row 1. Cells with commas/quotes/newlines are quoted and escaped.
- **XLSX**: minimal Open Office XML written via `ZipOutputStream` — no external library. Five ZIP entries: `[Content_Types].xml`, `_rels/.rels`, `xl/workbook.xml`, `xl/_rels/workbook.xml.rels`, `xl/worksheets/sheet1.xml`. Cells use inline strings (`t="inlineStr"`).

### Export columns

`ID · Cliente ID · Estado · Total · Pagado · Notas · Saldo Extra · Creado (epoch ms) · Pagado en (epoch ms)`

### File location

Saved to `context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)/depuracion/<filename>`.  
URI served via `FileProvider` (authority `${applicationId}.fileprovider`; path declared in `res/xml/file_paths.xml`).

### Delete logic

```kotlin
// Phase 2 — hard delete from Supabase (ON DELETE CASCADE removes detalle_pedido)
supabase.from("pedidos").delete { filter { lt("created_at", cutoffMs) } }

// Clean Room mirror
pedidoDao.deleteByCreatedAtBefore(cutoffMs)  // FK CASCADE removes detalle_pedido locally too
```

---

## Entry point in Perfil

`PerfilScreen.kt` — inside the `if (state.role == UserRole.SUPERUSUARIO)` block, after Ajustes:

```
Section: MANTENIMIENTO
  SettingRow — icon: Delete (red), "Depuración de datos" → DepuracionRoute
```

---

## Strings

All strings namespaced under `depuracion_*`:

| Key | Value |
|-----|-------|
| `depuracion_title` | Depuración |
| `depuracion_warning_title` | Acción permanente |
| `depuracion_warning_body` | Los registros eliminados no se pueden recuperar |
| `depuracion_section_que` | Qué depurar |
| `depuracion_section_formato` | Formato de respaldo |
| `depuracion_registros_label` | Registros |
| `depuracion_registros_value` | Pedidos |
| `depuracion_cutoff_label` | Anteriores a |
| `depuracion_count_pill` | `%1$d pedidos encontrados` |
| `depuracion_cta` | Exportar y eliminar |
| `depuracion_phase1_label` | Generando respaldo… |
| `depuracion_phase2_label` | Eliminando registros… |
| `depuracion_error_title` | No se pudo generar el respaldo |
| `depuracion_error_body` | No se eliminó ningún registro. Tu base de datos está intacta. |
| `depuracion_error_safe_pill` | 0 pedidos eliminados |
| `depuracion_retry` | Reintentar |
| `depuracion_done_title` | Depuración completada |
| `depuracion_done_body` | `%1$d pedidos exportados y eliminados` |
| `depuracion_done_cta` | Listo |
| `depuracion_confirm_title` | `¿Eliminar %1$d pedidos?` |
| `depuracion_confirm_body` | Primero se descargará el respaldo. Luego el borrado es permanente… |
| `depuracion_confirm_type_hint` | Escribe ELIMINAR para confirmar |
| `depuracion_confirm_cta` | Eliminar |

Also added to Perfil strings: `perfil_section_mantenimiento`, `perfil_mantenimiento_title`, `perfil_mantenimiento_subtitle`.
