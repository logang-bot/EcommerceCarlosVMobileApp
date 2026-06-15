# Feature: Pedidos

## Status: ✅ Done (Phase 4 + Phase 5 + Phase 6)

---

## Spec summary

Two sub-features:
1. **Creación de Pedido** *(Phase 4 — ✅ Done)* — "add to cart" flow: product grid, inline quantity controls, cart panel, price-override warning, confirm + payment sheet.
2. **Detalle de Pedido** *(Phase 5 — 🔲 Pending)* — view line items, payment history, mark as paid or register partial payment.

---

## Role-based access (INVITADO restrictions)

`DetallePedidoUiState` carries `canWrite: Boolean` (computed as `user?.role != UserRole.INVITADO`).

| Element | Who sees it |
|---------|-------------|
| Edit (pencil) icon in `DetallePedidoScreen` top bar | SUPERUSUARIO + USUARIO only |
| Bottom bar ("Pago parcial" + "Marcar pagado") in `DetallePedidoScreen` | SUPERUSUARIO + USUARIO only (also hidden when `status == PAID`) |

INVITADO users can view pedido details and payment history but cannot record payments or navigate to the edit screen.

Note: `CreacionPedidoScreen` is never reachable for INVITADO because the "Nuevo Pedido" FAB in `DetalleClienteScreen` is already hidden by `canWrite` (see `docs/features/clientes.md`).

---

## Screens

| Screen | Route | File | Status |
|--------|-------|------|--------|
| Creación de Pedido | `CreacionPedidoRoute(clienteId, clienteName, mercadoName)` | `ui/screen/pedido/CreacionPedidoScreen.kt` | ✅ Done |
| Detalle de Pedido | `DetallePedidoRoute(pedidoId)` | `ui/screen/pedido/DetallePedidoScreen.kt` | ✅ Done |
| Editar Pedido | `EditarPedidoRoute(pedidoId)` | `ui/screen/pedido/EditarPedidoScreen.kt` | ✅ Done |

---

## Data layer

| File | Status |
|------|--------|
| `domain/model/Pedido.kt` | ✅ |
| `domain/model/DetallePedido.kt` | ✅ |
| `domain/model/PedidoStatus.kt` (enum: PENDING, PARTIAL, PAID) | ✅ |
| `domain/repository/PedidoRepository.kt` | ✅ |
| `data/local/entity/PedidoEntity.kt` | ✅ |
| `data/local/entity/DetallePedidoEntity.kt` | ✅ |
| `data/local/dao/PedidoDao.kt` | ✅ |
| `data/local/dao/DetallePedidoDao.kt` | ✅ |
| `data/remote/dto/PedidoDto.kt` | ✅ |
| `data/remote/dto/DetallePedidoDto.kt` | ✅ |
| `data/mapper/PedidoMapper.kt` | ✅ |
| `data/mapper/DetallePedidoMapper.kt` | ✅ |
| `data/repository/impl/PedidoRepositoryImpl.kt` | ✅ |
| `domain/usecase/CreatePedidoUseCase.kt` | ✅ |
| `domain/usecase/CreateSaldoExtraUseCase.kt` | ✅ |
| Room migration 8→9 (`MIGRATION_8_9`) — creates `pedidos` + `detalle_pedido` | ✅ |
| Room migration 9→10 (`MIGRATION_9_10`) — adds `isSaldoExtra` column to `pedidos` | ✅ |
| Room migration 10→11 (`MIGRATION_10_11`) — adds `itemCount INTEGER NOT NULL DEFAULT 0` to `pedidos` | ✅ |
| `PedidoDao.updateDate(id, createdAt)` — `UPDATE pedidos SET createdAt = :createdAt` | ✅ |
| `PedidoDao.updateAfterEdit(id, total, itemCount, status, paid, paidAt)` — single query updating all five fields atomically | ✅ |
| `DetallePedidoDao.getByPedidoFlow(pedidoId)` — `Flow<List<DetallePedidoEntity>>` for reactive line-item observation | ✅ |
| `PedidoRepository.updateDate(id, createdAt)` + `PedidoRepositoryImpl` impl | ✅ |
| `PedidoRepository.updateLines(pedidoId, detalles, newTotal, paid, paidAt)` — atomic line + total update | ✅ |
| `PedidoRepository.getDetallesByPedidoFlow(pedidoId)` — reactive flow counterpart to `getDetallesByPedido` | ✅ |

