# Feature: Reportes (Tab 3)

## Status: ✅ Done (Phase 8)

---

## Spec summary

Third tab of the bottom navigation bar. Two modes switchable via a segmented toggle:

- **Diario** — pedidos created in a date range: a "Facturado hoy" hero card (total + pedido count), a Pagado/Por pagar two-card row below it, and a flat list of every pedido in the range (client name, mercado, time, total/pending, status).
- **Por cliente** — a single client's pedidos for any date range: client selector, two summary cards (Pedidos count / Saldo extra amount), a "Historial" list of the client's regular pedidos — each with its product line items shown as a sublist — and, only when the client has saldo-extra entries, a separate "Saldo extra" section at the end.

Both modes were rebuilt around **pedidos as the unit of the report** (previously Diario tracked a mixed cobro/pedido event timeline, and Por cliente mixed saldo-extra into the main list) — see "Diario mode" and "Por cliente mode" below for the full before/after.

Report export via a pill-shaped "PDF" button in the Reportes tab's top bar (Diario and Por cliente) and the "Generar PDF" button (Reporte de Pedidos screen). Reports are saved as `.html` files directly to the device's **Downloads** folder — visible in any file manager. A `Toast` confirms success or reports the error.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Reportes tab | Tab 2 of `HomeRoute` | `ui/screen/reporte/ReporteScreen.kt` |
| Reporte de Pedidos (per client, from DetalleCliente) | `ReporteClienteRoute(clienteId)` | `ui/screen/reporte/ReporteClienteScreen.kt` |
| Generando / Listo reporte | `ReporteStatusRoute` | `ui/screen/reporte/ReporteStatusScreen.kt` |

---

## UI implementation notes

### Mode toggle

Rounded pill container (`surface2` bg, `border` inset, 3dp inner padding, 13dp corners). Two equal-weight buttons:
- Selected: `primary` bg, `SemiBold` text, `onPrimary` color.
- Unselected: transparent, normal weight, `text2` color.

### Date chip bar

`FlowRow` (wraps chips to next line instead of horizontal scroll). Each chip: `CircleShape`, `accentSoft` bg + primary border when selected; `surface2` + `border` unselected.

**Diario presets:** Hoy / Ayer / Semana / Personalizado  
**Por cliente presets:** Este mes / Trimestre / Año / Personalizado

- Semana = last 7 days (today back to 6 days ago).
- Trimestre = first day of current calendar quarter (Q1–Q4) to today.
- Año = Jan 1 of current year to today.

### Resolved date bar

Shown below the chip bar when a non-Personalizado preset is active. A 42dp pill row (`surface2` bg, `border` inset, 12dp corners) with a `CalendarToday` icon (16dp, `text3`) and the resolved date text (13sp Medium, `text2`). Examples: "Hoy · 13 Jun 2026", "Esta semana · 7 Jun – 13 Jun 2026". Built by `formatDiarioBarText` / `formatClienteBarText` helpers in `ReporteDateChips.kt`.

### "Personalizado" date row

Shown below the chip bar when `PERSONALIZADO` is active. Two `DateField` composables side by side ("Desde" / "Hasta"), each clickable → Material3 `DatePickerDialog`. Field shows `CalendarToday` icon + formatted date or "—" if not yet set.

### Diario mode

Rebuilt to match the "Reportes · Diario" screen in the team's Claude Design mockup project (`Pedidos y Cuentas - Mockups.html`, `DCArtboard id="reporte"`).

