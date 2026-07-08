# Feature: Clientes

## Status: ✅ Done — CRUD, Saldo Extra, Lista Negra, Detalle UI, Reporte de Pedidos complete

---

## Spec summary

Each Mercado contains a list of Clientes. The client row is fully colored by status (red/amber/green wash + left accent bar). Tapping a client opens their detail: balance header, pedidos list, and action buttons including "Agregar a Lista Negra" and "Agregar saldo extra".

---

## Screens

| Screen | Route | File | Status |
|--------|-------|------|--------|
| Lista de Clientes | `ClientesRoute(mercadoId)` | `ui/screen/cliente/ClientesScreen.kt` | ✅ Done |
| Detalle de Cliente | `DetalleClienteRoute(clienteId)` | `ui/screen/cliente/DetalleClienteScreen.kt` | ✅ Done |
| Crear / Editar Cliente | `CreateClienteRoute(mercadoId, clienteId?)` | `ui/screen/cliente/CreateClienteScreen.kt` | ✅ Done |
| Saldo Extra | `SaldoExtraRoute(clienteId)` | `ui/screen/cliente/SaldoExtraScreen.kt` | ✅ Done |
| Reporte de Pedidos | `ReporteClienteRoute(clienteId)` | `ui/screen/reporte/ReporteClienteScreen.kt` | ✅ Done |

---

## Data layer

| File | Status |
|------|--------|
| `domain/model/Cliente.kt` | ✅ |
| `domain/model/ClientStatus.kt` (enum: AL_DIA, ADVERTENCIA, CRITICO) | ✅ |
| `domain/repository/ClienteRepository.kt` | ✅ |
| `data/local/entity/ClienteEntity.kt` | ✅ |
| `data/local/dao/ClienteDao.kt` | ✅ |
| `data/remote/dto/ClienteDto.kt` | ✅ |
| `data/mapper/ClienteMapper.kt` | ✅ |
| `data/repository/impl/ClienteRepositoryImpl.kt` | ✅ |
| `domain/usecase/RefreshClienteDataUseCase.kt` | Refreshes clientes + pedidos in parallel; returns `Boolean` success. Client status is computed from unpaid pedidos, so both must be current together. | ✅ |
| Room migration 5→6 (`MIGRATION_5_6`) | ✅ |
| Room migration 7→8 (`MIGRATION_7_8`) — adds `blacklistBalance` column | ✅ |
| Room migration 11→12 (`MIGRATION_11_12`) — adds `blacklistIsManualAmount` column | ✅ |
| Room migration 12→13 (`MIGRATION_12_13`) — adds `primaryPhoneIndex` column | ✅ |
| `domain/model/PedidoLineItem.kt` | ✅ |
| `data/local/entity/PedidoWithLines.kt` — Room `@Embedded` + `@Relation` POJO | ✅ |
| `ui/screen/reporte/ReporteClienteUiState.kt` | ✅ |
| `ui/screen/reporte/ReporteClienteViewModel.kt` | ✅ |
| `ui/screen/reporte/ReporteClienteScreen.kt` | ✅ |

---

## Role-based access (INVITADO restrictions)

`ClientesUiState` and `DetalleClienteUiState` each carry `canWrite: Boolean`
(computed as `user?.role != UserRole.INVITADO`).

| Element | Who sees it |
|---------|-------------|
| FAB "Nuevo cliente" in `ClientesScreen` | SUPERUSUARIO + USUARIO only |
| Empty-state hint / action button in `ClientesScreen` | SUPERUSUARIO + USUARIO only |
| Edit (pencil) icon in `DetalleClienteScreen` | SUPERUSUARIO + USUARIO only |
| FAB "Nuevo Pedido" in `DetalleClienteScreen` | SUPERUSUARIO + USUARIO only (also hidden when client is blacklisted) |
| `ActionButtons` composable (Agregar a LN, Quitar de LN, Agregar saldo extra) | SUPERUSUARIO + USUARIO only — early `return` when `!canWrite` |

INVITADO users see the full client list, balances, status badges, and pedido history but cannot create clients, create pedidos, blacklist/unblacklist, or add saldo extra.

---

## UI notes (from mockup)