---

## UI — Phase 4: Creación de Pedido

### Screen structure

`CreacionPedidoScreen` uses a `Scaffold` with:
- `topBar` = `CreacionTopBar` (title "Nuevo pedido", subtitle "clienteName · mercadoName") + optional `SearchBar` injected below when search is active
- `bottomBar` = `CartPanel` (always visible; Scaffold automatically pads the content for its dynamic height)
- content = `ProductGridSection` (fills the padded area)

### Files

| File | Responsibility |
|------|---------------|
| `ProductGridSection.kt` | 3-column `LazyVerticalGrid` — search cell (fixed first), `ProductCard` per producto |
| `CartPanel.kt` | Persistent bottom panel — drag handle, cart rows, confirm CTA |
| `LineEditSheet.kt` | `ModalBottomSheet` — edit qty/price/notes for a cart line |
| `PagoSheet.kt` | `ModalBottomSheet` — select payment type (paid/partial/pending) and submit |
| `CartItem.kt` | UI-only data class for in-progress order lines |
| `CreacionPedidoUiState.kt` | Immutable UI state; exposes `filteredProductos`, `cartTotal`, `cartQuantityFor()` |
| `CreacionPedidoViewModel.kt` | `@HiltViewModel`; drives all user interactions |

`CreacionPedidoScreen` delegates its `Scaffold` to a private `CreacionPedidoContent(state, callbacks…)` composable; the screen entry point retains only the `LaunchedEffect` navigation side-effect and bottom-sheet overlays (`LineEditSheet`, `PagoSheet`).

### ProductGridSection

- First cell always: `SearchCell` (search icon + "Buscar producto") — tapping activates the inline search bar on the top bar
- While search is active: `SearchCell` is replaced by the regular grid filtered by `searchQuery`; `CartPanel` stays visible
- `ProductCard` non-cart state: product icon tile + name + price (mono)
- `ProductCard` in-cart state: green ring border, checkmark badge, `QuantityStepper` (− / qty / +) in place of price

**`SearchCell` sizing**: mirrors `ProductCard`'s column structure — same 72dp top `Box` (surface3 bg) containing a 38×38 icon box, same `padding(horizontal=8.dp, vertical=7.dp)` content column with a fixed-height label — ensuring both cells have the same intrinsic height within the grid row.

### CartPanel

- `DragHandle` pill at top
- Header row: cart icon + "Carrito" + "· N productos"
- Cart rows (`heightIn(max = 132dp)` scrollable): `×qty` label, name, unit info, subtotal, ×-remove; tapping row calls `onEditItem`
- Empty state: Row with 38×38 icon tile (surface2 bg, 1dp border, `ShoppingCart` icon in text4) + "Agrega productos al pedido"
- Confirm CTA (`height = 54dp`, `borderRadius = 15dp`): disabled (surface3 bg) when cart empty; enabled (primary bg) when filled — shows total amount (mono) + chevron icon in a 34×34 rounded box (`rgba white 18%` bg)

### LineEditSheet

- Product header: 48dp icon tile + name + "Precio de catálogo · Bs. X.XX"
- Quantity stepper: 56dp container, `surface2` bg, minus/number/plus
- Unit price field: `OutlinedTextField`; amber border when price ≠ catalogPrice
- Amber disclaimer banner (`PriceModifiedBanner`): shown when unit price is modified, displays both catalogue and new price
- Notes field: optional multiline (min 2 lines)
- Subtotal row: live `unitPrice × quantity`
- "Confirmar" primary CTA

### PagoSheet

- Header: "Confirmar pedido" title (centered), total in large mono (centered), "N productos · clienteName" (centered)
- Three `PaymentOptionRow` options — each has a 40×40 icon tile (rounded 12dp, tint bg), title/subtitle text, and a 20dp radio indicator circle:
  - **Marcar como pagado** — `Check` icon, `green`/`greenTint` colors; radio filled with `green` when selected
  - **Pago parcial** — `Payments` icon, `primary`/`accentTint` colors; reveals `PartialAmountInput` below when selected
  - **Dejar pendiente** — `Tag` icon, `amber`/`amberTint` colors
  - **Selected state**: row background = tint, 1.5dp colored border, radio filled with option color + white check
  - **Unselected state**: row background = `surface2`, 1dp neutral `border`, radio = transparent + `border3` ring