- **`FacturadoHeroCard`**: full-width, 18dp corners, `surface2` bg + `border` inset. Header row: 32×32dp `accentTint` icon tile (`ShoppingCart`, `primary` tint), "Facturado hoy" label (`text2`), right-aligned "N pedidos" count (`text3`). Below: 34sp Bold Monospace total (`onSurface`, explicit 38sp `lineHeight`, letterSpacing −1sp).
- **`PagadoPorPagarRow`** (+ private `MoneyMiniCard`): two cards side by side under the hero — "Pagado" (green tint, `Check` icon) and "Por pagar" (amber tint, `AttachMoney` icon). Each: icon+label row, then 21sp Bold Monospace value below it (explicit 24sp `lineHeight`).
- **`ReporteStatCard`**: shared card primitive (also used by Por cliente mode's `PedidosSaldoExtraCards`). 16dp corners, `surface2` bg + `border` inset, 34×34dp icon tile, 22sp SemiBold Monospace value (explicit 25sp `lineHeight` — guards the same wrap-overlap bug fixed on `DetalleClienteScreen`'s balance card), 12.5sp `text2` label.
- **Pedidos list**: reuses `HistorialSectionHeader` ("Pedidos" + "N pedidos" action) and `HistorialRow` — the same components Por cliente mode uses. Each `diarioPedidos` entry is one pedido: `title` = client name, `subtitle` = "d MMM, HH:mm · mercado". Flat rows separated by `HorizontalDivider` (no enclosing card).

**Data model**: `diarioPedidos: List<HistorialItem>` — one row per pedido created in the selected range (`createdAt in [from,to] && !isSaldoExtra`), each carrying its own `total`/`paid`/`pending`. `diarioFacturado`/`diarioPagado`/`diarioPendiente` are the range sums, computed once in the ViewModel and reused by the hero/mini cards and by the PDF's "Resumen" cards.

> **Superseded**: `CobradoHeroCard` (single "Cobrado hoy" figure), `DiarioStatCards` (Pedidos creados / Pendiente del día counts), and `MovimientosSection`/`MovimientoRow` (a mixed cobro+pedido *event* timeline backed by `MovimientoItem`/`MovimientoType`, since removed) have all been deleted. The old model could show a cobro event and a pedido-creation event for the same pedido as two separate rows; the new model shows one row per pedido, matching both the client's ask ("show pedidos") and the PDF export.

### Por cliente mode

- **`ClienteSelectorCard`**: `surface2` card with 44dp `ClienteAvatar` + name + mercado name + "Cambiar" pill (accentSoft bg). Tapping "Cambiar" opens `ClienteSelectorSheet`.
- **`ClienteSelectorSheet`**: `ModalBottomSheet` with a scrollable list of all non-blacklisted clients sorted by name. Tapping a row selects it and closes the sheet. Selected row has `accentSoft` bg + `Check` icon.
- **`PedidosSaldoExtraCards`**: two equal `ReporteStatCard`s — "Pedidos" (plain count, no currency, `primary`/`accentTint`, `Receipt` icon) and "Saldo extra" (`Bs. X.XX` = Σpending over `saldoExtras`, `amberText`/`amberTint`, `AttachMoney` icon). Replaces an earlier 3-card Facturado/Pagado/Saldo money breakdown — per client feedback this screen should foreground pedidos + product quantities, not a financial summary; "Saldo extra" is intentionally the *only* money figure shown here.
- **`HistorialSectionHeader`** + **`HistorialRow`**: shared with Diario mode. 36dp rounded tile (green `Check` for fully paid, accent `Receipt` for pending/partial, amber `Assessment` for saldo-extra) + title (formatted date, or "Saldo extra") + status subtext (pending amount in amber or "Pagado" in green) + total amount (right) + "Total"/"Extra" label below.
  - **Product sublist**: when `item.lines` is non-empty, a per-product list renders below the row — a `Column` with one `×qty productName` line per `PedidoLineItem`, indented 48dp to align under the title (not a single truncated comma-joined string). Only regular pedidos carry `lines`; saldo-extra entries never do.
- **Historial / Saldo extra split**: `state.historial` holds only the client's regular (non-saldo-extra) pedidos for the range. Saldo-extra pedidos live in a separate `state.saldoExtras` list, rendered as its own "Saldo extra" section (header + rows) *after* the Historial list — previously they were interleaved into a single list, distinguished only by an amber icon and a "Saldo extra" title.