**Client row (inside Mercado) — Fuerte style**:
- Full-row background wash: status tint at 30% alpha (dark) / 22% alpha (light) via `isSystemInDarkTheme()`
- Left accent bar: 6dp wide, solid status color (`redTint α=1`, `amber`, `green`)
- Balance text is colored by status (redText / amberText) when non-zero; `text3` when zero
- Circular avatar (initials, hue-deterministic background via name hash)
- Name + description + status badge + total balance

**Detalle de Cliente header**:
- 76dp circular avatar, name, description
- TopBar action: edit (pencil) icon → navigates to `CreateClienteRoute(mercadoId, clienteId)` for editing
- Tappable phone chip shows the **primary phone** (`phones.getOrElse(primaryPhoneIndex) { phones.firstOrNull() }`). Tap → `Intent(ACTION_DIAL, "tel:$phone")` — opens the device dialer.
- Location chip → `Intent(ACTION_VIEW, Uri.parse(mapsUrl))` — no lat/lng stored
- `BalanceBlock`: unified card that adapts to three states:
  - **Normal / AUTO blacklisted**: status-based `Brush.linearGradient`, label "Saldo total" (was "Saldo pendiente total"), 32sp monospace, `ClienteStatusBadge` on right
  - **MANUAL blacklisted**: red gradient (`rgba(240,90,80,0.16→0.04)`), label "Saldo en Lista Negra", 29sp `redText` monospace, red circular "⊘ Manual" badge on right
- `BalanceCaption`: small info row (info icon + "Monto ingresado al vetar — reemplaza el desglose de abajo.") shown only in MANUAL blacklisted state
- Both the balance amount text and the extra-balance caption row set an explicit `lineHeight` (guards against the wrapped/overlapping-line bug on narrow screens where the default line box was smaller than the font). The extra-balance caption's info icon uses `Alignment.Top` (not `CenterVertically`), so it lines up with the top of multi-line caption text instead of its vertical center.
- `BalanceBreakdown`: breakdown cards shown below the main block in all states:
  - Side-by-side "Pedidos" (blue, `Receipt` icon) + "Saldo extra" (amber, `Tag` icon) cards; both shown as `inactive=true` (gray bg, strikethrough amount, "Congelado" subtitle) when MANUAL
  - Full-width "Lista Negra" (red, `Block` icon) card with `badge="Auto"` only when AUTO blacklisted (`isBlacklisted && !isManualAmount`)
- "Agregar a Lista Negra" button (red-tint, shown only if not blacklisted) → navigates to `AgregarListaNegraRoute`
- "Quitar de Lista Negra" button (surface2, green check icon, shown only if blacklisted) → calls `DetalleClienteViewModel.onQuitarListaNegraClick()`
- "Agregar saldo extra" button — disabled (alpha 0.5, non-clickable) when blacklisted
- FAB "Nuevo Pedido" is hidden when client is blacklisted

**Detalle de Cliente — En Lista Negra state**:
- Banner at top of scrollable content (red-tint bg, ban icon, "En Lista Negra" title + "Vetado desde el [date] · no se pueden crear pedidos nuevos." subtitle)
- Avatar gets a 28dp red circular ban-badge overlay at bottom-right (3dp background-color border ring)
- "Agregar a Lista Negra" button replaced by "Quitar de Lista Negra" (surface2, green CheckCircle icon)
- "Agregar saldo extra" button is `alpha(0.5f)` and non-clickable
- When `blacklistIsManualAmount == true` (MANUAL): `BalanceBlock` shows the red variant with `blacklistBalance` amount; `BalanceCaption` info line shown; breakdown cards appear in `inactive=true` state (gray, strikethrough, "Congelado")
- When `blacklistIsManualAmount == false` (AUTO): `BalanceBlock` shows normal status gradient; an extra full-width "Lista Negra" `BalanceCard` is shown in the breakdown with the total balance and `badge="Auto"`
- Tapping "Quitar de Lista Negra" when `blacklistIsManualAmount == true` opens `QuitarListaNegraSheet` with two options:
  - **Restaurar datos** — clears the blacklist; pedidos unchanged
  - **Marcar todo como pagado** — bulk-marks all non-PAID pedidos as PAID via `PedidoDao.markAllPaidForCliente`. If `blacklistBalance > (pedidosBalance + extraBalance)`, a new saldo extra is created for the difference via `CreateSaldoExtraUseCase`. Then clears the blacklist.