- `PartialAmountInput`: custom styled row (54dp, `surface2` bg, 1.5dp accent border, `BasicTextField` with mono style, "Restan Bs. X.XX" suffix); `initialPayment = enteredAmount`
- "Registrar pedido" CTA — shows `CircularProgressIndicator` while saving; **disabled when "Pago parcial" is selected and the amount field is empty or zero** (`canConfirm = selected != PARTIAL || partialAmount > 0`)

### CreatePedidoUseCase

Single atomic operation: computes total → derives `PedidoStatus` → creates `Pedido` domain object → maps items to `DetallePedido` list → calls `pedidoRepository.create(pedido, detalles)`.

Status rules:
- `initialPayment >= total` → `PAID`
- `initialPayment > 0` → `PARTIAL`
- `initialPayment == 0` → `PENDING`

---

## Shared component

`ui/common/PayChip.kt` — reusable status pill used on `PedidoRow` (Detalle de Cliente) and future `DetallePedidoScreen`:

| Status | Background | Text color |
|--------|-----------|------------|
| PAID | `greenTint` | `greenText` |
| PARTIAL | `accentSoft` | `primary` |
| PENDING | `amberTint` | `amberText` |

---

## DetalleClienteScreen — Phase 4 updates

- `DetalleClienteViewModel` now `combine`s `clienteRepository.getByIdFlow` + `pedidoRepository.getByCliente` to compute real balance and status
- **Balance** = sum of `pending` for pedidos where `status == PARTIAL` OR (`status == PENDING && isSaldoExtra`). Regular PENDING pedidos are NOT counted — they represent unconfirmed orders, not actual debt. Saldo-extra entries always count since they are deliberate debt records.
- **Status**: `AL_DIA` if balance == 0; `CRITICO` if balance > 200 or any balance-contributing pedido is older than 30 days; `ADVERTENCIA` otherwise
- Empty state shown when no pedidos exist
- FAB navigates to `CreacionPedidoRoute(clienteId, clienteName, mercadoName)`

### BalanceBlock staleness fix (Phase 6)

`DetalleClienteViewModel` uses `SharingStarted.Eagerly` (not `WhileSubscribed`) so the upstream `combine` is always active while the ViewModel is alive. This ensures that when `DetallePedidoViewModel` writes a partial payment to the DB, Room notifies the running flow and `BalanceBlock` is updated before the user even navigates back.

### PedidoRow redesign (Phase 6)

`PedidoRow.kt` was refactored into focused sub-composables:

| Composable | Responsibility |
|---|---|
| `PedidoRow` | Entry point — routes to `OrderRowContent` or `SaldoExtraRowContent` |
| `PedidoIconTile` | 42×42 rounded tile: `surface3`/`amberTint` bg, `Receipt`/`Tag` icon |
| `OrderRowContent` | Regular order layout: date as primary text, "{n} productos" subtitle, amount column |
| `SaldoExtraRowContent` | Saldo layout (unchanged): "Saldo extra" + Manual badge, date + notes subtitle |
| `PartialAmountDisplay` | Strikethrough total (`text3`) + remaining in `blueText` (baseline-aligned) |
| `ManualBadge` | Amber "MANUAL" label pill |

**Row layout — regular order:**
- Primary text: formatted `createdAt` date (14.5sp, SemiBold)
- Subtitle: `"{n} productos"` using `pedido.itemCount` (12.5sp, `text3`)
- Amount — normal: total (14.5sp, SemiBold, mono)
- Amount — partial: `total` with `TextDecoration.LineThrough` in `text3` (12.5sp) + `pending` in `blueText` (14.5sp, Bold, baseline-aligned)
- `PayChip` below the amount

**Row layout — saldo extra:** unchanged from prior design.

**Row separators:** `PedidosSection` now renders a `HorizontalDivider` (`border` color, `start = 75.dp` padding to align with content, skipping the icon column) between rows.

