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
- `BlacklistRow`: 46dp `ClienteAvatar` (supports `photoUrl`) + name + balance (red monospace, right-aligned) + mercado name + reason (with `Block` icon, `text3` color) + "Agregado el {date}" (`text4`)
- Divider at `start = 79.dp`
- **Empty state** (no blacklisted clients): `Block` icon, "Lista negra vacía", no hint
- **Search empty state** (query yields no rows): `Search` icon, "Sin resultados", compact, 280dp height
- Mercado name is resolved at ViewModel level via `combine` — no extra DB join needed

### AgregarListaNegraScreen

- AppBar: "Agregar a Lista Negra", subtitle = client name (loaded from repo)
- **Pedidos pendientes section** — Phase 4 placeholder: shows a `Receipt` icon card with "Disponible cuando se implementen los pedidos (Fase 4)"
- **Total adeudado section** — two radio-style cards:
  - "Calcular automáticamente" — shown as a permanently disabled (unselected, grayed) card with note "Disponible en Fase 4 (requiere pedidos)"
  - "Ingresar manualmente" — always selected (accent tint background, primary border, filled radio dot); below it: `OutlinedTextField` with `Bs.` prefix, decimal keyboard
- **Motivo del veto** — multiline `OutlinedTextField`, required, min 120dp height
- **Sticky bottom bar**: "Confirmar y agregar a Lista Negra" danger button (`redTint` bg, `redText` color, `Block` icon). Enabled only when reason non-blank AND amount > 0.
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
data class AgregarListaNegraUiState(
    val clienteId: String = "",
    val clienteName: String = "",
    val manualAmount: String = "",
    val reason: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
) {
    val canConfirm get() = reason.isNotBlank() && (manualAmount.toDoubleOrNull() ?: 0.0) > 0.0
}
```

---

## ViewModels

**`ListaNegraViewModel`** — injects `ClienteRepository` + `MercadoRepository`. Uses `combine(getBlacklisted(), mercadoAll, query)` for reactive search and mercado name resolution.

**`AgregarListaNegraViewModel`** — injects `ClienteRepository`. Loads client name from `getById`. `onConfirm()` calls `clienteRepository.blacklist(id, reason, balance, at)` then invokes the success callback.

---

## Data layer

`blacklistBalance: Double` was added to `ClienteEntity` and `Cliente` in Room migration **7→8**:

```sql
ALTER TABLE clientes ADD COLUMN blacklistBalance REAL NOT NULL DEFAULT 0.0
```

New DAO operations added to `ClienteDao`:

```kotlin
@Query("SELECT * FROM clientes WHERE isBlacklisted = 1 ORDER BY blacklistedAt DESC")
fun getBlacklisted(): Flow<List<ClienteEntity>>

@Query("UPDATE clientes SET isBlacklisted = 1, blacklistReason = :reason, blacklistBalance = :balance, blacklistedAt = :at WHERE id = :id")
suspend fun blacklist(id: String, reason: String, balance: Double, at: Long)
```

Both methods are exposed through `ClienteRepository` / `ClienteRepositoryImpl`.

---

## Files

| File | Description |
|------|-------------|
| `ui/screen/lista_negra/ListaNegraUiState.kt` | `BlacklistUiModel` + `ListaNegraUiState` |
| `ui/screen/lista_negra/ListaNegraViewModel.kt` | Reactive list with mercado resolution |
| `ui/screen/lista_negra/ListaNegraScreen.kt` | Full list UI with search and banner |
| `ui/screen/lista_negra/AgregarListaNegraUiState.kt` | Form state + `canConfirm` |
| `ui/screen/lista_negra/AgregarListaNegraViewModel.kt` | Loads client name; calls `blacklist()` |
| `ui/screen/lista_negra/AgregarListaNegraScreen.kt` | Phased form UI + danger CTA |

---

## Open TODOs

- [ ] Phase 4: replace "Pedidos pendientes" placeholder with real pending pedidos list
- [ ] Phase 4: enable "Calcular automáticamente" — sum pending pedidos for the client
- [ ] Tap row in `ListaNegraScreen` → navigate to `DetalleClienteRoute(clienteId)` (client detail is read-only when blacklisted, but useful for audit)