- When `blacklistIsManualAmount == false` (AUTO), "Quitar de Lista Negra" unblacklists immediately with no sheet.

**"Cuenta" section** (formerly "Pedidos"):
- Section label renamed to "Cuenta"
- Three-dot overflow menu (`PedidosMenuButton`) at the right of the section header:
  - "Generar reporte" item — accent-tinted icon tile (`Description` icon), clickable, navigates to `ReporteClienteRoute(clienteId)`
  - "Filtrar por estado" sub-section with Pendiente / Parcial / Pagado toggle items
  - "Restablecer (todos)" item (enabled only when filters are active)
- When filters are active: colored status chips displayed below the section header (`FilterChipsRow`), each showing a 7dp status-color dot + label, plus a "✕ Limpiar" clear chip; section header also shows "N de M" count
- Filter state (`pedidoFilters: Set<PedidoStatus>`) lives in the ViewModel; `allPedidosCount` is the total unfiltered count

**Pedidos list** (Phase 4 — ✅ done): `PedidoRow` composable per pedido. Shows date, product count, total (mono), `PayChip`. Empty state shown when no pedidos exist (when filters active, empty state hides the "Nuevo pedido" hint).

**Expandable product panel** (post-Phase 7 — ✅ done):
- Regular pedido rows show a `CaretButton` (42×42dp, 12dp radius) instead of the receipt icon tile. Tapping it expands/collapses a `PedidoLinesPanel` below the row with a 180° animated caret rotation (`animateFloatAsState`, 180ms tween).
- Expanded button style: `accentSoft` bg + `primary` border + `primary` tint; collapsed: `surface3` bg + `border` + `text2` tint.
- `PedidoLinesPanel`: surface-color card with border inset, 10.5sp uppercase "N PRODUCTOS" header, then `HorizontalDivider`-separated rows showing `productName` (ellipsized) + mono `×quantity`. Left-padded 75dp to align under row text content.
- Saldo-extra rows are unchanged (amber Tag tile, amber bg tint, no expandable behavior).
- `DetalleClienteViewModel` uses `pedidoRepository.getByClienteWithLines()` so every pedido carries its `PedidoLineItem` list reactively.

**Search**: `OutlinedTextField` bar revealed via `AnimatedVisibility` when the search icon is tapped. `searchQuery` lives in `ClientesViewModel`; filtering is applied inside the `combine` block before the list reaches the UI.

**Filter menu**:
- A → Z (default), Críticos primero, Mayor saldo, Solo con deuda, Restablecer
- Implemented with Material3 `DropdownMenu` (Popup-backed) so it renders outside the layout hierarchy and is never clipped by the top bar's actions slot

---

## Implementation notes

- `phones` stored as pipe-separated string in Room (`"0414-123|0424-456"`); mapped to `List<String>` in domain.
- `primaryPhoneIndex` (Int, default 0) identifies which phone in the list is the primary contact. Stored in Room as `INTEGER NOT NULL DEFAULT 0`. On save, clamped to `min(index, phones.size - 1)` to handle blank-phone filtering. Removing a phone before the primary index shifts the index down; removing the primary resets to 0.
- Location is **only** a URL (`mapsUrl`) — no lat/lng. User pastes the link; tapping opens the device map app.
- Row color uses the **Fuerte** variant from the design: `bgAlpha = if (isDark) 0.30f else 0.22f`, bar 6dp, balance colored by status.
- Balance and status are now computed live in `DetalleClienteViewModel` by `combine`-ing `clienteFlow + pedidosFlow`. Balance = sum of pending amounts across non-PAID pedidos. See **Client Status Thresholds** section below.
- `ClienteAvatar` accepts an optional `photoUrl`; when set it renders the photo (via `PhotoThumbnail`) while preserving the status ring. Falls back to initials with deterministic `hsl(nameHash % 360, 32%, 26%)` bg.
- `PhoneListField` (in `CreateClienteComponents.kt`): redesigned phone editor. Each `PhoneRow` is a custom 52dp-min-height card (`surface2` bg, 14dp corners) with:
  - **Radio circle** (24dp, filled primary + Check icon when primary; outlined `text3` when not) — tap to set this phone as primary.
  - **Phone icon** + **`BasicTextField`** (15.5sp Monospace Medium).
  - **"PRINCIPAL" badge** (10.5sp, uppercase, primary text, `accentTint` bg, 6dp radius) — shown only on the primary row.
  - **Call button** (34dp, `Call` icon) — shown only in **edit mode** when the field has a value; launches `Intent(ACTION_DIAL)`.
  - **Delete button** (34dp, `Close` icon) — alpha 0.35 and disabled when only one phone exists.
  - Border: 1.5dp `primary` when primary row, 1dp `border2` otherwise.
  - Info hint below the label: "El teléfono **principal** es el que aparece en el detalle del cliente."
  - "Agregar otro teléfono" is a bordered pill row (not a `TextButton`).