**`itemCount` field:** Added `itemCount: Int = 0` to `Pedido` domain model and `PedidoEntity`. `CreatePedidoUseCase` sets it to `items.size`. Saldo-extra pedidos always default to 0. Existing rows migrate to 0 via `MIGRATION_10_11` (accurate counts can only be backfilled from `detalle_pedido`). New pedidos have the correct count from creation.

**`blueText` color:** Added to `PedidosExtendedColors` (`DarkBlueText = #7FB0FF`, `LightBlueText = #1D55C2`) — used exclusively for partial-payment remaining amounts.

---

## UI — Phase 5: Detalle de Pedido

### Files

| File | Responsibility |
|------|---------------|
| `DetallePedidoUiState.kt` | `pedido`, `detalles`, `clienteName`, `isLoading`, `isSaving`, `showPagoSheet` |
| `DetallePedidoViewModel.kt` | `@HiltViewModel`; nested `combine` (4 flows); loads `clienteName` via `ClienteRepository`; `onMarcarPagado()`, `onRegistrarPago(amount)`, pago sheet toggles |
| `DetallePedidoScreen.kt` | Scaffold entry + content, `PedidoStatusStrip`, `SaldoExtraBody`; `Edit` pencil icon in top-bar `actions` navigates to `EditarPedidoRoute` |
| `DetallePedidoActions.kt` | `DetallePedidoBottomBar`, `PagoParcialSheet`, `PagoParcialSheetContent` |
| `DetallePedidoLineItem.kt` | `LineItemsSection`, `LineItemRow`, `PriceModifiedHint` |
| `DetallePedidoSummary.kt` | `TotalBlock`, `PagosSection`, `DetalleSectionLabel` |
| `EditarPedidoUiState.kt` | `EditLineState` (mutable line), `EditarPedidoUiState` with `editedDate`, `lines`, `editingLineIndex`, `showPaymentSheet`, computed `newTotal` |
| `EditarPedidoViewModel.kt` | `@HiltViewModel`; loads pedido + detalles + clienteName once (one-shot, not flow); line mutation in-memory; `onShowPaymentSheet()` → sheet → `onSave(payment)` writes via `updateLines` + `updateDate` if date changed |
| `EditarPedidoScreen.kt` | Scaffold entry + content, `PagoInfoBanner`, `DateField`, `LinesSection`, `EditOrderLineRow`, `LineQuantityStepper`, `DangerZone`, `EditarPedidoBottomBar`, `EditLineSheet` (price/notes bottom sheet); "Guardar cambios" triggers `PagoSheet` with current status pre-selected |
| `ui/common/PagoSheet.kt` | Shared payment-type selector (`PagoSheet`): 3 options (PAID/PARTIAL/PENDING) with normalized status colors (green/blue/amber); used by both CreacionPedidoScreen and EditarPedidoScreen; accepts `initialStatus` + `initialPaidAmount` for pre-selection and `ctaLabel` for context-specific CTA text |

### Screen structure

`DetallePedidoScreen` uses a `Scaffold` with:
- `topBar` = `PedidosTopBar` (title "Pedido", subtitle = formatted date — e.g. "28 may 2026") + `Edit` `IconButton` in `actions` → navigates to `EditarPedidoRoute(pedidoId)`
- `bottomBar` = `DetallePedidoBottomBar` — hidden when `status == PAID`; "Pago parcial" (outlined, `Add` icon) + "Marcar pagado" (`primary` bg, `Check` icon); both use `heightIn(min = 50.dp)` to avoid text clipping
- content = scrollable `Column`: `PedidoStatusStrip` → `HorizontalDivider` → body → `HorizontalDivider` → `TotalBlock` → (if `paid > 0`) `HorizontalDivider` + `PagosSection`

**`PedidoStatusStrip`**: `Row` with `PayChip` + subtitle text.
  - Normal: `"N productos · clienteName"` (pluralStringResource)
  - Saldo extra: `"Saldo extra · clienteName"`

