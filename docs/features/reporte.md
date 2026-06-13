# Feature: Reportes (Tab 3)

## Status: ✅ Done (Phase 8)

---

## Spec summary

Third tab of the bottom navigation bar. Two modes switchable via a segmented toggle:

- **Diario** — daily stats for any date range: hero "Cobrado hoy" card, two stat cards (Pedidos creados / Pendiente del día), and a movements list (cobros + new pedidos).
- **Por cliente** — client-specific stats for any date range: client selector, three stat cards (Facturado / Pagado / Saldo), and an historial list of all pedidos/saldo-extra in the range.

Report export via the top-bar `Description` icon (Reportes tab) and the "Generar PDF" button (Reporte de Pedidos screen). Reports are saved as `.html` files directly to the device's **Downloads** folder — visible in any file manager. A `Toast` confirms success or reports the error.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Reportes tab | Tab 2 of `HomeRoute` | `ui/screen/reporte/ReporteScreen.kt` |
| Reporte de Pedidos (per client, from DetalleCliente) | `ReporteClienteRoute(clienteId)` | `ui/screen/reporte/ReporteClienteScreen.kt` |

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

- **`CobradoHeroCard`**: full-width, 20dp corners, `Brush.verticalGradient` from `green.copy(α=0.16)` to `green.copy(α=0.05)`, `green.copy(α=0.22)` border. Title `greenText`, 38sp Bold Monospace amount, `text3` cobro count subtitle.
- **`ReporteStatCard`**: shared card composable. 16dp corners, `surface2` bg + `border` inset. Contains: a 34×34dp icon container (10dp corners, `iconBgColor` bg, 18dp icon in `valueColor`), then 22sp SemiBold Monospace value (letterSpacing −0.5sp), then 12.5sp `text2` label.
- **`DiarioStatCards`**: two equal `ReporteStatCard`s — "Pedidos creados" (`accentTint` icon bg, `primary` value, `ShoppingCart` icon) + "Pendiente del día" (`amberTint` icon bg, `amberText` value, `AttachMoney` icon).
- **`MovimientosSection`**: `MOVIMIENTOS DE HOY` section label + list of `MovimientoRow`s or "Sin movimientos en este período" text.
- **`MovimientoRow`**: 8dp `CircleShape` dot (`greenText` for COBRO, `primary` for PEDIDO) + name + "Cobro/Pedido · mercado · HH:mm" subtitle + right-aligned amount (green for COBRO).

### Por cliente mode

- **`ClienteSelectorCard`**: `surface2` card with 44dp `ClienteAvatar` + name + mercado name + "Cambiar" pill (accentSoft bg). Tapping "Cambiar" opens `ClienteSelectorSheet`.
- **`ClienteSelectorSheet`**: `ModalBottomSheet` with a scrollable list of all non-blacklisted clients sorted by name. Tapping a row selects it and closes the sheet. Selected row has `accentSoft` bg + `Check` icon.
- **`ClienteStatCards`**: three equal cards — Facturado (`accentSoft`/`primary`) + Pagado (`greenTint`/`greenText`) + Saldo (`amberTint`/`amberText`).
- **`HistorialSectionHeader`** + **`HistorialRow`**: 36dp rounded tile (green Check for PAID, accent Receipt for PENDING/PARTIAL, amber Assessment for saldoExtra) + title (formatted date or "Saldo extra") + status subtext (pending amount in amber or "Pagado" in green) + total amount (right) + "Total"/"Extra" label below.

### Report export — save to Downloads

Both modes share a single save-to-file flow (replaces the previous `WebView + PrintManager` print dialog):

1. HTML is generated via the same `buildDiarioHtml` / `buildPorClienteHtml` / `buildReporteClienteHtml` builders.
2. `saveReportToDownloads(context, html, fileName)` (`ui/screen/reporte/ReporteSaver.kt`) writes the file:
   - **API 29+**: `MediaStore.Downloads` API — no storage permission needed.
   - **API 24–28**: `Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)` + `File.writeText`.
3. Returns a `SaveResult` sealed interface: `Success(fileName)`, `NoSpace`, or `Error(cause)`.
4. `showSaveToast(context, result)` shows a `Toast.LENGTH_LONG`:
   - Success → `"Reporte guardado en Descargas"`
   - No space → `"Sin espacio suficiente en el dispositivo"`
   - Other error → `"Error al guardar el reporte"`

**Filenames:**
- Reportes tab (Diario mode): `Reporte_Diario_20260613_1432.html`
- Reportes tab (Por cliente mode): `Reporte_PorCliente_20260613_1432.html`
- Generar reporte screen: `Reporte_{SafeClientName}_20260613_1432.html` (spaces → `_`, non-alphanumeric stripped)

- **Diario HTML** (`buildDiarioHtml`): header with date range, 3 summary cards (Cobrado/Pedidos creados/Pendiente), movements table (Cliente / Tipo chip / Monto).
- **Cliente HTML** (`buildClienteHtml`): header with client name + date range, 3 summary cards (Facturado/Pagado/Saldo), historial table (Pedido / Estado chip / Total / Pendiente).

---

## State