- `CirclePhotoPicker` (in `CreateClienteComponents.kt`): 96dp circle picker used in create/edit forms. Create mode shows a `Person` icon placeholder; edit mode shows the existing `ClienteAvatar` when no new photo is picked; both modes use `BitmapFactory`/`LaunchedEffect` to render the actual photo once selected. Camera button (32dp) always present at bottom-right.
- `CreateClienteViewModel`: `init` block restores `photoUri` from `c.photoUrl` when editing; `onSave` includes `photoUrl = s.photoUri?.toString()` when saving the `Cliente`.
- Gallery picks are copied to `cacheDir/images/` via `copyImageToCache()` on selection (same as other photo flows).
- `formatBalance` is `internal fun` defined in `ClientesScreen.kt`, shared with `DetalleClienteScreen.kt` via module scope.
- `ClienteRepository` exposes `getBlacklisted()`, `blacklist(id, reason, balance, at)`, and `unblacklist(id)`. All implemented in `ClienteRepositoryImpl` and delegated to `ClienteDao`.
- `ClienteDao.unblacklist` resets `isBlacklisted=0`, `blacklistReason=NULL`, `blacklistBalance=0`, `blacklistedAt=NULL`.
- `DetalleClienteViewModel` 5-flow `combine`: `clienteFlow + pedidosFlow + umbralesFlow + _showUnblacklistSheet + _pedidoFilters`. Computes `balance` (for status), `pedidosBalance` (non-PAID regular pedidos), `extraBalance` (non-PAID saldo-extra), `unpaidPedidosCount`, `unpaidExtraCount`, filtered `pedidos` list, and `allPedidosCount`.
- `DetalleClienteViewModel` exposes `onTogglePedidoFilter(PedidoStatus)` and `onClearPedidoFilters()` for the filter menu.
- `DetalleClienteScreen.onListaNegraClick` navigates to `AgregarListaNegraRoute(clienteId)`. After confirming blacklist, navigation pops back to `DetalleClienteScreen`, which reactively switches to the blacklisted state.
- `DetalleClienteScreen.onQuitarListaNegraClick` calls `viewModel.onQuitarListaNegraClick()`:
  - If `cliente.blacklistIsManualAmount == false`: unblacklists immediately (AUTO amount — no ambiguity).
  - If `cliente.blacklistIsManualAmount == true`: opens `QuitarListaNegraSheet` with two options:
    - **Restaurar datos** → clears the blacklist; pedidos are unchanged.
    - **Marcar todo como pagado** → marks all existing non-PAID pedidos as PAID; if `blacklistBalance > (pedidosBalance + extraBalance)`, calls `CreateSaldoExtraUseCase` for the difference; then clears the blacklist.
- `BalanceBlock` is a unified composable; `isManualBlacklisted=true` switches it to the red gradient / "Saldo en Lista Negra" / Manual-badge variant.
- `BalanceBreakdown` always rendered; shows `inactive=true` cards when MANUAL, extra full-width LN card when AUTO blacklisted.
- `BlacklistBalanceBlock` has been removed — its purpose is now served by the unified `BalanceBlock` design.
- `ClientesScreen` and `MercadosScreen` "Lista Negra" buttons navigate to `ListaNegraRoute`.

---

## SaldoExtra screen