Body adapts by pedido type:
- **Normal pedido**: `LineItemsSection` — `LineItemRow` per `DetallePedido`: 38×38 product tile (surface3 + Sell icon), product name (bold), `×qty · Bs. X.XX`, optional strikethrough catalogPrice + amber dot if `isPriceModified`, subtotal (mono), optional italic notes; `PriceModifiedHint` (amber pill) kept from original design
- **Saldo extra** (`isSaldoExtra = true`): `SaldoExtraBody` — shows `notes` description in a styled block

### TotalBlock

| Row | Shown when | Color |
|-----|-----------|-------|
| "Total del pedido" + total | always | `text2` |
| "Pagado" + paid | `paid > 0` | `greenText` |
| divider + "Saldo restante" + pending | always | `amberText` (if pending > 0) / `text2` |

### PagosSection

Shown when `paid > 0`. Single-row simplified history (no separate `pagos` table exists): 30×30 `greenTint` tile + `Check` icon + date (`paidAt ?: createdAt`, formatted "dd MMM yyyy" in Spanish) + "+ Bs. X.XX" in `greenText` mono. A future `pagos` table would enable full per-payment history.

### Edit icon (top-bar action)

Simple `IconButton` with `Icons.Default.Edit` in the `PedidosTopBar` `actions` slot. Tapping navigates to `EditarPedidoRoute(pedidoId)` using `state.pedido?.id`.

### PagoParcialSheet

`ModalBottomSheet` (experimental): title + remaining balance label, `OutlinedTextField` with `Bs.` prefix + decimal keyboard, "Registrar pago" CTA.

Validation mirrors `PagoSheet` in Creación de Pedido:
- `isAmountEmpty` — blank input
- `isAmountTooHigh` — entered amount exceeds remaining balance
- `canConfirm = !isAmountEmpty && !isAmountTooHigh`
- Button is always tappable but alpha-dimmed to 50% when `!canConfirm`; tapping while invalid sets `showError = true`
- `isError` on `OutlinedTextField` + error text below (reuses `pedidos_pago_parcial_error_vacio` / `pedidos_pago_parcial_error_maximo` strings)
- `LaunchedEffect(amountText)` resets `showError` on every keystroke

### ViewModel payment logic

`onMarcarPagado()` — sets `status = PAID`, `paid = total`, `paidAt = now`.

`onRegistrarPago(amount)` — `newPaid = (paid + amount).coerceAtMost(total)`; status becomes `PAID` if `newPaid >= total`, `PARTIAL` otherwise; `paidAt = System.currentTimeMillis()` always set (so `PagosSection` shows the payment date, not the pedido creation date).

---

## UI — Phase 6: Editar Pedido

### Navigation

`DetallePedidoScreen` → `Edit` icon (top-bar) → `EditarPedidoRoute(pedidoId)` → `EditarPedidoScreen`. On save, `popBackStack()` returns to `DetallePedidoScreen`, which auto-refreshes because `DetallePedidoViewModel` combines two reactive Room flows — `getByIdFlow` (pedido) and `getDetallesByPedidoFlow` (line items) — so both the payment state and line items reflect writes from `EditarPedidoViewModel.onSave()` without any explicit signaling.

On delete from `EditarPedidoScreen`: two `popBackStack()` calls — first pops `EditarPedidoScreen`, second pops `DetallePedidoScreen` — landing back on the client detail.

### Screen structure

`EditarPedidoScreen` uses a `Scaffold` with:
- `topBar` = `PedidosTopBar` (title "Editar pedido", subtitle = "date · clienteName")
- `bottomBar` = `EditarPedidoBottomBar` — "Nuevo total" amount on the left + "Guardar cambios" `Button` with `Check` icon; tapping opens the `PagoSheet` overlay (preselected to current pedido status)
- content = scrollable `Column`:
  - `PagoInfoBanner` — blue-tint info banner shown when `pedido.paid > 0`; describes the status and abonado amount
  - `DateField` — section label + 50dp tappable row (surface2 bg, border2, calendar icon + date text + chevron) → `DatePickerDialog`
  - `LinesSection` — section label "N productos" + `EditOrderLineRow` per line (divided by start-inset `HorizontalDivider`)
  - `DangerZone` — `HorizontalDivider` + "Eliminar pedido" red-tint button + helper text → `AlertDialog` confirmation

