# Feature: Lista Negra

## Status: ✅ Done (Phase 7)

---

## Spec summary

Global view of all blacklisted clients across all mercados. Accessible from the Lista Negra entry in `MercadosScreen` or the "Ver Lista Negra de este mercado" button in `ClientesScreen`. Adding a client is triggered from `DetalleClienteScreen` and records the owed balance and a mandatory reason.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Lista Negra (global) | `ListaNegraRoute` | `ui/screen/lista_negra/ListaNegraScreen.kt` |
| Agregar a Lista Negra | `AgregarListaNegraRoute(clienteId)` | `ui/screen/lista_negra/AgregarListaNegraScreen.kt` |

---

## Entry points

| From | Action | Navigates to |
|------|--------|--------------|
| `MercadosScreen` | "Lista Negra" card button | `ListaNegraRoute` |
| `ClientesScreen` | "Ver Lista Negra de este mercado" button | `ListaNegraRoute` |
| `DetalleClienteScreen` | "Agregar a Lista Negra" button (hidden when already blacklisted) | `AgregarListaNegraRoute(clienteId)` |

---

## UI implementation notes

### ListaNegraScreen

- AppBar: "Lista Negra", subtitle "Todos los mercados · solo lectura", back + search icon
- Search bar (`AnimatedVisibility`) — filters by client name
- **Red summary banner** (when list is non-empty): `Block` icon + "N clientes vetados" + "Saldo total irrecuperable · Bs. XX,XX"
- `BlacklistRow`: 46dp `ClienteAvatar` (supports `photoUrl`) + name + balance (red monospace, right-aligned) + mercado name + reason (with `Block` icon, `text3` color) + "Agregado el {date}" (`text4`). Tapping a row navigates to `DetalleClienteRoute(clienteId)`.
- Divider at `start = 79.dp`
- **Empty state** (no blacklisted clients): `Block` icon, "Lista negra vacía", no hint
- **Search empty state** (query yields no rows): `Search` icon, "Sin resultados", compact, 280dp height
- Mercado name is resolved at ViewModel level via `combine` — no extra DB join needed

### AgregarListaNegraScreen

- AppBar: "Agregar a Lista Negra", subtitle = client name (loaded from repo)
- **Pedidos pendientes section** — `PendingPedidosList`: rounded 14dp card (`surface2` bg, 1dp `border`). Each `PendingPedidoRow` shows title + date (left) and pending amount in monospace (right), with `HorizontalDivider` between rows. Empty state: centered "Sin pedidos pendientes". Title is "Saldo extra" for `isSaldoExtra`, "Pedido" otherwise.
- **Total adeudado section** — `TotalModeCard` composable renders both options:
  - **AUTO** — when selected: `accentTint` bg, 1.5dp primary border, filled primary circle with white `Check` icon, trailing `Text` with `autoAmount` at 17sp bold in primary color
  - **MANUAL** — when selected: same accent styling; `OutlinedTextField` (`Bs.` prefix, decimal keyboard) appears below the card
  - Unselected card: `surface2` bg, 1dp `border`, empty circle with `border3` outline
  - Default mode on first load: AUTO when `pendingPedidos.isNotEmpty()`, MANUAL otherwise; user selection preserved after initial load
- **Motivo del veto** — multiline `OutlinedTextField`, required, min 120dp height
- **Sticky bottom bar**: "Confirmar y agregar a Lista Negra" danger button (`redTint` bg, `redText` color, `Block` icon). Enabled only when reason non-blank AND `effectiveAmount > 0`.
- On confirm: calls `blacklist()` → pops back to `DetalleClienteScreen`. Because `DetalleClienteViewModel` observes `getByIdFlow()`, the screen reactively updates: `isBlacklisted` becomes `true` and the "Agregar a Lista Negra" button disappears without any manual refresh

---

## State

```kotlin
// ListaNegraUiState.kt
data class BlacklistUiModel(
    val clienteId: String,
    val name: String,
    val photoUrl: String?,
    val mercadoName: String,
    val blacklistBalance: Double,
    val blacklistReason: String?,
    val blacklistedAt: Long?,
)

data class ListaNegraUiState(
    val items: List<BlacklistUiModel> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
) {
    val filtered: List<BlacklistUiModel>  // name-filtered subset
    val totalBalance: Double              // sum of all blacklistBalance
}

// AgregarListaNegraUiState.kt
enum class TotalMode { AUTO, MANUAL }

data class AgregarListaNegraUiState(
    val clienteId: String = "",
    val clienteName: String = "",
    val pendingPedidos: List<Pedido> = emptyList(),
    val totalMode: TotalMode = TotalMode.MANUAL,
    val manualAmount: String = "",
    val reason: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) {
    val autoAmount: Double get() = pendingPedidos.sumOf { it.pending }
    val effectiveAmount: Double get() = when (totalMode) {
        TotalMode.AUTO -> autoAmount
        TotalMode.MANUAL -> manualAmount.toDoubleOrNull() ?: 0.0
    }
    val canConfirm: Boolean get() = reason.isNotBlank() && effectiveAmount > 0.0
}
```

---

## ViewModels

**`ListaNegraViewModel`** — injects `ClienteRepository` + `MercadoRepository`. Uses `combine(getBlacklisted(), mercadoAll, query)` for reactive search and mercado name resolution.

**`AgregarListaNegraViewModel`** — injects `ClienteRepository` + `PedidoRepository`. Uses `combine(clienteRepository.getByIdFlow, pedidoRepository.getByCliente)` to reactively load the client name and filter non-PAID pedidos. Default `TotalMode` is set on first load (AUTO when pending list is non-empty, MANUAL otherwise) and preserved after. `onConfirm()` calls `clienteRepository.blacklist(id, reason, effectiveAmount, at, isManualAmount = totalMode == MANUAL)` then invokes the success callback.