Split across two files: `SaldoExtraScreen.kt` (scaffold, save bar, previews) + `SaldoExtraFields.kt` (all field composables, previews).

**AppBar**: `PedidosTopBar` — back arrow + title "Agregar saldo extra" + client name subtitle + Close (×) icon action (both back and close pop the stack).

**Content column**: `Arrangement.spacedBy(18.dp)`, `padding(horizontal = 20.dp, top = 8.dp, bottom = 24.dp)`. Fields in order:

| Field | Composable | Notes |
|-------|-----------|-------|
| Categoría | `SaldoExtraCategoryField` | Locked row: 30×30 amber-tint tile (`amberTint` bg, 9dp radius, `Tag` icon 17dp) + "Saldo" (15.5sp SemiBold) + `Check` icon + "Fijo" (11.5sp, text3); `surface` bg, `border` inset |
| Monto | `SaldoExtraAmountHero` | Centered hero: "Monto" label (13sp, text2) → `BasicTextField(wrapContentWidth)` with "Bs." (22sp, text2) in `decorationBox` + 46sp mono Bold letterSpacing −1.5sp; 2dp accent underline (180dp, centered); turns error-red when `amountError` |
| Descripción | `SaldoExtraDescriptionField` | Label "Descripción *" (required asterisk in accent); `OutlinedTextField`, `surface2` bg, `border2` inset when non-empty, `88dp` min height |
| Fecha | `SaldoExtraDateField` | Tappable `surface2` row; date formatted `"d 'de' MMMM 'de' yyyy"` (Spanish locale); opens `DatePickerDialog` |

**Bottom bar**: `SaldoExtraSaveBar` — "Registrar saldo" CTA (52dp, 15dp radius); spinner while saving; disabled until `canSave`.

Each composable gets its own label above (13sp, `FontWeight.Medium`, `text2`) via the private `SaldoExtraFieldLabel` helper; required fields get an accent ` *` suffix.

---

## Client Status Thresholds

Computed live from `clienteFlow + pedidosFlow + umbralesFlow` (three-way `combine`). Thresholds are configurable by superusers from **Mi Perfil → Ajustes → Umbrales de estado**.

**Display balance** (`balance` field, shown in `BalanceBlock`) = `pedidosBalance + extraBalance`, i.e. the sum of the two breakdown cards below it:
- `pedidosBalance` = sum of `pending` for regular (non-saldo-extra) pedidos with `status != PAID` (PENDING or PARTIAL)
- `extraBalance` = sum of `pending` for saldo-extra pedidos with `status != PAID`

> Fixed bug: `balance` used to be computed independently as `Σpending` for `status == PARTIAL` (any kind) OR `status == PENDING && isSaldoExtra`, which silently excluded regular `PENDING` pedidos — a client with only a brand-new, untouched pedido showed "Saldo Total: Bs. 0" while the "Pedidos" breakdown card below correctly showed its amount. `balance` is now just `pedidosBalance + extraBalance`, guaranteeing the total always matches the sum of the two cards under it.

**Status balance** (`statusBalance`, used only for status computation) = sum of `pending` for:
- `status == PARTIAL && !isSaldoExtra`

> Saldo-extra entries are excluded from the status calculation. A client with only saldo-extra debt is shown as `AL_DIA` in the row badge and gradient — the saldo amount is still visible in the display balance but does not drive the warning/critical color.
>
> **Verified** (already correct, no change needed): `statusBalance` is pedidos-only in both `DetalleClienteViewModel.computeStatus` and `ClientesViewModel.computeStatus` — saldo-extra pedidos never affect the status badge/gradient/color in either place.

**Status rules** (evaluated against `statusBalance`):

| Status (enum) | Display label | Condition | Color |
|--------|-------|-----------|-------|
| `AL_DIA` | "Al día" | `statusBalance == 0` | green |
| `CRITICO` | "Crítico" | `statusBalance > montoMaximo` OR any `PARTIAL && !isSaldoExtra` pedido has `createdAt` older than `diasMaximos` days | red |
| `ADVERTENCIA` | "Cobrar" | `statusBalance > 0` and neither CRITICO condition applies | amber |