**Data model**: `HistorialItem` gained `subtitle: String` (Diario uses it for "date · mercado"; Por cliente's regular rows leave it blank, saldo-extra rows use it for "date · notes") and `lines: List<PedidoLineItem>` (product line items, populated from `Pedido.lines` for regular pedidos only). `facturado`/`pagado`/`saldo` are computed from the *billable* (non-saldo-extra) subset — this was already correct before this round of work and remains the source for the PDF's "Resumen" cards; only the on-screen 3-card money breakdown was removed in favor of `PedidosSaldoExtraCards`.

### Report export — status screen + save/share

Tapping the pill-shaped "PDF" button (Reportes tab top bar — `RoundedCornerShape(percent = 50)`, primary bg, `Description` icon + "PDF" text) or "Generar PDF" (ReporteClienteScreen) no longer directly downloads the file. Instead:

1. HTML is built via `buildReporteHtml` / `buildReporteClienteHtml`. `buildReporteHtml` additionally reads `R.drawable.img_logo` once (`LocalContext` + `remember`), base64-encodes it, and passes it down as `logoDataUri` — both `buildDiarioHtml` and `buildPorClienteHtml` embed it as `<img src="data:image/png;base64,...">` in the header, top-left. This is necessary (not just cosmetic) because the exported file is a standalone `.html` shared/opened outside the app, so it can't reference the app's local drawable by path.
2. A `PendingExport(html, fileName, itemCount, isMovimientosVariant)` is stored in `ReporteExportHolder` (singleton). For the Reportes tab, `isMovimientosVariant` is always `false` now — both Diario and Por cliente are pedido-based, so there's no "movimientos" variant left to distinguish (previously Diario passed its pedido count under the "movimientos" wording by mistake; see `GeneratingBody`'s step label below).
3. Navigation goes to `ReporteStatusRoute` → `ReporteStatusScreen`.

#### `ReporteStatusScreen` — generating state

Shown while the HTML is saved to the app cache (`context.cacheDir/reports/<fileName>`):
- Shimmer skeleton thumbnail (150×197dp, 9dp corners, `Brush.linearGradient` shimmer + border).
- "Creando tu reporte…" title + item-count description.
- Animated `LinearProgressIndicator` tied to step progress (0→12%→62%→95% over ~950ms).
- Three `GenStep` rows (circle badge + label):
  - Step 0 active: `accentTint` bg + spinning `Sync` icon; label "Pedidos/Movimientos reunidos".
  - Step 1 active: saving to cache.
  - Step 2 active: transitioning to ready.
  - Done steps: `greenTint` bg + `Check` icon. Todo steps: `surface3` bg + small dot.
- AppBar: "Generando reporte" title + "Cancelar" text button.

#### `ReporteStatusScreen` — ready state

Shown after cache write completes:
- Green check badge (56dp, 18dp corners, `greenTint` bg).
- "Tu reporte está listo" title + subtitle.
- `ReportDocPreview` (150×197dp): Compose-drawn stylized document thumbnail — green header bar with logo circle + name line, three mini stat boxes (green/accent/amber tints), section label + divider, and six alternating table rows with a coloured dot + value line.
- File meta card (`surface2` bg, `border` inset, 15dp corners): red-tint doc icon + filename + "HTML · N KB".
- AppBar: "Reporte listo" title (no Cancelar).
- Bottom bar (two buttons, 52dp height, 15dp corners):
  - **Compartir** (`OutlinedButton`): fires `Intent.ACTION_SEND` with the cached file via `FileProvider` (authority `${packageId}.fileprovider`, path `reports/`). Android native share sheet.
  - **Descargar** (`Button`, primary): calls `saveReportToDownloads` → `showSaveToast`. After first tap, button turns green ("Descargado" + Check icon, disabled to prevent duplicate).

