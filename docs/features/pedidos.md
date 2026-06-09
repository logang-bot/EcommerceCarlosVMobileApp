# Feature: Pedidos

## Status: ✅ Done (Phase 4 + Phase 5)

---

## Spec summary

Two sub-features:
1. **Creación de Pedido** *(Phase 4 — ✅ Done)* — "add to cart" flow: product grid, inline quantity controls, cart panel, price-override warning, confirm + payment sheet.
2. **Detalle de Pedido** *(Phase 5 — 🔲 Pending)* — view line items, payment history, mark as paid or register partial payment.

---

## Screens

| Screen | Route | File | Status |
|--------|-------|------|--------|
| Creación de Pedido | `CreacionPedidoRoute(clienteId, clienteName, mercadoName)` | `ui/screen/pedido/CreacionPedidoScreen.kt` | ✅ Done |
| Detalle de Pedido | `DetallePedidoRoute(pedidoId)` | `ui/screen/pedido/DetallePedidoScreen.kt` | ✅ Done |

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
| `PedidoDao.updateDate(id, createdAt)` — `UPDATE pedidos SET createdAt = :createdAt` | ✅ |
| `PedidoRepository.updateDate(id, createdAt)` + `PedidoRepositoryImpl` impl | ✅ |

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
- `PedidoRow` (in `ui/screen/cliente/`) renders each pedido: icon tile, product count + date, total (mono), `PayChip`
- Empty state shown when no pedidos exist
- FAB navigates to `CreacionPedidoRoute(clienteId, clienteName, mercadoName)`

---

## UI — Phase 5: Detalle de Pedido

### Files

| File | Responsibility |
|------|---------------|
| `DetallePedidoUiState.kt` | `pedido`, `detalles`, `clienteName`, `isLoading`, `isSaving`, `showPagoSheet`, `showDeleteConfirm`, `showDatePicker` |
| `DetallePedidoViewModel.kt` | `@HiltViewModel`; nested `combine` (5 flows) + two chained `.combine` for modal flags; loads `clienteName` via `ClienteRepository`; `onMarcarPagado()`, `onRegistrarPago(amount)`, sheet toggles, `onDeletePedido()`, `onUpdateDate(createdAt)` |
| `DetallePedidoScreen.kt` | Scaffold entry + content, `PedidoStatusStrip`, `SaldoExtraBody`, `PedidoOverflowMenu`, `PedidoMenuItem`, delete `AlertDialog`, `DatePickerDialog` |
| `DetallePedidoActions.kt` | `DetallePedidoBottomBar`, `PagoParcialSheet`, `PagoParcialSheetContent` |
| `DetallePedidoLineItem.kt` | `LineItemsSection`, `LineItemRow`, `PriceModifiedHint` |
| `DetallePedidoSummary.kt` | `TotalBlock`, `PagosSection`, `DetalleSectionLabel` |

### Screen structure

`DetallePedidoScreen` uses a `Scaffold` with:
- `topBar` = `PedidosTopBar` (title "Pedido", subtitle = formatted date — e.g. "28 may 2026") + `PedidoOverflowMenu` in the `actions` slot
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

### PedidoOverflowMenu

`DropdownMenu` composable anchored to a `MoreVert` `IconButton` in the top-bar `actions` slot. Matches `ClientesFilterMenu` design (`elevated` container color, `border2` 1dp stroke, `RoundedCornerShape(16.dp)`, `shadowElevation = 16.dp`, width 220dp). Two `PedidoMenuItem` rows:

- **Modificar fecha** (`CalendarToday` icon) — opens a Material3 `DatePickerDialog` pre-filled with `pedido.createdAt`. On confirm, `selectedDateMillis` is adjusted from UTC midnight to local midnight (`utcMidnight - TimeZone.getDefault().getOffset(utcMidnight)`) before calling `onUpdateDate(Long)`. This prevents the date appearing one day early on UTC-negative devices.
- **Eliminar pedido** (`Delete` icon, `error` color) — opens an `AlertDialog` confirmation. On confirm, calls `pedidoRepository.delete(pedidoId)` and pops back.

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

`onDeletePedido(onSuccess)` — calls `pedidoRepository.delete(pedidoId)` then invokes the callback.

`onUpdateDate(createdAt)` — calls `pedidoRepository.updateDate(pedidoId, createdAt)`; dismisses the date picker.

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