> The days check only applies to regular (non-saldo-extra) PARTIAL pedidos.
>
> The `ADVERTENCIA` enum constant's display label was renamed from "Advertencia" to **"Cobrar"** (`R.string.status_advertencia`) to match the Claude Design source (`kit.jsx`'s `STATUS.warn.label`). The enum name itself is unchanged. Two duplicate string sets that had drifted apart in wording were consolidated into the single canonical `status_al_dia`/`status_advertencia`/`status_critico` trio (previously `BusquedaScreen.kt` had its own `cliente_status_*` copies and `UmbralesScreen.kt` had `umbrales_status_*` copies — both deleted, both screens now read the shared strings) so the label can't drift out of sync again.

**Threshold defaults**: `montoMaximo = 200.0 Bs`, `diasMaximos = 30 days`.

**"Older than N days"**: `(System.currentTimeMillis() - pedido.createdAt) > days.toLong() * 24 * 60 * 60 * 1000`

**Implementation**:
- `DetalleClienteViewModel.kt` — `computeStatus(balance, pedidos, umbrales)` + `isOlderThan(createdAt, days)`
- `ClientesViewModel.kt` — same logic applied over `getAllUnpaid()` grouped by `clienteId`
- `UmbralesManager.kt` — `@Singleton` `StateFlow<Umbrales>` backed by `SharedPreferences`; all injecting ViewModels recompute automatically on threshold change

---

## Reporte de Pedidos

**Route**: `ReporteClienteRoute(clienteId: String)` → `ui/screen/reporte/ReporteClienteScreen.kt`

**Entry point**: "Generar reporte" item in `PedidosMenuButton` dropdown.

**Screen layout**:
- `TopAppBar`: "Reporte de pedidos" title + client name subtitle + back arrow.
- Scrollable body with four sections: Encabezado, Cliente info, Resumen, Pedidos list.
- Bottom export bar (above `navigationBarsPadding`) with full-width "Exportar PDF" button.

**Encabezado card**: `accentSoft` bg, `Description` icon in `accentTint` tile, company name + generated date.

**Cliente info section**: 2×2 grid of `ReporteInfoItem` (label in `text4` uppercase, value in 13.5sp SemiBold): Nombre, Mercado, Descripción, Teléfono.

**Resumen section**: 3 side-by-side `ReporteResumenCard` composables — "Sin pagar" (blue), "Saldo pendiente" (amber), "Total pedidos" (green). Balance uses same computation as `DetalleClienteViewModel`.

**Pedidos list**: Card container per pedido (`ReportePedidoRow`) showing date, product count, product names summary (inline), total/pending amounts, and `PayChip`. Saldo-extra rows use amber tint background.

**PDF export**: On "Exportar PDF" button tap, `buildReporteHtml(state, date)` generates a self-contained HTML string with inline CSS. A `WebView` loads it via `loadDataWithBaseURL`, then `WebViewClient.onPageFinished` triggers `PrintManager.print()` using `webView.createPrintDocumentAdapter()`. The system print dialog handles save-to-PDF / share.

**ViewModel** (`ReporteClienteViewModel`):
- 3-flow `combine`: `clienteFlow + pedidosWithLinesFlow + _mercadoName`.
- `_mercadoName` is a `MutableStateFlow<String>` seeded in `init` via a one-shot `getByIdFlow(...).first { it != null }` + `mercadoRepository.getById(...)`.
- `balance` = sum of `pending` for PARTIAL pedidos + PENDING saldo-extras.
- `unpaidCount` = count of non-saldo-extra pedidos that are not PAID.

## Open TODOs

- [x] Implement `SaldoExtraScreen` (pre-filled category "Saldo", description, amount, date)
- [x] Add `SaldoExtraRoute` to `AppRoutes.kt` and `AppNavigation.kt`
- [x] Phase 4: real balance/status computed from pedidos in `DetalleClienteViewModel`
- [x] Phase 7: Lista Negra state in `DetalleClienteScreen` — banner, avatar badge, button swap, FAB hidden, "Quitar de Lista Negra" action
- [x] Reporte de Pedidos — full screen with PDF export via `WebView` + `PrintManager`
- [x] Primary phone: mark one phone as primary in create/edit forms; shown in detalle header; call button per row in edit mode (DB v13)