#### `ReporteStatusScreen` — error state

Shown if the cache write fails (IOException or any unexpected exception):
- Red warning badge (64dp, 20dp corners, `errorContainer` bg).
- Two distinct error messages:
  - **No space** (`ENOSPC` / "No space left" in exception message): "Sin espacio suficiente" + "Libera espacio e intenta de nuevo."
  - **Other error**: "No se pudo generar" + "Ocurrió un error inesperado."
- AppBar: "Error al generar".
- Bottom bar: **Volver** (`OutlinedButton`, pops back) + **Reintentar** (`Button`, increments `retryKey` → re-runs `LaunchedEffect(retryKey)`).
- The `pending` data is held in `remember { }` (not cleared from holder on error), so retries work without re-navigating.

> Fixed bug: the "no se perdió nada" reassurance card at the bottom of this screen (`ReporteErrorBody.kt`) used a raw `MaterialTheme.colorScheme.surface` background — pure white in light theme, indistinguishable from the near-white page background, effectively rendering as an unstyled white smear glued against the surrounding content. Its sibling card just above it already used `ext.surface2` correctly; the error card now matches.

#### `ReporteSaver.kt` (unchanged)

`saveReportToDownloads(context, html, fileName)` writes to the device Downloads folder:
- **API 29+**: `MediaStore.Downloads` API — no storage permission needed.
- **API 24–28**: `Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)` + `File.writeText`.
Returns `SaveResult`: `Success(fileName)`, `NoSpace`, or `Error(cause)`.
`showSaveToast` shows a `Toast.LENGTH_LONG` with the result in Spanish.

#### `ReporteExportHolder.kt`

```kotlin
data class PendingExport(val html: String, val fileName: String, val itemCount: Int, val isMovimientosVariant: Boolean)
object ReporteExportHolder { var pending: PendingExport? = null }
```

**Filenames:**
- Reportes tab (Diario mode): `Reporte_Diario_20260613_1432.html`
- Reportes tab (Por cliente mode): `Reporte_PorCliente_20260613_1432.html`
- Generar reporte screen: `Reporte_{SafeClientName}_20260613_1432.html` (spaces → `_`, non-alphanumeric stripped)

- **Diario HTML** (`buildDiarioHtml`): logo + header with date range, 3 summary cards ("Total pedidos" / Pagado / Pendiente), pedidos table (Cliente / Total / Pagado / Pendiente) with a bold totals row (`.tot`).
- **Por cliente HTML** (`buildPorClienteHtml`): logo + header with client name + date range, 3 summary cards (Facturado/Pagado/Saldo), Historial table (Pedido / Detalle as a bulleted `<ul><li>` product list / Total / Pagado / Saldo / Estado chip) with a totals row, and — only when `state.saldoExtras` is non-empty — a separate "Saldo extra" table (Detalle+subtitle / Total / Pagado / Saldo) with its own totals row.

---

## State

```kotlin
enum class ReporteMode { DIARIO, POR_CLIENTE }
enum class DiarioPreset { HOY, AYER, SEMANA, PERSONALIZADO }
enum class ClientePreset { MES, TRIMESTRE, ANIO, PERSONALIZADO }

data class HistorialItem(
    val pedidoId: String, val title: String, val date: Long,
    val total: Double, val paid: Double, val pending: Double, val isSaldoExtra: Boolean,
    val subtitle: String = "", val lines: List<PedidoLineItem> = emptyList(),
)
data class ClienteOption(val id: String, val name: String, val photoUrl: String?, val mercadoName: String)
data class ReporteUiState(
    val mode: ReporteMode,
    // Diario
    val diarioPreset, diarioFromMs, diarioToMs,
    val diarioFacturado, diarioPagado, diarioPendiente,
    val diarioPedidos: List<HistorialItem>,
    // Por cliente
    val clientePreset, clienteFromMs, clienteToMs,
    val selectedClienteId, selectedClienteName, selectedClientePhotoUrl, selectedMercadoName,
    val facturado, pagado, saldo,
    val historial: List<HistorialItem>,
    val saldoExtras: List<HistorialItem>,
    // Shared
    val customDiarioFrom/To, customClienteFrom/To,
    val allClientes: List<ClienteOption>,
    val isLoading: Boolean,
)
```