```kotlin
enum class ReporteMode { DIARIO, POR_CLIENTE }
enum class DiarioPreset { HOY, AYER, SEMANA, PERSONALIZADO }
enum class ClientePreset { MES, TRIMESTRE, ANIO, PERSONALIZADO }
enum class MovimientoType { COBRO, PEDIDO }

data class MovimientoItem(
    val pedidoId: String, val clienteName: String, val mercadoName: String,
    val type: MovimientoType, val amount: Double, val timestamp: Long,
)
data class HistorialItem(
    val pedidoId: String, val title: String, val date: Long,
    val total: Double, val paid: Double, val pending: Double, val isSaldoExtra: Boolean,
)
data class ClienteOption(val id: String, val name: String, val photoUrl: String?, val mercadoName: String)
data class ReporteUiState(
    val mode: ReporteMode,
    // Diario
    val diarioPreset, diarioFromMs, diarioToMs,
    val cobradoTotal, cobroCount, pedidosCreadosCount, pendienteDelDia,
    val movimientos: List<MovimientoItem>,
    // Por cliente
    val clientePreset, clienteFromMs, clienteToMs,
    val selectedClienteId, selectedClienteName, selectedClientePhotoUrl, selectedMercadoName,
    val facturado, pagado, saldo,
    val historial: List<HistorialItem>,
    // Shared
    val customDiarioFrom/To, customClienteFrom/To,
    val allClientes: List<ClienteOption>,
    val isLoading: Boolean,
)
```

---

## ViewModel

`ReporteViewModel` uses a single `MutableStateFlow<ReporteInput>` to hold all user selections (mode, presets, selected client, custom dates). This combines with a nested `combine(pedidoRepository.getAll(), clienteRepository.getAll(), mercadoRepository.getAll())` triple, producing the full `ReporteUiState` reactively.

**Diario computation:**
- `cobros` = pedidos where `paidAt in [from,to]` and `paid > 0`
- `createdInRange` = pedidos where `createdAt in [from,to]` and `!isSaldoExtra`
- Movements = cobros (as COBRO) + non-cobro createdInRange items (as PEDIDO), sorted by timestamp DESC

**Por cliente computation:**
- Filter all pedidos for `selectedClienteId` where `createdAt in [from,to]`
- `billable` = non-saldoExtra subset; `facturado`=Σtotal, `pagado`=Σpaid, `saldo`=Σpending
- Historial sorted by `createdAt DESC`

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
| `ui/screen/reporte/ReporteUiState.kt` | Enums + data classes for Reporte tab (`ReporteMode`, `DiarioPreset`, `ClientePreset`, `MovimientoItem`, `HistorialItem`, `ClienteOption`, `ReporteUiState`) |
| `ui/screen/reporte/ReporteViewModel.kt` | Mode/preset state machine + reactive stats computation |
| `ui/screen/reporte/ReporteScreen.kt` | Reporte tab entry + Scaffold/LazyColumn orchestration (DatePickerDialogs, ClienteSelectorSheet) |
| `ui/screen/reporte/ReporteClienteUiState.kt` | State + `ReporteClientePreset` enum for per-client report |
| `ui/screen/reporte/ReporteClienteViewModel.kt` | Date range computation, pedido filtering, warning threshold |
| `ui/screen/reporte/ReporteClienteScreen.kt` | Per-client report entry + Scaffold/Column orchestration (DatePickerDialogs) |

### Components — Reporte tab

| File | Description |
|------|-------------|
| `ui/screen/reporte/components/ReporteModeToggle.kt` | `ReporteModeToggle` + `ModeButton` (pill mode selector) |
| `ui/screen/reporte/components/ReporteDateChips.kt` | `DiarioDateChips`, `ClienteDateChips`, `DateChip`, `CustomDateRow`, `DateField` |
| `ui/screen/reporte/components/ReporteDiarioContent.kt` | `CobradoHeroCard`, `DiarioStatCards`, `ReporteStatCard`, `MovimientoRow`, section headers |
| `ui/screen/reporte/components/ReportePorClienteContent.kt` | `ClienteSelectorCard`, `ClienteSelectorSheet`, `ClienteStatCards`, `HistorialRow`, section headers |

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
1. **Intro text** — 13.5sp, `text2` color, lineHeight 20sp.
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
Full-width "Generar PDF" button (52dp, 15dp corners, primary color, Description icon). Disabled until range is valid.

### HTML export (`buildReporteClienteHtml`)
CV logo box + company name header · client section (nombre, mercado, descripción, teléfono) · period section (range + count) · 3 stat boxes (Facturado / Pagado / Saldo pendiente) · table (Fecha / Detalle / Total / Pagado / Saldo / Estado) with totals row · footer. Saved to Downloads via `saveReportToDownloads`.

### ViewModel logic
- `ReporteClientePreset`: HOY (today start→end), SEMANA (Monday of current week → today end), MES (first of month → today end), PERSONALIZADO (user picks).
- Pedidos filtered by `createdAt in [fromMs..toMs]`.
- `WARNING_THRESHOLD = 50`.
- Custom dates: `selectedDateMillis` from DatePickerState (UTC midnight) → converted to local start/end of day.

---

## Open TODOs

- [ ] Phase 9: Sync reportes data from Supabase (real-time cobros/pedidos across devices)