### EditOrderLineRow

Each line:
- 40×40 product tile (surface3, border, Sell icon)
- Name + tappable price row (opens `EditLineSheet`): unit price in mono, amber if modified + strikethrough catalog price, `Edit` icon in accent
- Subtotal (mono, right-aligned)
- Below: `LineQuantityStepper` (38dp, surface2 + border2, minus/qty/plus) + "Quitar" button (redTint bg, redText, trash icon) + optional italic notes block

### EditLineSheet

`ModalBottomSheet` (skipPartiallyExpanded):
- Product header (48dp tile + name + "Precio de catálogo · Bs. X.XX")
- Unit price `OutlinedTextField` with `Bs.` prefix
- `PriceModifiedBanner` (amber tint, Tag icon) shown when price ≠ catalogPrice
- Notes `OutlinedTextField` (optional, 2–4 lines)
- Subtotal row
- "Confirmar" CTA → `onSave(price, notes)`

### EditarPedidoViewModel save flow

1. User taps "Guardar cambios" → `onShowPaymentSheet()` → `PagoSheet` overlay appears pre-selected with `pedido.status` and `pedido.paid` prefilled in the partial field
2. User confirms a payment option → `onSave(payment: Double, onSuccess)`
3. `onSave`: saves date if changed, calls `updateLines` with the chosen payment, then `onSuccess()` → `popBackStack()`

`updateLines` in `PedidoRepositoryImpl`:
- Recomputes status: `PAID` if `paid >= newTotal`, `PARTIAL` if `paid > 0`, `PENDING` otherwise
- `detallePedidoDao.deleteByPedido` + `insertAll`
- `pedidoDao.updateAfterEdit(id, total, itemCount, status, paid, paidAt)` — writes all five columns in one shot, so `paid` is always persisted regardless of the new status

### PagoSheet — shared payment-type selector

Moved to `ui/common/PagoSheet.kt` (used by both CreacionPedidoScreen and EditarPedidoScreen).

**Status color normalization** (matches `PayChip`):
| Status | Icon | Active color | Tint bg |
|--------|------|-------------|---------|
| PAID | `Check` | `ext.green` | `ext.greenTint` |
| PARTIAL | `Payments` | `ext.blue` | `ext.blueTint` |
| PENDING | `Tag` | `ext.amber` | `ext.amberTint` |

`ext.blue` was added to `PedidosExtendedColors` (`DarkBlue = #4C8DF5`, `LightBlue = #2563EB`) — the base blue used for partial-payment state borders and icon tints.

**API parameters:**
- `initialStatus: PedidoStatus = PENDING` — pre-selects the matching radio
- `initialPaidAmount: Double = 0.0` — prefills the partial text field (used in edit context)
- `ctaLabel: String? = null` — null falls back to "Registrar pedido"; EditarPedidoScreen passes "Guardar cambios"

**`PartialAmountInput` border** uses `ext.blue` (focused) instead of `primary` (green) to keep the blue identity for partial-payment state.

---

## SaldoExtra — special pedido type

`isSaldoExtra: Boolean = false` is stored on `Pedido`/`PedidoEntity`. When `true`:
- No `detalle_pedido` rows are created
- `notes` stores the user-entered description
- `total` = entered amount; `paid = 0`; `status = PENDING`
- `PedidoRow` renders an amber Tag icon tile + "Saldo extra" label + amber "Manual" badge (no `PayChip`)

`CreateSaldoExtraUseCase(clienteId, description, amount, date)` handles creation. Screen: `SaldoExtraScreen` + `SaldoExtraFields` (route `SaldoExtraRoute(clienteId)`) — see `docs/features/clientes.md → SaldoExtra screen` for UI details.

`PedidoRepository.getAllUnpaid()` returns all non-PAID pedidos (including saldo-extra ones) — used by `MercadosViewModel` to compute per-mercado status.

---

## Open TODOs

- [ ] Wire "Calcular automáticamente" in `AgregarListaNegraScreen` using real pedido balance (`pedidoRepository.getByCliente(clienteId)` is available)
- [ ] Payment history list (chronological per pedido) — payments are currently accumulated via `updateStatus`; a separate `pagos` table would be needed to track individual payment events