> `MovimientoItem`/`MovimientoType` (and the `cobradoTotal`/`cobroCount`/`pedidosCreadosCount`/`pendienteDelDia`/`movimientos` fields they backed) were removed entirely — Diario is pedido-based now, sharing `HistorialItem` with Por cliente mode instead of tracking a separate event timeline.

---

## ViewModel

### Data sourcing

**Both report ViewModels read exclusively from Room — no Supabase calls happen at query time.**

- `ReporteViewModel` subscribes to `pedidoRepository.getAll()`, `clienteRepository.getAll()`, and `mercadoRepository.getAll()` — all `Flow<List<...>>` backed by Room DAOs. All date filtering (`paidAt in from..to`, `createdAt in from..to`) and aggregation happen in-memory inside the `combine` block after Room emits.
- `ReporteClienteViewModel` subscribes to `clienteRepository.getByIdFlow(clienteId)` and `pedidoRepository.getByClienteWithLines(clienteId)`. Date range filtering is also applied in-memory.

This means report data is always as fresh as the last successful sync. The `isLoading` flag (driven by `isSyncing && pedidos.isEmpty()`) shows a loading overlay on first open before any sync has completed, preventing the user from seeing an empty or partial report.

**Edge case — annual preset (`ClientePreset.ANIO` / `ReporteClientePreset.MES` spanning many months):** all historical pedidos must already be in Room. Since the full-fetch syncer now pages through all records via `range()` (Phase 10c), this is covered as long as at least one full sync has completed. A user opening the app for the very first time and immediately running a year-wide report will see incomplete data until the initial sync finishes.

`ReporteViewModel` uses a single `MutableStateFlow<ReporteInput>` to hold all user selections (mode, presets, selected client, custom dates). This combines with a nested `combine(pedidoRepository.getAll(), clienteRepository.getAll(), mercadoRepository.getAll())` triple, producing the full `ReporteUiState` reactively.

**Diario computation:**
- `createdInRange` = pedidos where `createdAt in [from,to]` and `!isSaldoExtra`
- `diarioPedidos` = `createdInRange` mapped 1:1 to `HistorialItem` (`title` = client name, `subtitle` = "d MMM, HH:mm · mercado"), sorted by `createdAt` DESC
- `diarioFacturado`/`diarioPagado`/`diarioPendiente` = Σtotal / Σpaid / Σpending over `createdInRange`

> Previously computed two overlapping event sets (`cobros` by `paidAt`, `createdInRange` by `createdAt`) and merged them into a COBRO/PEDIDO timeline — a pedido paid today but created earlier could appear as two rows. Replaced with a single per-pedido pass.

**Por cliente computation:**
- Filter all pedidos for `selectedClienteId` where `createdAt in [from,to]`
- Partition into `billable` (non-saldoExtra) and `extras` (saldoExtra)
- `facturado`=Σtotal, `pagado`=Σpaid, `saldo`=Σpending — over `billable` only (unchanged)
- `historial` = `billable` mapped to `HistorialItem` (`title` = formatted date, `lines` = `p.lines`), sorted `createdAt` DESC
- `saldoExtras` = `extras` mapped to `HistorialItem` (`title` = "Saldo extra", `subtitle` = "date · notes"), sorted `createdAt` DESC

**Date helpers:** `startOfDay(offset)`, `endOfDay(offset)`, `startOfMonth()`, `startOfQuarter()` (first day of current Q), `startOfYear()` — all via `Calendar.getInstance()`.