---

## Data layer

`blacklistBalance: Double` and `blacklistIsManualAmount: Boolean` are stored on `ClienteEntity` / `Cliente`.

| Column | Migration | Purpose |
|--------|-----------|---------|
| `blacklistBalance` | 7→8 | Amount owed at the time of blacklisting |
| `blacklistIsManualAmount` | 11→12 | `true` when the user typed the amount manually (MANUAL mode), `false` for AUTO (computed from pedidos) |

`blacklistIsManualAmount` drives:
- Whether `DetalleClienteScreen` shows the `BlacklistBalanceBlock` (manual amount) and dims `BalanceBlock`
- Whether "Quitar de Lista Negra" opens the resolution sheet or unblacklists immediately

### DAO operations

`blacklistBalance: Double` was added to `ClienteEntity` and `Cliente` in Room migration **7→8**:

```sql
ALTER TABLE clientes ADD COLUMN blacklistBalance REAL NOT NULL DEFAULT 0.0
```

DAO operations on `ClienteDao`:

```kotlin
@Query("SELECT * FROM clientes WHERE isBlacklisted = 1 ORDER BY blacklistedAt DESC")
fun getBlacklisted(): Flow<List<ClienteEntity>>

@Query("UPDATE clientes SET isBlacklisted = 1, blacklistReason = :reason, blacklistBalance = :balance, blacklistedAt = :at, blacklistIsManualAmount = :isManualAmount WHERE id = :id")
suspend fun blacklist(id: String, reason: String, balance: Double, at: Long, isManualAmount: Boolean)

@Query("UPDATE clientes SET isBlacklisted = 0, blacklistReason = NULL, blacklistBalance = 0, blacklistIsManualAmount = 0, blacklistedAt = NULL WHERE id = :id")
suspend fun unblacklist(id: String)
```

DAO operations added to `PedidoDao`:
```kotlin
@Query("UPDATE pedidos SET status = 'PAID', paid = total, paidAt = :paidAt WHERE clienteId = :clienteId AND status != 'PAID' AND isDeleted = 0")
suspend fun markAllPaidForCliente(clienteId: String, paidAt: Long)

// Same predicate, read before the update so each settled pedido can be queued for sync.
@Query("SELECT * FROM pedidos WHERE clienteId = :clienteId AND status != 'PAID' AND isDeleted = 0")
suspend fun unpaidForCliente(clienteId: String): List<PedidoEntity>
```

⚠️ The bulk `UPDATE` has no single entity id, so `PedidoRepositoryImpl.markAllPaidForCliente` reads
the affected rows **first** — afterwards the predicate matches nothing — and enqueues one `UPSERT`
per settled pedido. Until Phase 17h it enqueued nothing at all, so the settlement stayed on the
device while the saldo extra created alongside it synced normally.

All methods are exposed through their respective repository interfaces and impls.

---

## Files

| File | Description |
|------|-------------|
| `ui/screen/lista_negra/ListaNegraUiState.kt` | `BlacklistUiModel` + `ListaNegraUiState` |
| `ui/screen/lista_negra/ListaNegraViewModel.kt` | Reactive list with mercado resolution |
| `ui/screen/lista_negra/ListaNegraScreen.kt` | Full list UI with search and banner; rows are now clickable → `DetalleClienteRoute` |
| `ui/screen/lista_negra/AgregarListaNegraUiState.kt` | `TotalMode` enum + form state with `autoAmount`/`effectiveAmount`/`canConfirm` |
| `ui/screen/lista_negra/AgregarListaNegraViewModel.kt` | `combine` flow for client + pending pedidos; default mode logic; calls `blacklist()` with `isManualAmount` |
| `ui/screen/lista_negra/AgregarListaNegraScreen.kt` | Screen entry + `AgregarListaNegraContent` + `SectionLabel` + full-screen previews |
| `ui/screen/lista_negra/PendingPedidosSection.kt` | `PendingPedidosList` + `PendingPedidoRow` composables with previews |
| `ui/screen/lista_negra/TotalModeCard.kt` | `TotalModeCard` composable (AUTO/MANUAL radio-style card) with previews |
| `ui/screen/cliente/DetalleClienteBalance.kt` | `BalanceBlock` (unified — handles normal, AUTO-LN, MANUAL-LN states) + `BalanceCaption` + `BalanceCard` + `BalanceBreakdown` |
| `ui/screen/cliente/QuitarListaNegraSheet.kt` | `ModalBottomSheet` with radio-style option cards matching the mockup: header icon + title + subtitle (bold client name); "Restaurar pedidos y saldos" (blue, pre-selected) + "Marcar todo como pagado" (green, disabled when AUTO); amber info banner when manual; confirm + cancel buttons |

---

## Open TODOs

- [x] Tap row in `ListaNegraScreen` → navigate to `DetalleClienteRoute(clienteId)`
- [x] Unified `BalanceBlock` with AUTO/MANUAL/normal states replacing `BlacklistBalanceBlock`
- [x] `BalanceBreakdown` cards (Pedidos/Extra/LN) with inactive state for MANUAL
- [x] `QuitarListaNegraSheet` — resolution sheet with mockup-faithful radio option cards (Restaurar / Marcar como pagados)
- [x] Excess saldo extra on "Marcar todo como pagado": if `blacklistBalance > pendingSum`, a saldo extra is created for the difference via `CreateSaldoExtraUseCase` before marking all pedidos as paid