**Actions:** `setMode`, `setDiarioPreset`, `setClientePreset`, `setCustomDiario{From,To}`, `setCustomCliente{From,To}`, `selectCliente`.

---

## Data layer changes

### PedidoDao — new query

```kotlin
@Query("SELECT * FROM pedidos ORDER BY createdAt DESC")
fun getAll(): Flow<List<PedidoEntity>>
```

Added to `PedidoRepository` interface and `PedidoRepositoryImpl`. **No DB migration required** — reads from the existing `pedidos` table.

---

## Files

### Entry screens & state

| File | Description |
|------|-------------|
| `ui/screen/reporte/ReporteUiState.kt` | Enums + data classes for Reporte tab (`ReporteMode`, `DiarioPreset`, `ClientePreset`, `HistorialItem`, `ClienteOption`, `ReporteUiState`) |
| `ui/screen/reporte/ReporteViewModel.kt` | Mode/preset state machine + reactive pedido aggregation (Diario: pedidos in range; Por cliente: client's pedidos split into `historial` + `saldoExtras`) |
| `ui/screen/reporte/ReporteScreen.kt` | Reporte tab entry + Scaffold/LazyColumn orchestration (DatePickerDialogs, ClienteSelectorSheet); reads `img_logo` once and base64-encodes it for PDF export |
| `ui/screen/reporte/ReporteClienteUiState.kt` | State + `ReporteClientePreset` enum for per-client report |
| `ui/screen/reporte/ReporteClienteViewModel.kt` | Date range computation, pedido filtering, warning threshold |
| `ui/screen/reporte/ReporteClienteScreen.kt` | Per-client report entry + Scaffold/Column orchestration (DatePickerDialogs) |

### Components — Reporte tab

| File | Description |
|------|-------------|
| `ui/screen/reporte/components/ReporteModeToggle.kt` | `ReporteModeToggle` + `ModeButton` (pill mode selector) |
| `ui/screen/reporte/components/ReporteDateChips.kt` | `DiarioDateChips`, `ClienteDateChips`, `DateChip`, `CustomDateRow`, `DateField` |
| `ui/screen/reporte/components/ReporteDiarioContent.kt` | `ReporteStatCard` (shared primitive), `FacturadoHeroCard`, `PagadoPorPagarRow` (+ private `MoneyMiniCard`) |
| `ui/screen/reporte/components/ReportePorClienteContent.kt` | `ClienteSelectorCard`, `ClienteSelectorSheet`, `PedidosSaldoExtraCards`, `HistorialSectionHeader` (title + optional trailing count), `HistorialRow` (with per-product sublist) |

### Components — Generar reporte screen

| File | Description |
|------|-------------|
| `ui/screen/reporte/components/ReporteClienteRangeSelector.kt` | `IntroText`, `RangeSectionLabel`, `PresetChipsRow`, `ReporteClientePresetChip`, `ResolvedDateBar`, `CustomDateFields`, `DateFieldButton`, `formatResolvedBarText` |
| `ui/screen/reporte/components/ReporteClienteSummaryCards.kt` | `SummaryCard`, `WarningBanner` |
| `ui/screen/reporte/components/ReporteClientePedidos.kt` | `PreviewListSection`, `ReportePedidoPreviewRow`, `GenerarPdfBar` |

### HTML builders & export

| File | Description |
|------|-------------|
| `ui/screen/reporte/html/ReporteHtml.kt` | `buildReporteHtml`, `buildDiarioHtml`, `buildPorClienteHtml` |
| `ui/screen/reporte/html/ReporteClienteHtml.kt` | `buildReporteClienteHtml`, `formatPeriodLabel` |
| `ui/screen/reporte/ReporteSaver.kt` | `saveReportToDownloads` (MediaStore API 29+ / File API 24–28) + `SaveResult` sealed interface + `showSaveToast` |
| `ui/screen/reporte/ReporteExportHolder.kt` | `PendingExport` data class + `ReporteExportHolder` singleton (passes HTML between calling screen and status screen) |
| `ui/screen/reporte/ReporteStatusScreen.kt` | Generating state (shimmer + steps + progress bar) and ready state (file meta + Download + Share actions) |

### Data layer

| File | Description |
|------|-------------|
| `data/local/dao/PedidoDao.kt` | Added `getAll()` |
| `domain/repository/PedidoRepository.kt` | Added `getAll()` |
| `data/repository/impl/PedidoRepositoryImpl.kt` | Added `getAll()` impl |

---

---

## Generar reporte de pedidos (`ReporteClienteScreen`)

Standalone screen reached from the "Generar reporte" menu item in `DetalleClienteScreen`. Route: `ReporteClienteRoute(clienteId)`.

### AppBar
Title: "Generar reporte" / subtitle: "{nombre} · {mercado}". Back arrow (left) + close × (right, also pops).

### Body (scrollable)
1. **Intro text** — 13.5sp, `text2` color, lineHeight 20sp. Copy explicitly states what the generated PDF will contain (products + quantities per pedido, plus saldo-extra movements), so the user knows this is a detailed pedido-level export before they generate it — distinct from the summary-style "Por cliente" report on the Reportes tab.
2. **"RANGO RÁPIDO"** section label (11.5sp, semibold, uppercase, 0.5sp letterSpacing).
3. **Preset chips row** (horizontally scrollable): Hoy / Esta semana / Este mes / Personalizado.  
   - Selected: primary bg + `onPrimary` text, 38dp height, 11dp corners.  
   - Unselected: `surface2` bg + `border2` inset.
4. **Date display** (below chips):
   - Non-Personalizado: `ResolvedDateBar` — 44dp, 12dp corners, `surface2`+`border`, calendar icon + resolved text (e.g. "Esta semana · 8 Jun – 13 Jun 2026").
   - Personalizado: two `DateFieldButton`s side by side (Desde / Hasta), 50dp each, accent calendar icon, chevron right, DatePickerDialog on tap.
5. **Summary card** (`SummaryCard`) — "Pedidos en el rango" (count, 20sp bold mono) + divider + "Monto total" (amount, 16sp semibold mono). `surface2` bg, 16dp corners.
6. **Warning banner** (`WarningBanner`) — shown when `pedidosCount > 50`. Amber tint bg + amber border, Info icon + "Rango amplio" title + description.
7. **Preview list** (`PreviewListSection`) — "PEDIDOS DE HOY" (HOY preset) or "PEDIDOS EN EL REPORTE" + "N de total" counter. Shows up to 5 `ReportePedidoPreviewRow` items + "y N pedidos más en el PDF" footer if count > 5.

### Bottom bar (`GenerarPdfBar`)
Full-width "Generar PDF" button (52dp, 15dp corners, primary color, Description icon). Disabled until range is valid. Rendered as the Scaffold's `bottomBar` (not appended to the end of the scrollable column), so it stays pinned to the bottom of the screen regardless of list length — same sticky behavior as the Reportes tab's PDF action.

### HTML export (`buildReporteClienteHtml`)

Reworked to reach visual parity with the Reportes tab's reports and to be the most complete of the three (it now shows more detail per pedido than either Diario or Por cliente):

- **Header** — now matches `buildDiarioHtml`/`buildPorClienteHtml` exactly: real app logo (`R.drawable.img_logo`, base64 `<img>`, 48×48) instead of the old static "CV" text badge, `#2FA24E` green accent border (was `#1E7D38`), 20px bold title ("Reporte de pedidos"), and a 15px bold `.client-meta` line for `{clienteName} · {mercadoName}` — same size/weight the Por-cliente report uses for its client line. `ReporteClienteScreen` now reads and base64-encodes the logo the same way `ReporteScreen` does, and passes it into `buildReporteClienteHtml` as a new `logoDataUri` parameter.
- **Cliente section** — since nombre/mercado now live in the header, this section only renders Descripción/Teléfono (when present) and is omitted entirely if the client has neither.
- **Pedidos table** — single unified list (not split into separate tables like Por cliente's Historial/Saldo-extra split):
  - **Product detail**: each regular pedido's `Detalle` cell now renders its `PedidoLineItem`s as a bulleted `<ul><li>×qty productName</li></ul>` list (previously a single comma-joined line) — matching the Por-cliente report's product list style.
  - **Saldo extra rows**: kept in the same table as regular pedidos (per requirement — one complete list) but visually distinguished: amber-tinted row background (`.row-extra`), a 3px amber left border on the first cell, and a small "◆ Saldo extra" tag rendered under the date. The existing amber "Saldo extra" status chip is unchanged.
  - **Totals**: `Total facturado`/`Pagado`/`Saldo pendiente` are computed over *all* `pedidosInRange`, saldo extra included — a deliberate difference from the Por-cliente report (which excludes saldo extra from its headline totals and gives it a separate totals row). Since this report intentionally keeps everything in one list, a small note (`Incluye Bs. X.XX en saldo extra (N movimientos)`) is shown under the stat cards whenever saldo-extra entries exist, so the reader can see how much of the total is a manual adjustment without the totals hiding it.
  - **Payment sublist**: each pedido row's `Detalle` cell now also renders a small "Pagos" sub-list — one line per real payment event (`d MMM, HH:mm — Bs. X.XX`), sourced from the new `pagos` table (see `docs/db-schema.md`). This replaces the earlier limitation where partial-payment history was dropped for lack of a per-payment ledger — that ledger now exists (built for `DetallePedidoScreen`'s `PagosSection`, see `docs/features/pedidos.md`), so the report reuses it. Rendered for both regular and saldo-extra rows uniformly (`.pay-hist`/`.pay-hist-title`/`.pay-list` CSS classes).
- Saved to Downloads via `saveReportToDownloads`, same as before.

### ViewModel: sourcing the payment sublist

`ReporteClienteViewModel.uiState` combine now also subscribes to `pedidoRepository.getPagosByClienteFlow(clienteId)` (a 5th flow, joined via `PagoDao.getByClienteFlow` — `pagos INNER JOIN pedidos ON pagos.pedidoId = pedidos.id WHERE pedidos.clienteId = :clienteId`). Following this feature's established pattern, all filtering happens in-memory after Room emits: pagos are filtered down to `it.pedidoId in pedidosInRange.map{it.id}` and exposed as `ReporteClienteUiState.pagos`. `buildReporteClienteHtml` groups them by `pedidoId` once (`state.pagos.groupBy { it.pedidoId }`) and looks up each pedido's sublist while building its row.

### ViewModel logic
- `ReporteClientePreset`: HOY (today start→end), SEMANA (Monday of current week → today end), MES (first of month → today end), PERSONALIZADO (user picks).
- Pedidos filtered by `createdAt in [fromMs..toMs]`.
- `WARNING_THRESHOLD = 50`.
- Custom dates: `selectedDateMillis` from DatePickerState (UTC midnight) → converted to local start/end of day.

---

## Open TODOs

- [ ] Real-time cross-device report data: currently reports reflect whatever is in Room at the time of viewing. A pull-to-refresh trigger on the Reportes tab (similar to the other list screens) would let users manually force a delta sync before generating a report, ensuring the latest cobros and pedidos from other devices are included.
- [ ] Per-payment history: `pedidos` only stores a single cumulative `paid` amount and a single `paidAt` (most recent payment date) — see `docs/db-schema.md`. Showing a real list of "paid Bs. X on date Y" for partial payments would require a new `pagos`/`cobros` child table logging every individual payment event, plus updating every payment-recording flow to insert into it. Explicitly out of scope for the `ReporteClienteScreen` PDF rework — deferred until/unless payment-level history is actually needed.
